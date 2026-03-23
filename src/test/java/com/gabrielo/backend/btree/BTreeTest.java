package com.gabrielo.backend.btree;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.gabrielo.backend.Record;
import com.gabrielo.backend.RecordSerializer;
import com.gabrielo.backend.disk.DiskManager;
import com.gabrielo.backend.pager.Pager;

public class BTreeTest {

  @TempDir
  Path tempDir;

  Path dbFile;

  DiskManager diskManager;

  RecordSerializer serializer;

  Pager pager;

  BTree bTree;

  int PAGE_SIZE = 4096;

  @BeforeEach
  void setup() {
    dbFile = tempDir.resolve("test.ddb");
    diskManager = new DiskManager(dbFile, PAGE_SIZE);
    serializer = new RecordSerializer();
    pager = new Pager(diskManager);
    bTree = new BTree(pager);
  }

  @Test
  void insertsOneRecord() throws IOException {
    Record record = new Record(1, "Broilo", "broilorex@gmail.com");

    bTree.insert(record.id(), serializer.serialize(record));

    assertThat(serializer.deserialize(bTree.search(record.id()))).isEqualTo(record);
  }

  @Test
  void insertsManyRecordsAndFindsThem() throws IOException {
    Record record1 = new Record(1, "Broilo", "broilorex@gmail.com");
    Record record2 = new Record(2, "Gabrielo", "gabrielodon@pescao.com");
    Record record3 = new Record(3, "Brielingson", "brielingson@pescao.com");

    bTree.insert(record1.id(), serializer.serialize(record1));
    bTree.insert(record2.id(), serializer.serialize(record2));
    bTree.insert(record3.id(), serializer.serialize(record3));

    assertThat(serializer.deserialize(bTree.search(record1.id()))).isEqualTo(record1);
    assertThat(serializer.deserialize(bTree.search(record2.id()))).isEqualTo(record2);
    assertThat(serializer.deserialize(bTree.search(record3.id()))).isEqualTo(record3);
  }

  @Test
  void insertsRecordsWithDisorderedIdsAndFindsThem() throws IOException {
    Record record1 = new Record(5, "Broilo", "broilorex@gmail.com");
    Record record2 = new Record(0, "Gabrielo", "gabrielodon@pescao.com");
    Record record3 = new Record(10, "Brielingson", "brielingson@pescao.com");

    bTree.insert(record1.id(), serializer.serialize(record1));
    bTree.insert(record2.id(), serializer.serialize(record2));
    bTree.insert(record3.id(), serializer.serialize(record3));

    assertThat(serializer.deserialize(bTree.search(record1.id()))).isEqualTo(record1);
    assertThat(serializer.deserialize(bTree.search(record2.id()))).isEqualTo(record2);
    assertThat(serializer.deserialize(bTree.search(record3.id()))).isEqualTo(record3);
  }

  @Test
  void insertsMoreThanOneLeafAndFindsAll() throws IOException {
    int totalRecords = 57; // exceeds LEAF_MAX_CELLS (56), forces one split
    Record[] records = new Record[totalRecords];
    for (int i = 0; i < totalRecords; i++) {
      records[i] = new Record(i, "Name" + i, "email" + i + "@mail.com");
      bTree.insert(records[i].id(), serializer.serialize(records[i]));
    }

    for (int i = 0; i < totalRecords; i++) {
      byte[] found = bTree.search(records[i].id());
      assertThat(found).as("record with key %d should be found", i).isNotNull();
      assertThat(serializer.deserialize(found)).isEqualTo(records[i]);
    }
  }

  @Test
  void insertsMultipleLeafsWorthOfRecords() throws IOException {
    int totalRecords = 200; // forces multiple leaf splits
    Record[] records = new Record[totalRecords];
    for (int i = 0; i < totalRecords; i++) {
      records[i] = new Record(i, "Name" + i, "email" + i + "@mail.com");
      bTree.insert(records[i].id(), serializer.serialize(records[i]));
    }

    for (int i = 0; i < totalRecords; i++) {
      byte[] found = bTree.search(records[i].id());
      assertThat(found).as("record with key %d should be found", i).isNotNull();
      assertThat(serializer.deserialize(found)).isEqualTo(records[i]);
    }
  }

  @Test
  void searchReturnsNullForNonExistentKey() throws IOException {
    for (int i = 0; i < 10; i++) {
      Record r = new Record(i, "Name" + i, "email" + i + "@mail.com");
      bTree.insert(r.id(), serializer.serialize(r));
    }

    assertThat(bTree.search(999)).isNull();
  }
}
