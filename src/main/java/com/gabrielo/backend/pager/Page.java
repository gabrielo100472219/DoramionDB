package com.gabrielo.backend.pager;

import java.nio.ByteBuffer;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Page {

  @Getter
  private final ByteBuffer buffer;

  @Getter
  private final int id;

  @Getter
  private int recordCount;

  @Getter
  private boolean dirty;

  private final int recordSize;

  @Getter
  private final int size = 4096;

  public Page(int id, int recordSize) {
    this.buffer = ByteBuffer.allocate(size);
    this.id = id;
    this.recordCount = 0;
    this.recordSize = recordSize;
    this.dirty = false;
  }

  public void markDirty() {
    this.dirty = true;
  }

  public boolean insert(byte[] recordData) {
    if (isFull()) {
      return false;
    }
    buffer.put(recordData);
    recordCount++;
    dirty = true;
    return true;
  }

  private boolean isFull() {
    return (recordCount + 1) * recordSize > size;
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
