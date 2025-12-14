package com.gabrielo.core;

import com.gabrielo.storage.Record;
import com.gabrielo.storage.Table;

import java.util.List;

public class SqlEngine {

	private final Table table;

	public SqlEngine() {
		this.table = new Table();
	}

	public SqlEngine(Table table) {
		this.table = table;
	}
	public SqlExecutionResult executeStatement(String statement) {
		return switch (statement) {
			case "insert" -> this.executeInsert();
			case "select" -> this.executeSelect();
			default -> new SqlExecutionResult(false, "Unknown command: " + statement, List.of());
		};
	}

	private SqlExecutionResult executeInsert() {
		this.table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		return new SqlExecutionResult(true, "Inserted successfully!", List.of());
	}

	private SqlExecutionResult executeSelect() {
		final List<Record> result = this.table.getAllData();
		return new SqlExecutionResult(true, "Inserted successfully!", result);
	}
}
