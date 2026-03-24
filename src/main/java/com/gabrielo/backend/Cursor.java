package com.gabrielo.backend;

import com.gabrielo.backend.btree.BTree;
import com.gabrielo.backend.btree.LeafNode;
import com.gabrielo.backend.pager.Page;
import com.gabrielo.backend.pager.Pager;

import java.io.IOException;

public class Cursor {

  private final Pager pager;

  private final BTree bTree;

  private final RecordSerializer serializer = new RecordSerializer();

  private int currentLeafPageId;

  private int cellIndex;

  private LeafNode currentLeaf;

  public Cursor(Pager pager, BTree bTree) throws IOException {
    this.pager = pager;
    this.bTree = bTree;
    this.currentLeafPageId = bTree.getLeftMostLeafPageId();
    this.cellIndex = 0;
    if (currentLeafPageId != -1) {
      Page page = pager.getPage(currentLeafPageId);
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

    // Current leaf exhausted — check if there's a next sibling
    int nextPageId = currentLeaf.getNextLeafPageId();
    return nextPageId != -1;
  }

  public Record next() throws IOException {
    if (!hasNext()) {
      throw new IllegalStateException("No more records available");
    }

    // Advance to next leaf if current is exhausted
    if (cellIndex >= currentLeaf.getNumCells()) {
      int nextPageId = currentLeaf.getNextLeafPageId();
      Page nextPage = pager.getPage(nextPageId);
      currentLeaf = new LeafNode(nextPage);
      currentLeafPageId = nextPageId;
      cellIndex = 0;
    }

    byte[] recordBytes = currentLeaf.getCell(cellIndex);
    cellIndex++;
    return serializer.deserialize(recordBytes);
  }
}
