package com.gabrielo.backend;

import lombok.SneakyThrows;

import java.util.List;

public class Table {

	private final Pager pager;

	public Table(Pager pager) {
		this.pager = pager;
	}

	@SneakyThrows
	public List<Record> getAllData() {
		return pager.getAllRecords();
	}

	@SneakyThrows
	public void insert(int id, String name, String email) {
		pager.insert(new Record(id, name, email));
	}
}
