package com.gabrielo.backend.btree;

import java.nio.ByteBuffer;

import com.gabrielo.backend.pager.Page;

public class InternalNode {

  private final Page page;

  private static final int NUM_KEYS_OFFSET = NodeLayout.COMMON_HEADER_SIZE;

  private static final int RIGHT_CHILD_OFFSET = NUM_KEYS_OFFSET + NodeLayout.NUM_KEYS_SIZE;

  public InternalNode(Page page) {
    this.page = page;
  }

  public void initialize() {
    ByteBuffer buffer = page.getBuffer();
    buffer.put(0, NodeLayout.NODE_TYPE_INTERNAL);
    buffer.put(1, (byte) 0); // isRoot: false
    buffer.putInt(2, 0); // parentPageId
    buffer.putInt(NUM_KEYS_OFFSET, 0); // numKeys
    buffer.putInt(RIGHT_CHILD_OFFSET, -1); // rightChildPageId
  }

  public int getNumKeys() {
    return page.getBuffer().getInt(NUM_KEYS_OFFSET);
  }

  private void setNumKeys(int numKeys) {
    page.getBuffer().putInt(NUM_KEYS_OFFSET, numKeys);
  }

  public int getRightChildPageId() {
    return page.getBuffer().getInt(RIGHT_CHILD_OFFSET);
  }

  public void setRightChildPageId(int pageId) {
    page.getBuffer().putInt(RIGHT_CHILD_OFFSET, pageId);
  }

  public int getParentPageId() {
    return page.getBuffer().getInt(2);
  }

  public void setParentPageId(int pageId) {
    page.getBuffer().putInt(2, pageId);
  }

  private int entryOffset(int index) {
    return NodeLayout.INTERNAL_HEADER_SIZE + (index * NodeLayout.INTERNAL_CHILD_SIZE);
  }

  public int getKey(int index) {
    return page.getBuffer().getInt(entryOffset(index) + NodeLayout.CHILD_PAGE_ID_SIZE);
  }

  private void setKey(int index, int key) {
    page.getBuffer().putInt(entryOffset(index) + NodeLayout.CHILD_PAGE_ID_SIZE, key);
  }

  public int getChildPageId(int index) {
    return page.getBuffer().getInt(entryOffset(index));
  }

  private void setChildPageId(int index, int pageId) {
    page.getBuffer().putInt(entryOffset(index), pageId);
  }

  /**
   * Insert a key with its left and right child page IDs.
   * The leftChildPageId goes into the entry at the insert position,
   * and the rightChildPageId becomes the right child of the next entry
   * or the node's rightChild if inserted at the end.
   */
  public void insert(int key, int leftChildPageId, int rightChildPageId) {
    int numKeys = getNumKeys();
    int insertIndex = findInsertPosition(key);

    shiftEntriesRight(numKeys, insertIndex);

    setChildPageId(insertIndex, leftChildPageId);
    setKey(insertIndex, key);

    if (insertIndex == numKeys) {
      setRightChildPageId(rightChildPageId);
    } else {
      setChildPageId(insertIndex + 1, rightChildPageId);
    }

    setNumKeys(numKeys + 1);
  }

  private void shiftEntriesRight(int numKeys, int insertIndex) {
    ByteBuffer buffer = page.getBuffer();
    for (int i = numKeys - 1; i >= insertIndex; i--) {
      int srcOffset = entryOffset(i);
      int destOffset = entryOffset(i + 1);
      byte[] entry = new byte[NodeLayout.INTERNAL_CHILD_SIZE];
      buffer.get(srcOffset, entry);
      buffer.put(destOffset, entry);
    }
  }

  /**
   * Find which child page to follow for the given search key.
   * Returns the page ID of the child that could contain the key.
   *
   * Layout: child0 | key0 | child1 | key1 | ... | keyN-1 | rightChild
   * Keys at index i separate child[i] (left) from child[i+1] (right).
   * For search key k: go left of the first key > k, or rightChild if none.
   */
  public int findChild(int key) {
    int numKeys = getNumKeys();

    // Binary search for the first key strictly greater than search key
    int low = 0;
    int high = numKeys;
    while (low < high) {
      int mid = (low + high) / 2;
      if (getKey(mid) <= key) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }

    // low = index of first key > search key
    if (low == numKeys) {
      return getRightChildPageId();
    }
    return getChildPageId(low);
  }

  private int findInsertPosition(int key) {
    int low = 0;
    int high = getNumKeys();
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
    return getNumKeys() >= NodeLayout.INTERNAL_MAX_KEYS;
  }

  /**
   * Directly place a key and its left child at the given index.
   * Used during internal node splitting where entries are copied
   * sequentially without shifting.
   */
  public void insertEntry(int index, int key, int childPageId) {
    setKey(index, key);
    setChildPageId(index, childPageId);
  }

  /**
   * Directly set the number of keys. Used during splitting
   * to shrink a node without clearing data.
   */
  public void setNumKeysDirectly(int numKeys) {
    setNumKeys(numKeys);
  }
}
