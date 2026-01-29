package com.gabrielo.backend;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DiskManagerTest {

	int pageSize = 4096;

	int recordSize = 68;

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
	void insertsOnePageToFile(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, pageSize);
		int id = 0;
		Page page = new Page(id, recordSize);

		diskManager.writePageToDisk(page);

		assertThat(diskManager.readPageFromDisk(id)).isEqualTo(page);
	}
}
