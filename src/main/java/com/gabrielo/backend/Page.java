package com.gabrielo.backend;

import lombok.Getter;

import java.nio.ByteBuffer;

public class Page {

	private final ByteBuffer buffer;

	@Getter
	private final int id;

	@Getter
	private int recordCount;

	private final int recordSize;

	private static final int pageSize = 4096;

	public Page(int id, int recordSize) {
		this.buffer = ByteBuffer.allocate(pageSize);
		this.id = id;
		this.recordCount = 0;
		this.recordSize = recordSize;
	}

	public boolean insert(byte[] recordData) {
		if (isFull()) {
			return false;
		}
		buffer.put(recordData);
		recordCount++;
		return true;
	}

	private boolean isFull() {
		return (recordCount + 1) * recordSize > pageSize;
	}

	public byte[] getRecordAt(int recordIndex) {
		if (recordIndex < 0 || recordIndex >= recordCount) {
			throw new IndexOutOfBoundsException(
					"Record index " + recordIndex + " out of bounds for size " + recordCount);
		}
		byte[] resultBuffer = new byte[recordSize];
		int offset = recordIndex * recordSize;
		buffer.get(offset, resultBuffer, 0, recordSize);
		return resultBuffer;
	}
}
