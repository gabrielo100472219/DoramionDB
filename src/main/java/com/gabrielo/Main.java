package com.gabrielo;

import com.gabrielo.backend.disk.DiskManager;
import com.gabrielo.backend.pager.Pager;
import com.gabrielo.backend.btree.BTree;
import com.gabrielo.backend.Table;
import com.gabrielo.core.Interface;
import com.gabrielo.core.SqlEngine;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

	private static final int PAGE_SIZE = 4096;
	private static final Path DB_PATH = Path.of("data/database.ddb");

	public static void main(String[] args) throws IOException {
		DiskManager diskManager = new DiskManager(DB_PATH, PAGE_SIZE);
		Pager pager = new Pager(diskManager);
		BTree bTree = new BTree(pager);
		Table table = new Table(pager, bTree);
		table.open();
		SqlEngine engine = new SqlEngine(table);
		new Interface(engine, () -> {
			try {
				table.close();
			} catch (IOException e) {
				System.err.println("Error closing database: " + e.getMessage());
			}
		}).runDatabaseEngine();
	}
}
