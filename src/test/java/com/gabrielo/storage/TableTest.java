package com.gabrielo.storage;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TableTest {

	@Test
	void getAllDataOnEmptyTable() {
		final Table table = new Table(new ArrayList<>());

		assertThat(table.getAllData()).isEqualTo(List.of());
	}

	@Test
	void insertIntoDatabaseSingleRecord() {
		final Table table = new Table(new ArrayList<>());

		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");

		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

	@Test
	void insertIntoDatabaseMultipleRecords() {
		final Table table = new Table(new ArrayList<>());

		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		table.insert(2, "Brielingson", "brielingson@pescao.com");
		table.insert(3, "Gabrielin", "gabrielin@pescao.com");

		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com")));
	}
}
