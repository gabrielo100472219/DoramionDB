package com.gabrielo.backend;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagerTest {

	@Test
	void insertsSingleRecordAndReadsIt() {
		Pager pager = new Pager();
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");

		pager.insert(record);

		assertThat(pager.getAllRecords()).isEqualTo(List.of(record));
	}

	@Test
	void readingRecordDoesNotDeleteIt() {
		Pager pager = new Pager();
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");

		pager.insert(record);
		pager.getAllRecords();

		assertThat(pager.getAllRecords()).isEqualTo(List.of(record));
	}

	@Test
	void insertsMultipleRecordsAndReadsThem() {
		Pager pager = new Pager();
		List<Record> recordsToInsert = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));

		recordsToInsert.forEach(pager::insert);

		assertThat(pager.getAllRecords()).isEqualTo(recordsToInsert);
	}

	@Test
	void handlesInsertsOfMoreRecordsThanTheFittingInOnePage() {
		Pager pager = new Pager();
		int recordsPerPage = 4096 / 68;

		for (int i = 0; i < recordsPerPage; i++) {
			pager.insert(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}
		var result = pager.getAllRecords();
		assertThat(result).hasSize(recordsPerPage);
		for (int i = 0; i < recordsPerPage; i++) {
			assertThat(result.get(i)).isEqualTo(new Record(i, "Gabrielo" + i, "gabrielodon@pescao.com" + i));
		}
	}
}