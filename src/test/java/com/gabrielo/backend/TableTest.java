package com.gabrielo.backend;

import com.gabrielo.backend.btree.BTree;
import com.gabrielo.backend.disk.DiskManager;
import com.gabrielo.backend.pager.Pager;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TableTest {

	private static final int PAGE_SIZE = 4096;

	@TempDir
	Path tempDir;

	private int testCounter = 0;

	private Table createTable() {
		Path dbFile = tempDir.resolve("test" + (testCounter++) + ".ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager = new Pager(diskManager);
		BTree bTree = new BTree(pager);
		return new Table(pager, bTree);
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

	@Test
	@SneakyThrows
	void dataSurvivesCloseAndReopen() {
		Path dbFile = tempDir.resolve("persist.ddb");

		// Session 1: insert data, close
		DiskManager dm1 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager1 = new Pager(dm1);
		BTree bTree1 = new BTree(pager1);
		Table table1 = new Table(pager1, bTree1);
		table1.open();

		table1.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		table1.insert(2, "Brielingson", "brielingson@pescao.com");
		table1.insert(3, "Gabrielin", "gabrielin@pescao.com");

		table1.close();

		// Session 2: reopen from same file, verify data
		DiskManager dm2 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager2 = new Pager(dm2);
		BTree bTree2 = new BTree(pager2);
		Table table2 = new Table(pager2, bTree2);
		table2.open();

		assertThat(table2.getAllData()).isEqualTo(List.of(
				new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com")));
	}

	@Test
	@SneakyThrows
	void dataSurvivesMultipleSessions() {
		Path dbFile = tempDir.resolve("multi_session.ddb");

		// Session 1: insert initial data
		DiskManager dm1 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager1 = new Pager(dm1);
		BTree bTree1 = new BTree(pager1);
		Table table1 = new Table(pager1, bTree1);
		table1.open();
		table1.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		table1.close();

		// Session 2: insert more data
		DiskManager dm2 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager2 = new Pager(dm2);
		BTree bTree2 = new BTree(pager2);
		Table table2 = new Table(pager2, bTree2);
		table2.open();
		table2.insert(2, "Brielingson", "brielingson@pescao.com");
		table2.close();

		// Session 3: verify all data from both sessions
		DiskManager dm3 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager3 = new Pager(dm3);
		BTree bTree3 = new BTree(pager3);
		Table table3 = new Table(pager3, bTree3);
		table3.open();

		assertThat(table3.getAllData()).isEqualTo(List.of(
				new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com")));
	}

	@Test
	@SneakyThrows
	void emptyDatabaseSurvivesCloseAndReopen() {
		Path dbFile = tempDir.resolve("empty_persist.ddb");

		// Session 1: open and close with no data
		DiskManager dm1 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager1 = new Pager(dm1);
		BTree bTree1 = new BTree(pager1);
		Table table1 = new Table(pager1, bTree1);
		table1.open();
		table1.close();

		// Session 2: reopen, verify empty
		DiskManager dm2 = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager2 = new Pager(dm2);
		BTree bTree2 = new BTree(pager2);
		Table table2 = new Table(pager2, bTree2);
		table2.open();

		assertThat(table2.getAllData()).isEqualTo(List.of());
	}
}
