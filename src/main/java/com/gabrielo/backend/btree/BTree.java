package com.gabrielo.backend.btree;

import com.gabrielo.backend.pager.Page;
import com.gabrielo.backend.pager.Pager;

import java.io.IOException;

public class BTree {

  private final Pager pager;

  private int rootPageId = 0;

  private boolean rootExists = false;

  public BTree(Pager pager) {
    this.pager = pager;
  }

  public void insert(int key, byte[] record) throws IOException {
    if (!rootExists) {
      Page root = pager.allocateNewPage();
      rootPageId = root.getId();
      rootExists = true;
      LeafNode leafNode = new LeafNode(root);
      leafNode.initialize();
    }
    Page root = pager.getPage(rootPageId);
    LeafNode leafNode = new LeafNode(root);
    leafNode.insert(key, record);
    root.markDirty();
  }

  public byte[] search(int key) throws IOException {
    if (!rootExists) {
      return null;
    }
    Page page = pager.getPage(rootPageId);
    LeafNode leafNode = new LeafNode(page);
    int cellIndex = leafNode.findCell(key);
    if (cellIndex == -1) {
      return null;
    }
    return leafNode.getCell(cellIndex);
  }
}
