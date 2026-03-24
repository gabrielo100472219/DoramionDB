package com.gabrielo.backend.disk;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DiskManagerTest {

  int pageSize = 4096;

  @Test
  @SneakyThrows
  void defaultNumberOfPagesIsZero(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    assertThat(diskManager.getNumberOfPages()).isZero();
  }

  @Test
  @SneakyThrows
  void createsFileIfItDoesntExist(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    assertThat(testFile).doesNotExist();

    diskManager.getNumberOfPages();

    assertThat(testFile).exists();
  }

  @Test
  @SneakyThrows
  void writesAndReadsOneEmptyPage(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);
    byte[] buffer = new byte[pageSize];

    diskManager.writePageToDisk(0, buffer);

    assertThat(diskManager.readPageFromDisk(0)).isEqualTo(buffer);
  }

  @Test
  @SneakyThrows
  void writesAndReadsMultipleEmptyPages(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    for (int i = 0; i < 3; i++) {
      diskManager.writePageToDisk(i, new byte[pageSize]);
    }

    for (int i = 0; i < 3; i++) {
      assertThat(diskManager.readPageFromDisk(i)).isEqualTo(new byte[pageSize]);
    }
  }

  @Test
  @SneakyThrows
  void writesAndReadsPageWithData(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);
    byte[] buffer = new byte[pageSize];
    ByteBuffer.wrap(buffer).putInt(0, 42);
    ByteBuffer.wrap(buffer).putInt(100, 999);

    diskManager.writePageToDisk(0, buffer);

    byte[] loaded = diskManager.readPageFromDisk(0);
    assertThat(ByteBuffer.wrap(loaded).getInt(0)).isEqualTo(42);
    assertThat(ByteBuffer.wrap(loaded).getInt(100)).isEqualTo(999);
  }

  @Test
  @SneakyThrows
  void writesAndReadsMultiplePagesWithData(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    for (int i = 0; i < 3; i++) {
      byte[] buffer = new byte[pageSize];
      ByteBuffer.wrap(buffer).putInt(0, i * 100);
      diskManager.writePageToDisk(i, buffer);
    }

    for (int i = 0; i < 3; i++) {
      byte[] loaded = diskManager.readPageFromDisk(i);
      assertThat(ByteBuffer.wrap(loaded).getInt(0)).isEqualTo(i * 100);
    }
  }

  @Test
  @SneakyThrows
  void updatingPageDoesNotDuplicateIt(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    byte[] buffer = new byte[pageSize];
    ByteBuffer.wrap(buffer).putInt(0, 1);
    diskManager.writePageToDisk(0, buffer);

    ByteBuffer.wrap(buffer).putInt(0, 2);
    diskManager.writePageToDisk(0, buffer);

    assertThat(diskManager.getNumberOfPages()).isEqualTo(1);
    byte[] loaded = diskManager.readPageFromDisk(0);
    assertThat(ByteBuffer.wrap(loaded).getInt(0)).isEqualTo(2);
  }

  @Test
  @SneakyThrows
  void defaultRootPageIdIsMinusOne(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    assertThat(diskManager.readRootPageId()).isEqualTo(-1);
  }

  @Test
  @SneakyThrows
  void writesAndReadsRootPageId(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    diskManager.writeRootPageId(5);

    assertThat(diskManager.readRootPageId()).isEqualTo(5);
  }

  @Test
  @SneakyThrows
  void rootPageIdSurvivesPageWrites(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    diskManager.writeRootPageId(3);

    byte[] buffer = new byte[pageSize];
    ByteBuffer.wrap(buffer).putInt(0, 42);
    diskManager.writePageToDisk(0, buffer);

    assertThat(diskManager.readRootPageId()).isEqualTo(3);
    assertThat(diskManager.getNumberOfPages()).isEqualTo(1);
  }
}
