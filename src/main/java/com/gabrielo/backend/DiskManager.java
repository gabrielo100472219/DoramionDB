package com.gabrielo.backend;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DiskManager {

	private final Path filePath;

	private final int pageSize;

	private final int pageMetadataSize = Integer.BYTES;

	private final int metadataSize = Integer.BYTES;

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

			ByteBuffer buffer = ByteBuffer.allocate(4);
			channel.read(buffer, 0);
			buffer.flip();
			return buffer.getInt();
		}
	}

	private void initializeMetadata(FileChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(pageSize);
		buffer.putInt(0);
		buffer.flip();
		channel.write(buffer, 0);
	}

	public Page readPageFromDisk(int id) throws IOException {
		try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ)) {
			ByteBuffer pageBuffer = ByteBuffer.allocate(pageMetadataSize + pageSize);
			channel.read(pageBuffer, metadataSize);
			pageBuffer.flip();

			int storedId = pageBuffer.getInt();

			byte[] data = new byte[pageSize];
			pageBuffer.get(data);

			int recordSize = 68;
			Page page = new Page(storedId, recordSize);
			page.getBuffer().put(data);
			page.getBuffer().flip();

			return page;
		}
	}

	public void writePageToDisk(Page page) throws IOException {
		try (var channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ,
				StandardOpenOption.WRITE)) {

			ByteBuffer pageBuffer = ByteBuffer.allocate(pageMetadataSize + pageSize);
			pageBuffer.putInt(page.getId());
			pageBuffer.put(page.getBuffer().array());
			pageBuffer.flip();

			channel.write(pageBuffer, metadataSize);
		}
	}
}
