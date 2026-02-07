package com.gabrielo.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pager {

	private static final int MAX_NUMBER_OF_PAGES = 100;

	private final Page[] pages = new Page[MAX_NUMBER_OF_PAGES];

	private final RecordSerializer serializer = new RecordSerializer();

	private final DiskManager diskManager;

	private int totalPagesCreated = -1;

	public Pager(DiskManager diskManager) {
		this.diskManager = diskManager;
	}

	public void insert(Record record) throws IOException {
		initializeTotalPagesCreated();

		if (totalPagesCreated == 0) {
			totalPagesCreated = 1;
		}

		int lastPageId = totalPagesCreated - 1;
		int pageIndex = lastPageId % MAX_NUMBER_OF_PAGES;

		ensurePageAvailable(pageIndex, lastPageId);

		byte[] serializedRecord = serializer.serialize(record);
		if (!pages[pageIndex].insert(serializedRecord)) {
			Page nextPage = createNextPage();
			nextPage.insert(serializedRecord);
		}
	}

	private void initializeTotalPagesCreated() throws IOException {
		if (totalPagesCreated == -1) {
			totalPagesCreated = diskManager.getNumberOfPages();
		}
	}

	private void ensurePageAvailable(int pageIndex, int pageId) throws IOException {
		if (pages[pageIndex] == null) {
			pages[pageIndex] = loadOrCreatePage(pageId);
		}
	}

	private Page loadOrCreatePage(int pageId) throws IOException {
		Page page = diskManager.readPageFromDisk(pageId);
		return page == null ? new Page(pageId, serializer.RECORD_SIZE) : page;
	}

	private Page createNextPage() throws IOException {
		totalPagesCreated++;
		int newPageIndex = (totalPagesCreated - 1) % MAX_NUMBER_OF_PAGES;
		if (pages[newPageIndex] != null) {
			evictPage(newPageIndex);
		}
		pages[newPageIndex] = new Page(totalPagesCreated - 1, serializer.RECORD_SIZE);
		return pages[newPageIndex];
	}

	private void evictPage(int pageIndex) throws IOException {
		if (pages[pageIndex] != null && pages[pageIndex].isDirty()) {
			diskManager.writePageToDisk(pages[pageIndex]);
		}
	}

	public void flushAllPages() throws IOException {
		for (int i = 0; i < MAX_NUMBER_OF_PAGES; i++) {
			if (pages[i] != null && pages[i].isDirty()) {
				diskManager.writePageToDisk(pages[i]);
			}
		}
	}

	public List<Record> getAllRecords() throws IOException {
		initializeTotalPagesCreated();
		List<Record> recordList = new ArrayList<>();
		for (int i = 0; i < totalPagesCreated; i++) {
			int pageIndex = i % MAX_NUMBER_OF_PAGES;
			if (pages[pageIndex] == null || pages[pageIndex].getId() != i) {
				if (pages[pageIndex] != null) {
					evictPage(pageIndex);
				}
				pages[pageIndex] = diskManager.readPageFromDisk(i);
			}
			int recordsInPage = pages[pageIndex].getRecordCount();
			for (int j = 0; j < recordsInPage; j++) {
				recordList.add(serializer.deserialize(pages[pageIndex].getRecordAt(j)));
			}
		}
		return recordList;
	}
}
