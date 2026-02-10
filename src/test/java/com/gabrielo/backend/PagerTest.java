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

class PagerTest {

	private static final int PAGE_SIZE = 4096;

	private static final int RECORDS_PER_PAGE = PAGE_SIZE / 68;

	@TempDir
	Path tempDir;

	private Pager pager;

	@BeforeEach
	void beforeEach() {
		Path dbFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		pager = new Pager(diskManager);
	}

	@Test
	@SneakyThrows
	void insertsSingleRecordAndReadsIt() {
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");

		pager.insert(record);

		assertThat(pager.getRecordAt(0, 0)).isEqualTo(record);
	}

	@Test
	@SneakyThrows
	void readingRecordDoesNotDeleteIt() {
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
		pager.insert(record);
		pager.getRecordAt(0, 0);

		assertThat(pager.getRecordAt(0, 0)).isEqualTo(record);
	}

	@Test
	@SneakyThrows
	void insertsMultipleRecordsAndReadsThem() {
		Record record1 = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
		Record record2 = new Record(2, "Brielingson", "brielingson@pescao.com");
		Record record3 = new Record(3, "Gabrielin", "gabrielin@pescao.com");
		pager.insert(record1);
		pager.insert(record2);
		pager.insert(record3);

		assertThat(pager.getRecordAt(0, 0)).isEqualTo(record1);
		assertThat(pager.getRecordAt(0, 1)).isEqualTo(record2);
		assertThat(pager.getRecordAt(0, 2)).isEqualTo(record3);
	}

	@Test
	@SneakyThrows
	void handlesInsertsOfMoreRecordsThanTheFittingInOnePage() {
		for (int i = 0; i < RECORDS_PER_PAGE; i++) {
			pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}

		for (int i = 0; i < RECORDS_PER_PAGE; i++) {
			assertThat(pager.getRecordAt(0, i)).isEqualTo(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}
	}

	@Test
	@SneakyThrows
	void handlesInsertsOfMoreRecordsThanTheFittingInMemoryPages() {
		int numberOfPages = 105;
		for (int j = 0; j < numberOfPages; j++) {
			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
			}
		}

		for (int j = 0; j < numberOfPages; j++) {
			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				assertThat(pager.getRecordAt(j, i))
						.isEqualTo(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
			}
		}
	}

