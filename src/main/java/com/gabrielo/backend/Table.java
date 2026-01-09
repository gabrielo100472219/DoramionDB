package com.gabrielo.backend;

import java.util.List;

public class Table {

	private final Pager pager = new Pager();

	public List<Record> getAllData() {
		return pager.getAllRecords();
	}

	public void insert(int id, String name, String email) {
		pager.insert(new Record(id, name, email));
	}
}
