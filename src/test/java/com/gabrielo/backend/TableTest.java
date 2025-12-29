package com.gabrielo.backend;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TableTest {

	@Test
	void getAllDataOnEmptyTable() {
		Table table = new Table();

		assertThat(table.getAllData()).isEqualTo(List.of());
	}

	@Test
	void insertIntoDatabaseSingleRecord() {
		Table table = new Table();

		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");

		assertThat(table.getAllData())
				.isEqualTo(List.of(new com.gabrielo.backend.Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

	@Test
	void insertIntoDatabaseMultipleRecords() {
		Table table = new Table();

		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		table.insert(2, "Brielingson", "brielingson@pescao.com");
		table.insert(3, "Gabrielin", "gabrielin@pescao.com");

		assertThat(table.getAllData())
				.isEqualTo(List.of(new com.gabrielo.backend.Record(1, "Gabrielo", "gabrielodon@pescao.com"),
						new com.gabrielo.backend.Record(2, "Brielingson", "brielingson@pescao.com"),
						new Record(3, "Gabrielin", "gabrielin@pescao.com")));
	}
}
