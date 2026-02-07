package com.gabrielo.backend;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagerTest {

	private static final int PAGE_SIZE = 4096;
	int recordsPerPage = PAGE_SIZE / 68;

	@TempDir
	Path tempDir;

	private Pager createPager() {
		Path dbFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		return new Pager(diskManager);
	}

	@Test
	@SneakyThrows
	void insertsSingleRecordAndReadsIt() {
		Pager pager = createPager();
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");

		pager.insert(record);

		assertThat(pager.getAllRecords()).isEqualTo(List.of(record));
	}

	@Test
	@SneakyThrows
	void readingRecordDoesNotDeleteIt() {
		Pager pager = createPager();
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");

		pager.insert(record);
		pager.getAllRecords();

		assertThat(pager.getAllRecords()).isEqualTo(List.of(record));
	}

	@Test
	@SneakyThrows
	void insertsMultipleRecordsAndReadsThem() {
		Pager pager = createPager();
		List<Record> recordsToInsert = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));

		for (Record record : recordsToInsert) {
			pager.insert(record);
		}

		assertThat(pager.getAllRecords()).isEqualTo(recordsToInsert);
	}

	@Test
	@SneakyThrows
	void handlesInsertsOfMoreRecordsThanTheFittingInOnePage() {
		Pager pager = createPager();

		for (int i = 0; i < recordsPerPage; i++) {
			pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}
		var result = pager.getAllRecords();
		assertThat(result).hasSize(recordsPerPage);
		for (int i = 0; i < recordsPerPage; i++) {
			assertThat(result.get(i)).isEqualTo(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}
	}

	@Test
	@SneakyThrows
	void handlesInsertsOfMoreRecordsThanTheFittingInMemoryPages() {
		Pager pager = createPager();
		int numberOfPages = 105;

		for (int j = 0; j < numberOfPages; j++) {
			for (int i = 0; i < recordsPerPage; i++) {
				pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
			}
		}

		var result = pager.getAllRecords();
		assertThat(result).hasSize(recordsPerPage * numberOfPages);
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

		var result = pager2.getAllRecords();
		assertThat(result).containsExactly(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));
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

		var result = pager2.getAllRecords();
		assertThat(result).containsExactly(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));
	}
}