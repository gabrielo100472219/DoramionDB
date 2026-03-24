package com.gabrielo.backend.pager;

import com.gabrielo.backend.disk.DiskManager;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PagerTest {

  private static final int PAGE_SIZE = 4096;

  @TempDir
  Path tempDir;

  private Pager pager;

  @BeforeEach
  void beforeEach() {
    Path dbFile = tempDir.resolve("test.ddb");
    DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
    pager = new Pager(diskManager);
  }

  @Nested
  class AllocateNewPage {

    @Test
    @SneakyThrows
    void allocatesPageWithIdZeroWhenEmpty() {
      Page page = pager.allocateNewPage();

      assertThat(page.getId()).isZero();
    }

    @Test
    @SneakyThrows
    void allocatesPageWithIncrementingIds() {
      Page page0 = pager.allocateNewPage();
      Page page1 = pager.allocateNewPage();
      Page page2 = pager.allocateNewPage();

      assertThat(page0.getId()).isEqualTo(0);
      assertThat(page1.getId()).isEqualTo(1);
      assertThat(page2.getId()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    void allocatedPageHasCleanBuffer() {
      Page page = pager.allocateNewPage();

      byte[] allBytes = page.getBuffer().array();
      for (byte b : allBytes) {
        assertThat(b).isZero();
      }
    }
  }

  @Nested
  class GetPage {

    @Test
    @SneakyThrows
    void returnsAllocatedPage() {
      Page allocated = pager.allocateNewPage();

      Page retrieved = pager.getPage(0);

      assertThat(retrieved).isSameAs(allocated);
    }

    @Test
    @SneakyThrows
    void returnsCorrectPageById() {
      pager.allocateNewPage();
      Page page1 = pager.allocateNewPage();
      pager.allocateNewPage();

      Page retrieved = pager.getPage(1);

      assertThat(retrieved.getId()).isEqualTo(1);
      assertThat(retrieved).isSameAs(page1);
    }

    @Test
    @SneakyThrows
    void loadsPageFromDiskWhenNotInCache() {
      Path dbFile = tempDir.resolve("getpage_disk.ddb");
      DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
      Pager pager1 = new Pager(diskManager);

      Page allocated = pager1.allocateNewPage();
      allocated.getBuffer().putInt(0, 42);
      allocated.markDirty();
      pager1.flushAllPages();

      Pager pager2 = new Pager(diskManager);
      Page loaded = pager2.getPage(0);

      assertThat(loaded).isNotNull();
      assertThat(loaded.getId()).isZero();
      assertThat(loaded.getBuffer().getInt(0)).isEqualTo(42);
    }

    @Test
    @SneakyThrows
    void evictsAndReloadsWhenCacheSlotConflicts() {
      Path dbFile = tempDir.resolve("eviction.ddb");
      DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
      Pager testPager = new Pager(diskManager);

      // Allocate 101 pages — page 0 and page 100 share the same cache slot (index 0)
      for (int i = 0; i < 101; i++) {
        Page p = testPager.allocateNewPage();
        p.getBuffer().putInt(0, i);
        p.markDirty();
      }

      // Accessing page 0 should evict page 100 and reload page 0 from disk
      Page page0 = testPager.getPage(0);
      assertThat(page0.getId()).isZero();
      assertThat(page0.getBuffer().getInt(0)).isZero();
    }
  }

  @Nested
  class FlushAllPages {

    @Test
    @SneakyThrows
    void flushesDirtyPagesToDisk() {
      Path dbFile = tempDir.resolve("flush.ddb");
      DiskManager diskManager = new DiskManager(dbFile, PAGE_SIZE);
      Pager pager1 = new Pager(diskManager);

      Page page = pager1.allocateNewPage();
      page.getBuffer().putInt(0, 99);
      page.markDirty();
      pager1.flushAllPages();

      Pager pager2 = new Pager(diskManager);
      Page loaded = pager2.getPage(0);

      assertThat(loaded.getBuffer().getInt(0)).isEqualTo(99);
    }
  }
}
