package com.gabrielo.core;

import com.gabrielo.storage.Table;

public class SqlEngine {

	private final Table table;

	public SqlEngine() {
		this.table = new Table();
	}

	public SqlEngine(Table table) {
		this.table = table;
	}
	public SqlExecutionResult executeStatement(String statement) {
		this.table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		return new SqlExecutionResult(true, "Inserted successfully!");
	}
}
