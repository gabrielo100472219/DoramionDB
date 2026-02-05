package com.gabrielo.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TableTest {

	private static final int PAGE_SIZE = 4096;

	@TempDir
	Path tempDir;

	private Table createTable() {
		Path dbFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager = new Pager(diskManager);
		return new Table(pager);
	}

	@Test
	void getAllDataOnEmptyTable() {
		Table table = createTable();

		assertThat(table.getAllData()).isEqualTo(List.of());
	}

	@Test
	void insertIntoDatabaseSingleRecord() {
		Table table = createTable();

		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");

		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

	@Test
	void insertIntoDatabaseMultipleRecords() {
		Table table = createTable();

		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		table.insert(2, "Brielingson", "brielingson@pescao.com");
		table.insert(3, "Gabrielin", "gabrielin@pescao.com");

		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com")));
	}
}
