package com.gabrielo.backend.btree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gabrielo.backend.Record;
import com.gabrielo.backend.RecordSerializer;
import com.gabrielo.backend.pager.Page;

public class LeafNodeTest {

  RecordSerializer serializer = new RecordSerializer();

  LeafNode leafNode;

  @BeforeEach
  void beforeEach() {
    Page page = new Page(0);
    leafNode = new LeafNode(page);
    leafNode.initialize();
  }

  @Test
  void getNumCellsIsZeroWhenNoInserts() {
    assertThat(leafNode.getNumCells()).isZero();
  }

  @Test
  void insertOnEmptyLeafOccupiesFirstCell() {
    Record record = new Record(0, "Brielo", "bronson@gmail.com");

    leafNode.insert(record.id(), serializer.serialize(record));

    assertThat(serializer.deserialize(leafNode.getCell(0))).isEqualTo(record);
  }

  @Test
  void insertsMaintainSortedOrder() {
    Record record3 = new Record(3, "Charlie", "charlie@mail.com");
    Record record1 = new Record(1, "Alice", "alice@mail.com");
    Record record2 = new Record(2, "Bob", "bob@mail.com");

    leafNode.insert(record3.id(), serializer.serialize(record3));
    leafNode.insert(record1.id(), serializer.serialize(record1));
    leafNode.insert(record2.id(), serializer.serialize(record2));

    assertThat(leafNode.getKey(0)).isEqualTo(1);
    assertThat(leafNode.getKey(1)).isEqualTo(2);
    assertThat(leafNode.getKey(2)).isEqualTo(3);
    assertThat(serializer.deserialize(leafNode.getCell(0))).isEqualTo(record1);
    assertThat(serializer.deserialize(leafNode.getCell(1))).isEqualTo(record2);
    assertThat(serializer.deserialize(leafNode.getCell(2))).isEqualTo(record3);
  }

  @Test
  void findCellReturnsCellIndexByKey() {
    Record record3 = new Record(3, "Charlie", "charlie@mail.com");
    Record record1 = new Record(1, "Alice", "alice@mail.com");
    Record record2 = new Record(2, "Bob", "bob@mail.com");

    leafNode.insert(record3.id(), serializer.serialize(record3));
    leafNode.insert(record1.id(), serializer.serialize(record1));
    leafNode.insert(record2.id(), serializer.serialize(record2));

    assertThat(leafNode.findCell(1)).isEqualTo(0);
    assertThat(leafNode.findCell(2)).isEqualTo(1);
    assertThat(leafNode.findCell(3)).isEqualTo(2);
  }

  @Test
  void findCellReturnsNegativeOneWhenKeyNotFound() {
    Record record = new Record(5, "Eve", "eve@mail.com");
    leafNode.insert(record.id(), serializer.serialize(record));

    assertThat(leafNode.findCell(99)).isEqualTo(-1);
  }

  @Test
  void isFullReturnsFalseWhenEmpty() {
    assertThat(leafNode.isFull()).isFalse();
  }

  @Test
  void isFullReturnsFalseWhenPartiallyFilled() {
    for (int i = 1; i <= 3; i++) {
      Record r = new Record(i, "Name" + i, "email" + i + "@mail.com");
      leafNode.insert(r.id(), serializer.serialize(r));
    }

    assertThat(leafNode.isFull()).isFalse();
  }

  @Test
  void isFullReturnsTrueWhenMaxCellsReached() {
    for (int i = 1; i <= NodeLayout.LEAF_MAX_CELLS; i++) {
      Record r = new Record(i, "Name" + i, "email" + i + "@mail.com");
      leafNode.insert(r.id(), serializer.serialize(r));
    }

    assertThat(leafNode.getNumCells()).isEqualTo(56);
    assertThat(leafNode.isFull()).isTrue();
  }

  @Test
  void splitDividesCellsEvenly() {
    for (int i = 1; i <= 56; i++) {
      Record r = new Record(i, "Name" + i, "email" + i + "@mail.com");
      leafNode.insert(r.id(), serializer.serialize(r));
    }

    Page rightPage = new Page(1);
    int splitKey = leafNode.split(rightPage);
    LeafNode rightNode = new LeafNode(rightPage);

    assertThat(leafNode.getNumCells()).isEqualTo(28);
    assertThat(rightNode.getNumCells()).isEqualTo(28);
    assertThat(splitKey).isEqualTo(29);
  }

  @Test
  void splitPreservesAllRecords() {
    for (int i = 1; i <= 56; i++) {
      Record r = new Record(i, "Name" + i, "email" + i + "@mail.com");
      leafNode.insert(r.id(), serializer.serialize(r));
    }

    Page rightPage = new Page(1);
    leafNode.split(rightPage);
    LeafNode rightNode = new LeafNode(rightPage);

    // Left leaf has keys 1..28
    for (int i = 0; i < 28; i++) {
      assertThat(leafNode.getKey(i)).isEqualTo(i + 1);
    }
    // Right leaf has keys 29..56
    for (int i = 0; i < 28; i++) {
      assertThat(rightNode.getKey(i)).isEqualTo(i + 29);
    }
  }

  @Test
  void splitUpdatesSiblingLinkedList() {
    for (int i = 1; i <= 56; i++) {
      Record r = new Record(i, "Name" + i, "email" + i + "@mail.com");
      leafNode.insert(r.id(), serializer.serialize(r));
    }

    Page rightPage = new Page(1);
    leafNode.split(rightPage);
    LeafNode rightNode = new LeafNode(rightPage);

    // Left's next points to right
    assertThat(leafNode.getNextLeafPageId()).isEqualTo(1);
    // Right's next is -1 (no further sibling)
    assertThat(rightNode.getNextLeafPageId()).isEqualTo(-1);
  }
}
