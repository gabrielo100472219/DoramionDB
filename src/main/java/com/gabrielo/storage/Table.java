package com.gabrielo.storage;

import java.util.List;

public class Table {

	private List<Record> data;

	public Table(List<Record> data) {
		this.data = data;
	}

	public List<Record> getAllData() {
		return List.copyOf(this.data);
	}

	public void insert(int id, String name, String email) {
		this.data.add(new Record(id, name, email));
	}
}
