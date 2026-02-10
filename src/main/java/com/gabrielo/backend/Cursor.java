package com.gabrielo.backend;

import java.io.IOException;

public class Cursor {

	private final Pager pager;

	private int pageIndex;

	private int recordIndex;

	public Cursor(Pager pager) {
		this.pager = pager;
		this.pageIndex = 0;
		this.recordIndex = 0;
	}

	public boolean hasNext() throws IOException {
		int totalPages = pager.getTotalPagesCreated();
		if (totalPages == 0) {
			return false;
		}

		int currentPageRecordCount = pager.getRecordCountAt(pageIndex);

		if (recordIndex < currentPageRecordCount) {
			return true;
		}

		return pageIndex < totalPages - 1;
	}

	public Record next() throws IOException {
		if (!hasNext()) {
			throw new IllegalStateException("No more records available");
		}
		if (recordIndex >= pager.getRecordCountAt(pageIndex)) {
			pageIndex++;
			recordIndex = 0;
		}
		Record record = pager.getRecordAt(pageIndex, recordIndex);
		recordIndex++;
		return record;
	}
}
