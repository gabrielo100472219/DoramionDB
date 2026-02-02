package com.gabrielo.backend;

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

	int recordSize = 68;

	RecordSerializer serializer = new RecordSerializer();

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
	void insertsOneEmptyPageToFile(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, pageSize);
		int id = 0;
		Page page = new Page(id, recordSize);

		diskManager.writePageToDisk(page);

		assertThat(diskManager.readPageFromDisk(id)).usingRecursiveComparison().isEqualTo(page);
	}

	@Test
	@SneakyThrows
	void writesMultipleEmptyPagesToFile(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, pageSize);
		List<Page> pagesToInsert = List.of(new Page(1, recordSize), new Page(2, recordSize), new Page(3, recordSize));

		for (Page page : pagesToInsert) {
			diskManager.writePageToDisk(page);
		}

		assertThat(diskManager.readPageFromDisk(1)).usingRecursiveComparison(getPageComparisonConfig())
				.isEqualTo(pagesToInsert.getFirst());
		assertThat(diskManager.readPageFromDisk(2)).usingRecursiveComparison(getPageComparisonConfig())
				.isEqualTo(pagesToInsert.get(1));
		assertThat(diskManager.readPageFromDisk(3)).usingRecursiveComparison(getPageComparisonConfig())
				.isEqualTo(pagesToInsert.get(2));
	}

	@Test
	@SneakyThrows
	void insertsOneNonEmptyPageToFile(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, pageSize);
		int id = 0;
		Page page = new Page(id, recordSize);
		List<Record> recordsToInsert = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));
		recordsToInsert.forEach(recordToInsert -> page.insert(serializer.serialize(recordToInsert)));

		diskManager.writePageToDisk(page);

		assertThat(diskManager.readPageFromDisk(id)).usingRecursiveComparison().isEqualTo(page);
	}

	@Test
	@SneakyThrows
	void writesMultipleNonEmptyPagesToFile(@TempDir Path tempDir) {
		Path testFile = tempDir.resolve("test.ddb");
		DiskManager diskManager = new DiskManager(testFile, pageSize);
		List<Page> pagesToInsert = List.of(new Page(1, recordSize), new Page(2, recordSize), new Page(3, recordSize));
		List<Record> recordsToInsert = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));
		pagesToInsert.forEach(
				page -> recordsToInsert.forEach(recordToInsert -> page.insert(serializer.serialize(recordToInsert))));

		for (Page page : pagesToInsert) {
			diskManager.writePageToDisk(page);
		}

		assertThat(diskManager.readPageFromDisk(1)).usingRecursiveComparison(getPageComparisonConfig())
				.isEqualTo(pagesToInsert.getFirst());
		assertThat(diskManager.readPageFromDisk(2)).usingRecursiveComparison(getPageComparisonConfig())
				.isEqualTo(pagesToInsert.get(1));
		assertThat(diskManager.readPageFromDisk(3)).usingRecursiveComparison(getPageComparisonConfig())
				.isEqualTo(pagesToInsert.get(2));
	}

	private RecursiveComparisonConfiguration getPageComparisonConfig() {
		return RecursiveComparisonConfiguration.builder().withEqualsForType((actual, expected) -> {
			actual.rewind();
			expected.rewind();
			return actual.equals(expected);
		}, ByteBuffer.class).build();
	}
}
