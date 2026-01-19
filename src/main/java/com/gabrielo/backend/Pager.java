package com.gabrielo.backend;

import java.util.ArrayList;
import java.util.List;

public class Pager {

	private static final int MAX_NUMBER_OF_PAGES = 100;

	private static final int PAGE_SIZE = 4096;

	private final Page[] pages = new Page[MAX_NUMBER_OF_PAGES];

	private final RecordSerializer serializer = new RecordSerializer();

	private final int recordsPerPage = PAGE_SIZE / serializer.RECORD_SIZE;

	public void insert(Record record) {
		Page currentPage = getLastPage();

		byte[] serializedRecord = serializer.serialize(record);

		if (!currentPage.insert(serializedRecord)) {
			currentPage = createNewPage();
			currentPage.insert(serializedRecord);
		}
	}

	private Page getLastPage() {
		for (int i = MAX_NUMBER_OF_PAGES - 1; i >= 0; i--) {
			if (pages[i] != null) {
				return pages[i];
			}
		}
		return createNewPage();
	}

	private Page createNewPage() {
		for (int i = 0; i < MAX_NUMBER_OF_PAGES; i++) {
			if (pages[i] == null) {
				pages[i] = new Page(serializer.RECORD_SIZE);
				return pages[i];
			}
		}
		throw new IllegalStateException("All pages are full");
	}

	public List<Record> getAllRecords() {
		List<Record> recordList = new ArrayList<>();
		for (int i = 0; i < MAX_NUMBER_OF_PAGES; i++) {
			if (pages[i] == null) {
				break;
			}
			int recordsInPage = pages[i].getRecordCount();
			for (int j = 0; j < recordsInPage; j++) {
				recordList.add(serializer.deserialize(pages[i].getRecordAt(j)));
			}
		}
		return recordList;
	}
}
