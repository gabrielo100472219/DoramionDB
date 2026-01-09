package com.gabrielo.backend;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Pager {

	private final int MAX_NUMBER_OF_PAGES = 100;

	private final int PAGE_SIZE = 4096;

	private final Page[] pages = new Page[MAX_NUMBER_OF_PAGES];

	private final RecordSerializer serializer = new RecordSerializer();

	private final int recordsPerPage = PAGE_SIZE / serializer.RECORD_SIZE;

	private int recordCount = 0;

	public void insert(Record record) {
		byte[] serializedRecord = serializer.serialize(record);
		int pageIndex = recordCount == 0 ? 0 : recordCount / recordsPerPage;
		if (pages[pageIndex] == null) {
			pages[pageIndex] = new Page();
		}
		if (pages[pageIndex].buffer == null) {
			pages[pageIndex].buffer = ByteBuffer.allocate(PAGE_SIZE);
		}
		pages[pageIndex].buffer.put(serializedRecord);
		recordCount++;
	}

	public List<Record> getAllRecords() {
		byte[] rawData = new byte[serializer.RECORD_SIZE];
		List<Record> result = new ArrayList<>();
		for (int i = 0; i < recordCount; i++) {
			int pageIndex = i / recordsPerPage;
			int recordIndex = (i - pageIndex * recordsPerPage) * serializer.RECORD_SIZE;
			pages[pageIndex].buffer.get(recordIndex, rawData, 0, serializer.RECORD_SIZE);
			result.add(serializer.deserialize(rawData));
		}
		return result;
	}
}
