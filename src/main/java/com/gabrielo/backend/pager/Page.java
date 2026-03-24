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
  private boolean dirty;

  @Getter
  private final int size = 4096;

  public Page(int id) {
    this.buffer = ByteBuffer.allocate(size);
    this.id = id;
    this.dirty = false;
  }

  public void markDirty() {
    this.dirty = true;
  }
}
