package com.gabrielo.backend.disk;

import com.gabrielo.backend.pager.Page;
import lombok.SneakyThrows;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

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
    Page page = new Page(0);

    diskManager.writePageToDisk(page);

    assertThat(diskManager.readPageFromDisk(0))
        .usingRecursiveComparison(getPageComparisonConfig())
        .isEqualTo(page);
  }

  @Test
  @SneakyThrows
  void writesAndReadsMultipleEmptyPages(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);
    List<Page> pages = List.of(new Page(0), new Page(1), new Page(2));

    for (Page page : pages) {
      diskManager.writePageToDisk(page);
    }

    for (int i = 0; i < pages.size(); i++) {
      assertThat(diskManager.readPageFromDisk(i))
          .usingRecursiveComparison(getPageComparisonConfig())
          .isEqualTo(pages.get(i));
    }
  }

  @Test
  @SneakyThrows
  void writesAndReadsPageWithData(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);
    Page page = new Page(0);
    page.getBuffer().putInt(0, 42);
    page.getBuffer().putInt(100, 999);

    diskManager.writePageToDisk(page);

    Page loaded = diskManager.readPageFromDisk(0);
    assertThat(loaded.getBuffer().getInt(0)).isEqualTo(42);
    assertThat(loaded.getBuffer().getInt(100)).isEqualTo(999);
  }

  @Test
  @SneakyThrows
  void writesAndReadsMultiplePagesWithData(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    for (int i = 0; i < 3; i++) {
      Page page = new Page(i);
      page.getBuffer().putInt(0, i * 100);
      diskManager.writePageToDisk(page);
    }

    for (int i = 0; i < 3; i++) {
      Page loaded = diskManager.readPageFromDisk(i);
      assertThat(loaded.getBuffer().getInt(0)).isEqualTo(i * 100);
    }
  }

  @Test
  @SneakyThrows
  void updatingPageDoesNotDuplicateIt(@TempDir Path tempDir) {
    Path testFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(testFile, pageSize);

    Page page = new Page(0);
    page.getBuffer().putInt(0, 1);
    diskManager.writePageToDisk(page);

    page.getBuffer().putInt(0, 2);
    diskManager.writePageToDisk(page);

    assertThat(diskManager.getNumberOfPages()).isEqualTo(1);
    Page loaded = diskManager.readPageFromDisk(0);
    assertThat(loaded.getBuffer().getInt(0)).isEqualTo(2);
  }

  private RecursiveComparisonConfiguration getPageComparisonConfig() {
    return RecursiveComparisonConfiguration.builder().withEqualsForType((actual, expected) -> {
      actual.rewind();
      expected.rewind();
      return actual.equals(expected);
    }, ByteBuffer.class).build();
  }
}
