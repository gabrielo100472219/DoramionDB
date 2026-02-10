package com.gabrielo.backend;

import java.io.IOException;

import static com.gabrielo.backend.Schema.RECORD_SIZE;

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
			createNextPage();
		}

		int lastPageId = totalPagesCreated - 1;
		int pageIndex = lastPageId % MAX_NUMBER_OF_PAGES;

		if (pages[pageIndex] == null) {
			pages[pageIndex] = diskManager.readPageFromDisk(lastPageId);
		}

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

	private Page createNextPage() throws IOException {
		totalPagesCreated++;
		int newPageIndex = (totalPagesCreated - 1) % MAX_NUMBER_OF_PAGES;
		if (pages[newPageIndex] != null) {
			evictPage(newPageIndex);
		}
		pages[newPageIndex] = new Page(totalPagesCreated - 1, RECORD_SIZE);
		return pages[newPageIndex];
	}

	private void evictPage(int pageIndex) throws IOException {
		if (pages[pageIndex] != null && pages[pageIndex].isDirty()) {
			diskManager.writePageToDisk(pages[pageIndex]);
		}
	}

	public int getTotalPagesCreated() throws IOException {
		initializeTotalPagesCreated();
		return totalPagesCreated;
	}

	public int getRecordCountAt(int pageId) throws IOException {
		int pageIndex = pageId % MAX_NUMBER_OF_PAGES;
		ensurePageAvailable(pageIndex, pageId);
		return pages[pageIndex].getRecordCount();
	}

	public Record getRecordAt(int pageId, int recordIndex) throws IOException {
		int pageIndex = pageId % MAX_NUMBER_OF_PAGES;
		ensurePageAvailable(pageIndex, pageId);
		return serializer.deserialize(pages[pageIndex].getRecordAt(recordIndex));
	}

	public void flushAllPages() throws IOException {
		for (int i = 0; i < MAX_NUMBER_OF_PAGES; i++) {
			if (pages[i] != null && pages[i].isDirty()) {
				diskManager.writePageToDisk(pages[i]);
			}
		}
	}

	private void ensurePageAvailable(int pageIndex, int i) throws IOException {
		if (pages[pageIndex] == null) {
			pages[pageIndex] = diskManager.readPageFromDisk(i);
		}

		if (pages[pageIndex].getId() != i) {
			evictPage(pageIndex);
			pages[pageIndex] = diskManager.readPageFromDisk(i);
		}
	}
}
