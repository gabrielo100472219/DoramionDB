package com.gabrielo.backend;

import com.gabrielo.backend.btree.BTree;
import com.gabrielo.backend.pager.Pager;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Table {

	private final Pager pager;

	private final BTree bTree;

	private final RecordSerializer serializer = new RecordSerializer();

	public Table(Pager pager, BTree bTree) {
		this.pager = pager;
		this.bTree = bTree;
	}

	public void open() throws IOException {
		int rootPageId = pager.readRootPageId();
		bTree.setRootPageId(rootPageId);
	}

	public void close() throws IOException {
		pager.writeRootPageId(bTree.getRootPageId());
		pager.flushAllPages();
	}

	@SneakyThrows
	public List<Record> getAllData() {
		Cursor cursor = new Cursor(pager, bTree);
		List<Record> records = new ArrayList<>();
		while (cursor.hasNext()) {
			records.add(cursor.next());
		}
		return records;
	}

	@SneakyThrows
	public void insert(int id, String name, String email) {
		Record record = new Record(id, name, email);
		byte[] serialized = serializer.serialize(record);
		bTree.insert(id, serialized);
	}
}
