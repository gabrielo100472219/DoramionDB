package com.gabrielo.backend.btree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gabrielo.backend.pager.Page;

public class InternalNodeTest {

  int recordSize = 68;

  InternalNode internalNode;

  @BeforeEach
  void beforeEach() {
    Page page = new Page(0, recordSize);
    internalNode = new InternalNode(page);
    internalNode.initialize();
  }

  @Test
  void numKeysIsZeroAfterInitialization() {
    assertThat(internalNode.getNumKeys()).isZero();
  }

  @Test
  void rightChildIsNegativeOneAfterInitialization() {
    assertThat(internalNode.getRightChildPageId()).isEqualTo(-1);
  }

  @Test
  void insertsSingleKeyWithChildren() {
    // Tree: [child0=1] key=10 [rightChild=2]
    internalNode.insert(10, 1, 2);

    assertThat(internalNode.getNumKeys()).isEqualTo(1);
    assertThat(internalNode.getKey(0)).isEqualTo(10);
    assertThat(internalNode.getChildPageId(0)).isEqualTo(1);
    assertThat(internalNode.getRightChildPageId()).isEqualTo(2);
  }

  @Test
  void insertsMultipleKeysInSortedOrder() {
    // Insert keys out of order: 20, 10, 30
    // Expected layout: [child0] 10 [child1] 20 [child2] 30 [rightChild]
    internalNode.insert(20, 1, 2);
    internalNode.insert(10, 0, 1);
    internalNode.insert(30, 2, 3);

    assertThat(internalNode.getNumKeys()).isEqualTo(3);
    assertThat(internalNode.getKey(0)).isEqualTo(10);
    assertThat(internalNode.getKey(1)).isEqualTo(20);
    assertThat(internalNode.getKey(2)).isEqualTo(30);
    assertThat(internalNode.getChildPageId(0)).isEqualTo(0);
    assertThat(internalNode.getChildPageId(1)).isEqualTo(1);
    assertThat(internalNode.getChildPageId(2)).isEqualTo(2);
    assertThat(internalNode.getRightChildPageId()).isEqualTo(3);
  }

  @Test
  void findChildReturnsLeftChildForKeyLessThanFirstKey() {
    // [child0=1] 10 [child1=2] 20 [rightChild=3]
    internalNode.insert(10, 1, 2);
    internalNode.insert(20, 2, 3);

    assertThat(internalNode.findChild(5)).isEqualTo(1);
  }

  @Test
  void findChildReturnsMiddleChildForKeyBetweenKeys() {
    // [child0=1] 10 [child1=2] 20 [rightChild=3]
    internalNode.insert(10, 1, 2);
    internalNode.insert(20, 2, 3);

    assertThat(internalNode.findChild(15)).isEqualTo(2);
  }

  @Test
  void findChildReturnsRightChildForKeyGreaterThanAllKeys() {
    // [child0=1] 10 [child1=2] 20 [rightChild=3]
    internalNode.insert(10, 1, 2);
    internalNode.insert(20, 2, 3);

    assertThat(internalNode.findChild(25)).isEqualTo(3);
  }

  @Test
  void findChildReturnsCorrectChildForKeyEqualToExistingKey() {
    // [child0=1] 10 [child1=2] 20 [rightChild=3]
    // key == 10 means key <= 10, so it should go right of key 10 -> child1=2
    internalNode.insert(10, 1, 2);
    internalNode.insert(20, 2, 3);

    assertThat(internalNode.findChild(10)).isEqualTo(2);
    assertThat(internalNode.findChild(20)).isEqualTo(3);
  }

  @Test
  void isFullReturnsFalseWhenEmpty() {
    assertThat(internalNode.isFull()).isFalse();
  }
}
