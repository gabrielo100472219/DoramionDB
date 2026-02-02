package com.gabrielo.backend;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DiskManager {

	private final Path filePath;

	private final int pageSize;

	// Page id + Page number of records
	private static final int PAGE_METADATA_SIZE = 2 * Integer.BYTES;

	private static final int METADATA_SIZE = Integer.BYTES;

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

	private static int readNumberOfPagesFromMetadata(FileChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		channel.read(buffer, 0);
		buffer.flip();
		return buffer.getInt();
	}

	private void initializeMetadata(FileChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(pageSize);
		buffer.putInt(0);
		buffer.flip();
		channel.write(buffer, 0);
	}

	public Page readPageFromDisk(int id) throws IOException {
		try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ)) {
			if (channel.size() == 0) {
				initializeMetadata(channel);
				return null;
			}

			int numberOfPages = readNumberOfPagesFromMetadata(channel);
			ByteBuffer pageBuffer = ByteBuffer.allocate(PAGE_METADATA_SIZE + pageSize);

			for (int i = 0; i < numberOfPages; i++) {
				long readPosition = METADATA_SIZE + (long) i * (pageSize + PAGE_METADATA_SIZE);

				channel.read(pageBuffer, readPosition);
				pageBuffer.flip();

				int pageId = pageBuffer.getInt();

				if (pageId == id) {
					return createPageWith(id, pageBuffer);
				}
				pageBuffer.clear();
			}
			return null;
		}
	}

	private Page createPageWith(int id, ByteBuffer pageBuffer) {
		int recordSize = 68;
		Page page = new Page(id, recordSize);

		int recordCount = pageBuffer.getInt();

		for (int i = 0; i < recordCount; i++) {
			byte[] recordData = new byte[recordSize];
			pageBuffer.get(recordData);
			page.insert(recordData);
		}

		return page;
	}

	public void writePageToDisk(Page page) throws IOException {
		try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ,
				StandardOpenOption.WRITE)) {

			if (channel.size() == 0) {
				initializeMetadata(channel);
			}
			int numberOfPages = readNumberOfPagesFromMetadata(channel);
			long writePosition = METADATA_SIZE + (long) numberOfPages * (PAGE_METADATA_SIZE + pageSize);

			ByteBuffer pageBuffer = ByteBuffer.allocate(PAGE_METADATA_SIZE + pageSize);
			pageBuffer.putInt(page.getId());
			pageBuffer.putInt(page.getRecordCount());
			pageBuffer.put(page.getBuffer().array());
			pageBuffer.flip();

			channel.write(pageBuffer, writePosition);

			writeNumberOfPagesToMetadata(numberOfPages, channel);
		}
	}

	private void writeNumberOfPagesToMetadata(int numberOfPages, FileChannel channel) throws IOException {
		ByteBuffer metadataBuffer = ByteBuffer.allocate(METADATA_SIZE);
		metadataBuffer.putInt(numberOfPages + 1);
		metadataBuffer.flip();
		channel.write(metadataBuffer, 0);
	}
}
