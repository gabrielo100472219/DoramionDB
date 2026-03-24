package com.gabrielo.backend;

import com.gabrielo.backend.btree.BTree;
import com.gabrielo.backend.disk.DiskManager;
import com.gabrielo.backend.pager.Pager;
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

	private Pager pager;

	private BTree bTree;

	@BeforeEach
	@SneakyThrows
	void beforeEach(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, PAGE_SIZE);
		pager = new Pager(diskManager);
		bTree = new BTree(pager);
	}

	private void insertRecord(int id, String name, String email) throws Exception {
		RecordSerializer serializer = new RecordSerializer();
		Record record = new Record(id, name, email);
		bTree.insert(id, serializer.serialize(record));
	}

	@Nested
	class HasNext {

		@Test
		@SneakyThrows
		void when_no_records_exist_expect_false() {
			Cursor cursor = new Cursor(pager, bTree);

			boolean result = cursor.hasNext();

			assertThat(result).isFalse();
		}

		@Test
		@SneakyThrows
		void when_single_record_exists_expect_true() {
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			Cursor cursor = new Cursor(pager, bTree);

			boolean result = cursor.hasNext();

			assertThat(result).isTrue();
		}

		@Test
		@SneakyThrows
		void when_multiple_records_in_single_leaf_expect_true() {
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			insertRecord(2, "Maria", "maria@example.com");
			insertRecord(3, "Juan", "juan@example.com");
			Cursor cursor = new Cursor(pager, bTree);

			boolean result = cursor.hasNext();

			assertThat(result).isTrue();
		}

		@Test
		@SneakyThrows
		void when_multiple_leaves_exist_expect_true() {
			for (int i = 0; i < 60; i++) {
				insertRecord(i, "User" + i, "user" + i + "@example.com");
			}
			Cursor cursor = new Cursor(pager, bTree);

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
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			Cursor cursor = new Cursor(pager, bTree);

			Record result = cursor.next();

			assertThat(result).isEqualTo(expected);
		}

		@Test
		@SneakyThrows
		void when_multiple_records_exist_expect_first_record_returned() {
			Record first = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			insertRecord(2, "Maria", "maria@example.com");
			Cursor cursor = new Cursor(pager, bTree);

			Record result = cursor.next();

			assertThat(result).isEqualTo(first);
		}

		@Test
		@SneakyThrows
		void when_called_twice_expect_second_record_returned() {
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			insertRecord(2, "Maria", "maria@example.com");
			Cursor cursor = new Cursor(pager, bTree);

			cursor.next();
			Record result = cursor.next();

			assertThat(result).isEqualTo(new Record(2, "Maria", "maria@example.com"));
		}

		@Test
		@SneakyThrows
		void when_iterating_all_records_expect_all_records_returned_in_order() {
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			insertRecord(2, "Maria", "maria@example.com");
			insertRecord(3, "Juan", "juan@example.com");
			Cursor cursor = new Cursor(pager, bTree);

			assertThat(cursor.next()).isEqualTo(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
			assertThat(cursor.next()).isEqualTo(new Record(2, "Maria", "maria@example.com"));
			assertThat(cursor.next()).isEqualTo(new Record(3, "Juan", "juan@example.com"));
		}

		@Test
		@SneakyThrows
		void when_leaf_boundary_crossed_expect_records_from_next_leaf() {
			// Insert enough records to force a leaf split (>56)
			int totalRecords = 60;
			for (int i = 0; i < totalRecords; i++) {
				insertRecord(i, "User" + i, "user" + i + "@example.com");
			}
			Cursor cursor = new Cursor(pager, bTree);

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
		void when_iterating_across_multiple_leaves_expect_all_records_returned() {
			int totalRecords = 200;
			for (int i = 0; i < totalRecords; i++) {
				insertRecord(i, "User" + i, "user" + i + "@example.com");
			}
			Cursor cursor = new Cursor(pager, bTree);

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
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			Cursor cursor = new Cursor(pager, bTree);
			cursor.next();

			ThrowingCallable result = cursor::next;

			assertThatThrownBy(result).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@SneakyThrows
		void when_no_records_exist_expect_exception() {
			Cursor cursor = new Cursor(pager, bTree);

			ThrowingCallable result = cursor::next;

			assertThatThrownBy(result).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@SneakyThrows
		void when_has_next_returns_false_and_next_called_expect_exception() {
			insertRecord(1, "Gabrielo", "gabrielodon@pescao.com");
			insertRecord(2, "Maria", "maria@example.com");
			Cursor cursor = new Cursor(pager, bTree);

			while (cursor.hasNext()) {
				cursor.next();
			}
			ThrowingCallable result = cursor::next;

			assertThatThrownBy(result).isInstanceOf(IllegalStateException.class);
		}
	}
}
