package com.gabrielo.backend.btree;

import java.nio.ByteBuffer;

import com.gabrielo.backend.pager.Page;

public class LeafNode {

  private final Page page;

  private static final int RECORD_SIZE = 68;

  private static final int CELL_ID_SIZE = 4;

  private static final int CELL_SIZE = CELL_ID_SIZE + RECORD_SIZE;

  private static final int LEAF_HEADER_SIZE = NodeLayout.COMMON_HEADER_SIZE + NodeLayout.NUM_CELLS_SIZE
      + NodeLayout.NEXT_LEAF_PAGE_ID_SIZE;

  public LeafNode(Page page) {
    this.page = page;
  }

  public void initialize() {
    ByteBuffer buffer = page.getBuffer();
    buffer.put(0, (byte) 0); // nodeType: 0 = leaf
    buffer.put(1, (byte) 0); // isRoot: 0 = false
    buffer.putInt(2, 0); // parentPageId: 0
    buffer.putInt(6, 0); // numCells: 0
    buffer.putInt(10, -1); // nextLeafPageId: -1 (no sibling)
  }

  public int getNumCells() {
    return page.getBuffer().getInt(NodeLayout.COMMON_HEADER_SIZE);
  }

  public void insert(int key, byte[] record) {
    ByteBuffer buffer = page.getBuffer();
    int numCells = getNumCells();
    int insertIndex = findInsertPosition(key);
    shiftCellsToTheRight(buffer, numCells, insertIndex);
    int cellOffset = LEAF_HEADER_SIZE + (insertIndex * CELL_SIZE);
    buffer.putInt(cellOffset, key);
    buffer.put(cellOffset + CELL_ID_SIZE, record);
    buffer.putInt(NodeLayout.COMMON_HEADER_SIZE, numCells + 1);
  }

  private void shiftCellsToTheRight(ByteBuffer buffer, int numCells, int insertIndex) {
    for (int i = numCells - 1; i >= insertIndex; i--) {
      int srcOffset = LEAF_HEADER_SIZE + (i * CELL_SIZE);
      int destOffset = srcOffset + CELL_SIZE;
      byte[] cell = new byte[CELL_SIZE];
      buffer.get(srcOffset, cell);
      buffer.put(destOffset, cell);
    }
  }

  public byte[] getCell(int cellIndex) {
    ByteBuffer buffer = page.getBuffer();
    int cellOffset = LEAF_HEADER_SIZE + (cellIndex * CELL_SIZE);
    int recordOffset = cellOffset + CELL_ID_SIZE;
    byte[] record = new byte[RECORD_SIZE];
    buffer.get(recordOffset, record);
    return record;
  }

  public int getKey(int cellIndex) {
    ByteBuffer buffer = page.getBuffer();
    int cellOffset = LEAF_HEADER_SIZE + (cellIndex * CELL_SIZE);
    return buffer.getInt(cellOffset);
  }

  public int findCell(int key) {
    int low = 0;
    int high = getNumCells() - 1;
    while (low <= high) {
      int mid = (low + high) / 2;
      int midKey = getKey(mid);
      if (midKey == key) {
        return mid;
      } else if (midKey < key) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -1;
  }

  private int findInsertPosition(int key) {
    int low = 0;
    int high = getNumCells();
    while (low < high) {
      int mid = (low + high) / 2;
      if (getKey(mid) < key) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    return low;
  }

  public boolean isFull() {
    int numberOfCells = page.getBuffer().getInt(6);
    int numberOfCellsFittingNode = (page.getSize() - LEAF_HEADER_SIZE) / CELL_SIZE;
    return numberOfCells >= numberOfCellsFittingNode;
  }
}
