package com.gabrielo.backend.btree;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.gabrielo.backend.Record;
import com.gabrielo.backend.RecordSerializer;
import com.gabrielo.backend.disk.DiskManager;
import com.gabrielo.backend.pager.Pager;

public class BTreeTest {

  @TempDir
  Path tempDir;

  int PAGE_SIZE = 4096;

  @Test
  void insertsOneRecord() throws IOException {
    Record record = new Record(1, "Broilo", "broilorex@gmail.com");
    Path dbFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
    Pager pager = new Pager(diskManager);
    BTree bTree = new BTree(pager);
    RecordSerializer serializer = new RecordSerializer();

    bTree.insert(record.id(), serializer.serialize(record));

    assertThat(serializer.deserialize(bTree.search(record.id()))).isEqualTo(record);
  }

  @Test
  void insertsManyRecordsAndFindsThem() throws IOException {
    Record record1 = new Record(1, "Broilo", "broilorex@gmail.com");
    Record record2 = new Record(2, "Gabrielo", "gabrielodon@pescao.com");
    Record record3 = new Record(3, "Brielingson", "brielingson@pescao.com");
    Path dbFile = tempDir.resolve("test3.ddb");
    DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
    Pager pager = new Pager(diskManager);
    BTree bTree = new BTree(pager);
    RecordSerializer serializer = new RecordSerializer();

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
    Path dbFile = tempDir.resolve("test3.ddb");
    DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
    Pager pager = new Pager(diskManager);
    BTree bTree = new BTree(pager);
    RecordSerializer serializer = new RecordSerializer();

    bTree.insert(record1.id(), serializer.serialize(record1));
    bTree.insert(record2.id(), serializer.serialize(record2));
    bTree.insert(record3.id(), serializer.serialize(record3));

    assertThat(serializer.deserialize(bTree.search(record1.id()))).isEqualTo(record1);
    assertThat(serializer.deserialize(bTree.search(record2.id()))).isEqualTo(record2);
    assertThat(serializer.deserialize(bTree.search(record3.id()))).isEqualTo(record3);
  }
}
