package com.gabrielo;

import com.gabrielo.backend.DiskManager;
import com.gabrielo.backend.Pager;
import com.gabrielo.backend.Table;
import com.gabrielo.core.Interface;
import com.gabrielo.core.SqlEngine;

import java.nio.file.Path;

public class Main {

	private static final int PAGE_SIZE = 4096;
	private static final Path DB_PATH = Path.of("data/database.ddb");

	public static void main(String[] args) {
		DiskManager diskManager = new DiskManager(DB_PATH, PAGE_SIZE);
		Pager pager = new Pager(diskManager);
		Table table = new Table(pager);
		SqlEngine engine = new SqlEngine(table);
		new Interface(engine).runDatabaseEngine();
	}
}