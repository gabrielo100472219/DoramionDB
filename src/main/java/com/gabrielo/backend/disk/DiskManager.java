package com.gabrielo.backend.disk;

import com.gabrielo.backend.pager.Page;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DiskManager {

  private final Path filePath;

  private final int pageSize;

  private static final int PAGE_ID_SIZE = Integer.BYTES;

  private static final int ROOT_PAGE_ID_SIZE = Integer.BYTES;

  private static final int METADATA_SIZE = Integer.BYTES + ROOT_PAGE_ID_SIZE;

  public DiskManager(Path filePath, int pageSize) {
    this.filePath = filePath;
    this.pageSize = pageSize;
  }

  public int getNumberOfPages() throws IOException {
    try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ,
        StandardOpenOption.WRITE)) {

      if (channel.size() == 0) {
        initializeMetadata(channel);
        return 0;
      }

      return readNumberOfPagesFromMetadata(channel);
    }
  }

  public int readRootPageId() throws IOException {
    try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ,
        StandardOpenOption.WRITE)) {

      if (channel.size() == 0) {
        initializeMetadata(channel);
        return -1;
      }

      ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
      channel.read(buffer, Integer.BYTES); // offset 4: rootPageId
      buffer.flip();
      return buffer.getInt();
    }
  }

  public void writeRootPageId(int rootPageId) throws IOException {
    try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ,
        StandardOpenOption.WRITE)) {

      if (channel.size() == 0) {
        initializeMetadata(channel);
      }

      ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
      buffer.putInt(rootPageId);
      buffer.flip();
      channel.write(buffer, Integer.BYTES); // offset 4: rootPageId
    }
  }

  private static int readNumberOfPagesFromMetadata(FileChannel channel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    channel.read(buffer, 0);
    buffer.flip();
    return buffer.getInt();
  }

  private void initializeMetadata(FileChannel channel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(METADATA_SIZE);
    buffer.putInt(0);  // numPages
    buffer.putInt(-1); // rootPageId (-1 = no root)
    buffer.flip();
    channel.write(buffer, 0);
  }

  public Page readPageFromDisk(int id) throws IOException {
    try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ)) {
      if (channel.size() == 0) {
        return null;
      }

      long readPosition = METADATA_SIZE + (long) id * (PAGE_ID_SIZE + pageSize);
      ByteBuffer pageBuffer = ByteBuffer.allocate(PAGE_ID_SIZE + pageSize);
      channel.read(pageBuffer, readPosition);
      pageBuffer.flip();
      int pageId = pageBuffer.getInt();

      if (pageId == id) {
        Page page = new Page(id);
        byte[] data = new byte[pageSize];
        pageBuffer.get(data);
        page.getBuffer().put(0, data);
        return page;
      }
      return null;
    }
  }

  public void writePageToDisk(Page page) throws IOException {
    try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ,
        StandardOpenOption.WRITE)) {

      if (channel.size() == 0) {
        initializeMetadata(channel);
      }

      ByteBuffer pageBuffer = ByteBuffer.allocate(PAGE_ID_SIZE + pageSize);
      pageBuffer.putInt(page.getId());
      pageBuffer.put(page.getBuffer().array());
      pageBuffer.flip();

      long writePosition = METADATA_SIZE + (long) page.getId() * (PAGE_ID_SIZE + pageSize);
      channel.write(pageBuffer, writePosition);

      int numberOfPages = readNumberOfPagesFromMetadata(channel);
      if (page.getId() >= numberOfPages) {
        increaseNumberOfPagesToMetadata(numberOfPages, channel);
      }
    }
  }

  private void increaseNumberOfPagesToMetadata(int numberOfPages, FileChannel channel) throws IOException {
    ByteBuffer metadataBuffer = ByteBuffer.allocate(Integer.BYTES);
    metadataBuffer.putInt(numberOfPages + 1);
    metadataBuffer.flip();
    channel.write(metadataBuffer, 0);
  }
}
