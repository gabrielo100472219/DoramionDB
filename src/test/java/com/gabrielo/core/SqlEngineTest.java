package com.gabrielo.core;

import com.gabrielo.storage.Record;
import com.gabrielo.storage.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SqlEngineTest {

	@Test
	void insertsWhenStatementIsInsert() {
		final Table table = new Table();
		final SqlEngine engine = new SqlEngine(table);

		final SqlExecutionResult result = engine.executeStatement("insert");

		assertThat(result.isSuccess()).isTrue();
		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

	@Test
	void getsDataWhenStatementIsSelectSingleRow() {
		final List<Record> testData = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"));
		final Table table = new Table(testData);
		final SqlEngine engine = new SqlEngine(table);

		final SqlExecutionResult result = engine.executeStatement("select");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.queryResult()).isEqualTo(testData);
	}

	@Test
	void getsDataWhenStatementIsSelectMultipleRows() {
		final List<Record> testData = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));
		final Table table = new Table(testData);
		final SqlEngine engine = new SqlEngine(table);

		final SqlExecutionResult result = engine.executeStatement("select");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.queryResult()).isEqualTo(testData);
	}

	@Test
	void handlesUnknownCommandCorrectly() {
		final Table table = new Table();
		final SqlEngine engine = new SqlEngine(table);
		final String invalidStatement = "invalid statement";

		final SqlExecutionResult result = engine.executeStatement(invalidStatement);

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.message()).isEqualTo("Unknown command: " + invalidStatement);
	}

}