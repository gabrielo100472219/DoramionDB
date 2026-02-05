package com.gabrielo.core;

import com.gabrielo.backend.DiskManager;
import com.gabrielo.backend.Pager;
import com.gabrielo.backend.Record;
import com.gabrielo.backend.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SqlEngineTest {

	private static final int PAGE_SIZE = 4096;

	@TempDir
	Path tempDir;

	private int testCounter = 0;

	private Table createTable() {
		Path dbFile = tempDir.resolve("test" + (testCounter++) + ".ddb");
		DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
		Pager pager = new Pager(diskManager);
		return new Table(pager);
	}

	@Test
	void insertsWhenStatementIsInsert() {
		Table table = createTable();
		SqlEngine engine = new SqlEngine(table);

		SqlExecutionResult result = engine.executeStatement("insert 1 Gabrielo gabrielodon@pescao.com");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.message()).isEqualTo("Inserted successfully.");
		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

	@Test
	void getsDataWhenStatementIsSelectSingleRow() {
		Table table = createTable();
		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		SqlEngine engine = new SqlEngine(table);

		SqlExecutionResult result = engine.executeStatement("select");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.message()).isEqualTo("Queried successfully.");
		assertThat(result.queryResult()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

	@Test
	void getsDataWhenStatementIsSelectMultipleRows() {
		Table table = createTable();
		table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		table.insert(2, "Brielingson", "brielingson@pescao.com");
		table.insert(3, "Gabrielin", "gabrielin@pescao.com");
		SqlEngine engine = new SqlEngine(table);

		SqlExecutionResult result = engine.executeStatement("select");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.message()).isEqualTo("Queried successfully.");
		assertThat(result.queryResult()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com")));
	}

	@Test
	void handlesUnknownCommand() {
		Table table = createTable();
		SqlEngine engine = new SqlEngine(table);
		String invalidStatement = "invalid statement";

		SqlExecutionResult result = engine.executeStatement(invalidStatement);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.message()).isEqualTo("Unknown command: " + invalidStatement);
	}

	@Test
	void handlesEmptyQuery() {
		Table table = createTable();
		SqlEngine engine = new SqlEngine(table);

		SqlExecutionResult result = engine.executeStatement(" ");

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.message()).isEqualTo("Empty query");
	}

	@Test
	void insertHandlesWrongNumberOfArgs() {
		Table table = createTable();
		SqlEngine engine = new SqlEngine(table);
		String statement = "insert bad";

		SqlExecutionResult result = engine.executeStatement(statement);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.message())
				.isEqualTo("Received different number of arguments than expected: expected 3, received 1");
	}
}