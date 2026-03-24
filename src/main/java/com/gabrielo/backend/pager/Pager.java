package com.gabrielo.backend.pager;

import com.gabrielo.backend.disk.DiskManager;

import java.io.IOException;

public class Pager {

  private static final int MAX_NUMBER_OF_PAGES = 100;

  private final Page[] pages = new Page[MAX_NUMBER_OF_PAGES];

  private final DiskManager diskManager;

  private int totalPagesCreated = -1;

  public Pager(DiskManager diskManager) {
    this.diskManager = diskManager;
  }

  public Page getPage(int id) throws IOException {
    int pageIndex = id % MAX_NUMBER_OF_PAGES;
    ensurePageAvailable(pageIndex, id);
    return pages[pageIndex];
  }

  public Page allocateNewPage() throws IOException {
    initializeTotalPagesCreated();
    int newPageId = totalPagesCreated;
    totalPagesCreated++;
    int pageIndex = newPageId % MAX_NUMBER_OF_PAGES;
    if (pages[pageIndex] != null) {
      evictPage(pageIndex);
    }
    pages[pageIndex] = new Page(newPageId);
    return pages[pageIndex];
  }

  public void flushAllPages() throws IOException {
    for (int i = 0; i < MAX_NUMBER_OF_PAGES; i++) {
      if (pages[i] != null && pages[i].isDirty()) {
        diskManager.writePageToDisk(pages[i].getId(), pages[i].getBuffer().array());
      }
    }
  }

  public int readRootPageId() throws IOException {
    return diskManager.readRootPageId();
  }

  public void writeRootPageId(int rootPageId) throws IOException {
    diskManager.writeRootPageId(rootPageId);
  }

  private void initializeTotalPagesCreated() throws IOException {
    if (totalPagesCreated == -1) {
      totalPagesCreated = diskManager.getNumberOfPages();
    }
  }

  private void evictPage(int pageIndex) throws IOException {
    if (pages[pageIndex] != null && pages[pageIndex].isDirty()) {
      diskManager.writePageToDisk(pages[pageIndex].getId(), pages[pageIndex].getBuffer().array());
    }
  }

  private void ensurePageAvailable(int pageIndex, int id) throws IOException {
    if (pages[pageIndex] == null) {
      pages[pageIndex] = loadPageFromDisk(id);
    }

    if (pages[pageIndex].getId() != id) {
      evictPage(pageIndex);
      pages[pageIndex] = loadPageFromDisk(id);
    }
  }

  private Page loadPageFromDisk(int id) throws IOException {
    byte[] data = diskManager.readPageFromDisk(id);
    if (data == null) {
      return null;
    }
    Page page = new Page(id);
    page.getBuffer().put(0, data);
    return page;
  }
}
