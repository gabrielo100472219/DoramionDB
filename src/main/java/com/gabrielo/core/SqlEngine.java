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
		final String[] tokens = statement.split(" ");
		if (tokens.length == 0) {
			return new SqlExecutionResult(false, "Empty query", List.of());
		}

		return switch (tokens[0]) {
			case "insert" -> this.executeInsert(tokens);
			case "select" -> this.executeSelect();
			default -> new SqlExecutionResult(false, "Unknown command: " + statement, List.of());
		};
	}

	private SqlExecutionResult executeInsert(String[] tokens) {
		if (tokens.length != 4) {
			return new SqlExecutionResult(false,
					"Received different number of arguments than expected: expected 3, received " + (tokens.length - 1),
					List.of());
		}
		this.table.insert(1, "Gabrielo", "gabrielodon@pescao.com");
		return new SqlExecutionResult(true, "Inserted successfully.", List.of());
	}

	private SqlExecutionResult executeSelect() {
		final List<Record> result = this.table.getAllData();
		return new SqlExecutionResult(true, "Queried successfully.", result);
	}
}
