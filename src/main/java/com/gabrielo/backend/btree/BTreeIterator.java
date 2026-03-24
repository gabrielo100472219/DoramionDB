package com.gabrielo.backend.btree;

import com.gabrielo.backend.pager.Page;
import com.gabrielo.backend.pager.Pager;

import java.io.IOException;

public class BTreeIterator {

  private final Pager pager;

  private LeafNode currentLeaf;

  private int cellIndex;

  public BTreeIterator(Pager pager, BTree bTree) throws IOException {
    this.pager = pager;
    int leftMostPageId = bTree.getLeftMostLeafPageId();
    this.cellIndex = 0;
    if (leftMostPageId != -1) {
      Page page = pager.getPage(leftMostPageId);
      this.currentLeaf = new LeafNode(page);
    }
  }

  public boolean hasNext() throws IOException {
    if (currentLeaf == null) {
      return false;
    }

    if (cellIndex < currentLeaf.getNumCells()) {
      return true;
    }

    int nextPageId = currentLeaf.getNextLeafPageId();
    return nextPageId != -1;
  }

  public byte[] next() throws IOException {
    if (!hasNext()) {
      throw new IllegalStateException("No more records available");
    }

    if (cellIndex >= currentLeaf.getNumCells()) {
      int nextPageId = currentLeaf.getNextLeafPageId();
      Page nextPage = pager.getPage(nextPageId);
      currentLeaf = new LeafNode(nextPage);
      cellIndex = 0;
    }

    byte[] record = currentLeaf.getCell(cellIndex);
    cellIndex++;
    return record;
  }
}