	@Test
	@SneakyThrows
	void insertsWhenPagesExistOnDiskButNotInMemory() {
		Path dbFile = tempDir.resolve("persistence_insert.ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager1 = new Pager(diskManager);

		// Insert records and flush to disk
		pager1.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
		pager1.insert(new Record(2, "Brielingson", "brielingson@pescao.com"));
		pager1.flushAllPages();

		// Create new pager with same disk - simulates restart, memory is empty
		Pager pager2 = new Pager(diskManager);
		pager2.insert(new Record(3, "Gabrielin", "gabrielin@pescao.com"));

		assertThat(pager2.getRecordAt(0, 0)).isEqualTo(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
		assertThat(pager2.getRecordAt(0, 1)).isEqualTo(new Record(2, "Brielingson", "brielingson@pescao.com"));
		assertThat(pager2.getRecordAt(0, 2)).isEqualTo(new Record(3, "Gabrielin", "gabrielin@pescao.com"));
	}

	@Test
	@SneakyThrows
	void getsAllRecordsFromDiskWhenNoneInMemory() {
		Path dbFile = tempDir.resolve("persistence_read.ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager1 = new Pager(diskManager);

		// Insert records and flush to disk
		pager1.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
		pager1.insert(new Record(2, "Brielingson", "brielingson@pescao.com"));
		pager1.insert(new Record(3, "Gabrielin", "gabrielin@pescao.com"));
		pager1.flushAllPages();

		// Create new pager with same disk - simulates restart, memory is empty
		Pager pager2 = new Pager(diskManager);

		assertThat(pager2.getRecordAt(0, 0)).isEqualTo(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
		assertThat(pager2.getRecordAt(0, 1)).isEqualTo(new Record(2, "Brielingson", "brielingson@pescao.com"));
		assertThat(pager2.getRecordAt(0, 2)).isEqualTo(new Record(3, "Gabrielin", "gabrielin@pescao.com"));
	}

	@Test
	@SneakyThrows
	void getTotalPagesCreatedReturnsZeroWhenNoRecordsInserted() {
		assertThat(pager.getTotalPagesCreated()).isZero();
	}

	@Test
	@SneakyThrows
	void getTotalPagesCreatedReturnsOneAfterInsertingSingleRecord() {
		pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));

		assertThat(pager.getTotalPagesCreated()).isEqualTo(1);
	}

	@Test
	@SneakyThrows
	void getTotalPagesCreatedReturnsOneWhenRecordsFitInOnePage() {
		int recordsToInsert = RECORDS_PER_PAGE - 1;
		for (int i = 0; i < recordsToInsert; i++) {
			pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}

		assertThat(pager.getTotalPagesCreated()).isEqualTo(1);
	}

	@Test
	@SneakyThrows
	void getTotalPagesCreatedReturnsTwoWhenRecordsExceedOnePage() {
		for (int i = 0; i <= RECORDS_PER_PAGE; i++) {
			pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}

		assertThat(pager.getTotalPagesCreated()).isEqualTo(2);
	}

	@Test
	@SneakyThrows
	void getTotalPagesCreatedReturnsCorrectCountForMultiplePages() {
		int numberOfPages = 5;

		for (int j = 0; j < numberOfPages; j++) {
			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
			}
		}

		assertThat(pager.getTotalPagesCreated()).isEqualTo(numberOfPages);
	}

	@Test
	@SneakyThrows
	void getTotalPagesCreatedReturnsCorrectCountAfterRestart() {
		Path dbFile = tempDir.resolve("pages_count_persistence.ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager1 = new Pager(diskManager);
		int numberOfPages = 3;

		for (int j = 0; j < numberOfPages; j++) {
			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				pager1.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
			}
		}
		pager1.flushAllPages();

		// Create new pager with same disk - simulates restart
		Pager pager2 = new Pager(diskManager);

		assertThat(pager2.getTotalPagesCreated()).isEqualTo(numberOfPages);
	}

	@Nested
	class GetRecordAt {

		@Test
		@SneakyThrows
		void when_single_record_exists_expect_record_returned() {
			Record expected = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			pager.insert(expected);

			Record result = pager.getRecordAt(0, 0);

			assertThat(result).isEqualTo(expected);
		}

		@Test
		@SneakyThrows
		void when_multiple_records_in_page_expect_correct_record_returned() {
			Record first = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			Record second = new Record(2, "Maria", "maria@example.com");
			Record third = new Record(3, "Juan", "juan@example.com");
			pager.insert(first);
			pager.insert(second);
			pager.insert(third);

			Record result = pager.getRecordAt(0, 1);

			assertThat(result).isEqualTo(second);
		}

		@Test
		@SneakyThrows
		void when_invalid_record_index_expect_exception() {
			pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));

			ThrowingCallable result = () -> pager.getRecordAt(0, 5);

			assertThatThrownBy(result).isInstanceOf(IndexOutOfBoundsException.class);
		}

		@Test
		@SneakyThrows
		void when_negative_record_index_expect_exception() {
			pager.insert(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));

			ThrowingCallable result = () -> pager.getRecordAt(0, -1);

			assertThatThrownBy(result).isInstanceOf(IndexOutOfBoundsException.class);
		}

		@Test
		@SneakyThrows
		void when_record_on_disk_expect_record_loaded_and_returned() {
			Path dbFile = tempDir.resolve("persistence.ddb");
			DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
			Pager pager1 = new Pager(diskManager);
			Record expected = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
			pager1.insert(expected);
			pager1.flushAllPages();
			Pager pager2 = new Pager(diskManager);

			Record result = pager2.getRecordAt(0, 0);

			assertThat(result).isEqualTo(expected);
		}

		@Test
		@SneakyThrows
		void when_record_in_second_page_expect_correct_record_returned() {
			for (int i = 0; i < RECORDS_PER_PAGE; i++) {
				pager.insert(new Record(i, "User" + i, "user" + i + "@example.com"));
			}
			Record expectedInSecondPage = new Record(100, "SecondPage", "secondpage@example.com");
			pager.insert(expectedInSecondPage);

			Record result = pager.getRecordAt(1, 0);

			assertThat(result).isEqualTo(expectedInSecondPage);
		}
	}
}
