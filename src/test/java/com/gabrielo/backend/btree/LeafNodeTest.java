package com.gabrielo.backend.btree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gabrielo.backend.Record;
import com.gabrielo.backend.RecordSerializer;
import com.gabrielo.backend.pager.Page;

public class LeafNodeTest {

  int recordSize = 68;

  RecordSerializer serializer = new RecordSerializer();

  LeafNode leafNode;

  @BeforeEach
  void beforeEach() {
    Page page = new Page(0, recordSize);
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
}
