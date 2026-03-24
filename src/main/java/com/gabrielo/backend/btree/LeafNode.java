package com.gabrielo.backend.btree;

import java.nio.ByteBuffer;

import com.gabrielo.backend.pager.Page;

public final class LeafNode implements Node {

  private final Page page;

  public LeafNode(Page page) {
    this.page = page;
  }

  public void initialize() {
    ByteBuffer buffer = page.getBuffer();
    buffer.put(NodeLayout.NODE_TYPE_OFFSET, NodeLayout.NODE_TYPE_LEAF);
    buffer.put(NodeLayout.IS_ROOT_OFFSET, (byte) 0);
    buffer.putInt(NodeLayout.PARENT_PAGE_ID_OFFSET, 0);
    buffer.putInt(NodeLayout.NUM_CELLS_OFFSET, 0);
    buffer.putInt(NodeLayout.NEXT_LEAF_PAGE_ID_OFFSET, -1);
  }

  public int getPageId() {
    return page.getId();
  }

  public int getParentPageId() {
    return page.getBuffer().getInt(NodeLayout.PARENT_PAGE_ID_OFFSET);
  }

  public void setParentPageId(int pageId) {
    page.getBuffer().putInt(NodeLayout.PARENT_PAGE_ID_OFFSET, pageId);
  }

  public int getNextLeafPageId() {
    return page.getBuffer().getInt(NodeLayout.NEXT_LEAF_PAGE_ID_OFFSET);
  }

  public void setNextLeafPageId(int pageId) {
    page.getBuffer().putInt(NodeLayout.NEXT_LEAF_PAGE_ID_OFFSET, pageId);
  }

  public int getNumCells() {
    return page.getBuffer().getInt(NodeLayout.NUM_CELLS_OFFSET);
  }

  public void insert(int key, byte[] record) {
    ByteBuffer buffer = page.getBuffer();
    int numCells = getNumCells();
    int insertIndex = findInsertPosition(key);
    shiftCellsToTheRight(buffer, numCells, insertIndex);
    int cellOffset = NodeLayout.LEAF_HEADER_SIZE + (insertIndex * NodeLayout.CELL_SIZE);
    buffer.putInt(cellOffset, key);
    buffer.put(cellOffset + NodeLayout.CELL_KEY_SIZE, record);
    buffer.putInt(NodeLayout.NUM_CELLS_OFFSET, numCells + 1);
  }

  private void shiftCellsToTheRight(ByteBuffer buffer, int numCells, int insertIndex) {
    for (int i = numCells - 1; i >= insertIndex; i--) {
      int srcOffset = NodeLayout.LEAF_HEADER_SIZE + (i * NodeLayout.CELL_SIZE);
      int destOffset = srcOffset + NodeLayout.CELL_SIZE;
      byte[] cell = new byte[NodeLayout.CELL_SIZE];
      buffer.get(srcOffset, cell);
      buffer.put(destOffset, cell);
    }
  }

  public byte[] getCell(int cellIndex) {
    ByteBuffer buffer = page.getBuffer();
    int cellOffset = NodeLayout.LEAF_HEADER_SIZE + (cellIndex * NodeLayout.CELL_SIZE);
    int recordOffset = cellOffset + NodeLayout.CELL_KEY_SIZE;
    byte[] record = new byte[NodeLayout.RECORD_SIZE];
    buffer.get(recordOffset, record);
    return record;
  }

  public int getKey(int cellIndex) {
    ByteBuffer buffer = page.getBuffer();
    int cellOffset = NodeLayout.LEAF_HEADER_SIZE + (cellIndex * NodeLayout.CELL_SIZE);
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
    int numberOfCells = getNumCells();
    int numberOfCellsFittingNode = (page.getSize() - NodeLayout.LEAF_HEADER_SIZE) / NodeLayout.CELL_SIZE;
    return numberOfCells >= numberOfCellsFittingNode;
  }

  /**
   * Split this leaf node, moving the upper half of cells to the newPage.
   * Updates the sibling linked list (nextLeafPageId).
   * Returns the first key of the new right leaf (the split key to promote).
   */
  public int split(Page newPage) {
    LeafNode rightNode = new LeafNode(newPage);
    rightNode.initialize();

    int totalCells = getNumCells();
    int splitIndex = totalCells / 2;

    ByteBuffer srcBuffer = page.getBuffer();
    ByteBuffer destBuffer = newPage.getBuffer();

    // Copy upper half cells to the new leaf
    for (int i = splitIndex; i < totalCells; i++) {
      int srcOffset = NodeLayout.LEAF_HEADER_SIZE + (i * NodeLayout.CELL_SIZE);
      int destIndex = i - splitIndex;
      int destOffset = NodeLayout.LEAF_HEADER_SIZE + (destIndex * NodeLayout.CELL_SIZE);
      byte[] cell = new byte[NodeLayout.CELL_SIZE];
      srcBuffer.get(srcOffset, cell);
      destBuffer.put(destOffset, cell);
    }

    int rightCells = totalCells - splitIndex;
    rightNode.setNumCells(rightCells);

    // Update sibling linked list: newRight.next = this.next; this.next = newRight
    rightNode.setNextLeafPageId(getNextLeafPageId());
    setNextLeafPageId(newPage.getId());

    // Shrink this leaf to only keep the lower half
    setNumCells(splitIndex);

    newPage.markDirty();
    page.markDirty();

    // Return the split key (first key of the right leaf)
    return rightNode.getKey(0);
  }

  private void setNumCells(int numCells) {
    page.getBuffer().putInt(NodeLayout.NUM_CELLS_OFFSET, numCells);
  }
}
