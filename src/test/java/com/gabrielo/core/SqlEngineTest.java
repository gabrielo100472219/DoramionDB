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
	void getsDataWhenStatementIsSelect() {
		final Table table = new Table();
		final SqlEngine engine = new SqlEngine(table);

		engine.executeStatement("select");

		assertThat(table.getAllData()).isEqualTo(List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com")));
	}

}