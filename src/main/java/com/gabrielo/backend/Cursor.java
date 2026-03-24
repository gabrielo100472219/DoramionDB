package com.gabrielo.backend;

import com.gabrielo.backend.btree.BTree;
import com.gabrielo.backend.btree.BTreeIterator;
import com.gabrielo.backend.pager.Pager;

import java.io.IOException;

public class Cursor {

  private final BTreeIterator iterator;

  private final RecordSerializer serializer = new RecordSerializer();

  public Cursor(Pager pager, BTree bTree) throws IOException {
    this.iterator = new BTreeIterator(pager, bTree);
  }

  public boolean hasNext() throws IOException {
    return iterator.hasNext();
  }

  public Record next() throws IOException {
    byte[] recordBytes = iterator.next();
    return serializer.deserialize(recordBytes);
  }
}
