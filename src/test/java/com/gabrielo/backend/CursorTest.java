package com.gabrielo.backend;

import lombok.SneakyThrows;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorTest {

	private static final int PAGE_SIZE = 4096;

	private static final int RECORDS_PER_PAGE = PAGE_SIZE / 68;

	private Pager pager;

	@BeforeEach
	@SneakyThrows
	void beforeEach(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, PAGE_SIZE);
		pager = new Pager(diskManager);
	}

	@Nested
	class HasNext {

		@Test
		@SneakyThrows
		void when_no_pages_exist_expect_false() {
			Cursor cursor = new Cursor(pager);

			boolean result = cursor.hasNext();

			assertThat(result).isFalse();
		}

		@Test
		@SneakyThrows
		void when_single_record_exists_expect_true() {
			pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
			Cursor cursor = new Cursor(pager);

			boolean result = cursor.hasNext();

			assertThat(result).isTrue();
		}

		@Test
		@SneakyThrows
		void when_multiple_records_in_single_page_expect_true() {
			pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
			pager.insert(new Record(2, "Maria", "maria@example.com"));
			pager.insert(new Record(3, "Juan", "juan@example.com"));
			Cursor cursor = new Cursor(pager);

			boolean result = cursor.hasNext();

			assertThat(result).isTrue();
		}

		@Test
		@SneakyThrows
		void when_multiple_pages_exist_expect_true() {
			for (int i = 0; i < 50; i++) {
				pager.insert(new Record(i, "User" + i, "user" + i + "@example.com"));
			}
			Cursor cursor = new Cursor(pager);

			boolean result = cursor.hasNext();

			assertThat(result).isTrue();
		}
	}

	@Nested
	class Next {

		@Test
		@SneakyThrows
		void when_single_record_exists_expect_record_returned() {
			Record expected = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			pager.insert(expected);
			Cursor cursor = new Cursor(pager);

			Record result = cursor.next();

			assertThat(result).isEqualTo(expected);
		}

		@Test
		@SneakyThrows
		void when_multiple_records_exist_expect_first_record_returned() {
			Record first = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			Record second = new Record(2, "Maria", "maria@example.com");
			pager.insert(first);
			pager.insert(second);
			Cursor cursor = new Cursor(pager);

			Record result = cursor.next();

			assertThat(result).isEqualTo(first);
		}

		@Test
		@SneakyThrows
		void when_called_twice_expect_second_record_returned() {
			Record first = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			Record second = new Record(2, "Maria", "maria@example.com");
			pager.insert(first);
			pager.insert(second);
			Cursor cursor = new Cursor(pager);

			cursor.next();
			Record result = cursor.next();

			assertThat(result).isEqualTo(second);
		}

		@Test
		@SneakyThrows
		void when_iterating_all_records_expect_all_records_returned_in_order() {
			Record first = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			Record second = new Record(2, "Maria", "maria@example.com");
			Record third = new Record(3, "Juan", "juan@example.com");
			pager.insert(first);
			pager.insert(second);
			pager.insert(third);
			Cursor cursor = new Cursor(pager);

			assertThat(cursor.next()).isEqualTo(first);
			assertThat(cursor.next()).isEqualTo(second);
			assertThat(cursor.next()).isEqualTo(third);
		}

		@Test
		@SneakyThrows
		void when_page_boundary_crossed_expect_first_record_of_next_page_returned() {
			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				pager.insert(new Record(i, "User" + i, "user" + i + "@example.com"));
			}
			Record firstOfSecondPage = new Record(100, "SecondPage", "secondpage@example.com");
			pager.insert(firstOfSecondPage);
			Cursor cursor = new Cursor(pager);

			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				cursor.next();
			}
			Record result = cursor.next();

			assertThat(result).isEqualTo(firstOfSecondPage);
		}

		@Test
		@SneakyThrows
		void when_iterating_across_multiple_pages_expect_all_records_returned() {
			int totalRecords = RECORDS_PER_PAGE + 5;
			for (int i = 0; i < totalRecords; i++) {
				pager.insert(new Record(i, "User" + i, "user" + i + "@example.com"));
			}
			Cursor cursor = new Cursor(pager);

			int count = 0;
			while (cursor.hasNext()) {
				Record record = cursor.next();
				assertThat(record.id()).isEqualTo(count);
				count++;
			}

			assertThat(count).isEqualTo(totalRecords);
		}

		@Test
		@SneakyThrows
		void when_no_more_records_expect_exception() {
			pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
			Cursor cursor = new Cursor(pager);
			cursor.next();

			ThrowingCallable result = cursor::next;

			assertThatThrownBy(result).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@SneakyThrows
		void when_no_records_exist_expect_exception() {
			Cursor cursor = new Cursor(pager);

			ThrowingCallable result = cursor::next;

			assertThatThrownBy(result).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@SneakyThrows
		void when_has_next_returns_false_and_next_called_expect_exception() {
			pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
			pager.insert(new Record(2, "Maria", "maria@example.com"));
			Cursor cursor = new Cursor(pager);

			while (cursor.hasNext()) {
				cursor.next();
			}
			ThrowingCallable result = cursor::next;

			assertThatThrownBy(result).isInstanceOf(IllegalStateException.class);
		}
	}
}
