package com.gabrielo.backend;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DiskManager {

	private final Path filePath;

	private final int pageSize;

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
		return new Page(1, 1);
	}

	public void writePageToDisk(Page page) {

	}
}
