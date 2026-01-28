package com.gabrielo.backend;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageTest {

	RecordSerializer serializer = new RecordSerializer();

	int testId = 0;

	@Test
	void insertSingleRecord() {
		Page page = new Page(testId, serializer.RECORD_SIZE);
		Record record = new Record(1, "Brielard", "bronson.com");

		boolean result = page.insert(serializer.serialize(record));

		assertThat(result).isTrue();
		byte[] serializedRecord = page.getRecordAt(0);
		assertThat(serializer.deserialize(serializedRecord)).isEqualTo(record);
	}

	@Test
	void insertMultipleRecords() {
		Page page = new Page(testId, serializer.RECORD_SIZE);
		List<Record> recordsToInsert = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));

		recordsToInsert.stream().map(record -> page.insert(serializer.serialize(record)))
				.forEach(result -> assertThat(result).isTrue());

		for (int i = 0; i < recordsToInsert.size(); i++) {
			byte[] serializedRecord = page.getRecordAt(i);
			assertThat(serializer.deserialize(serializedRecord)).isEqualTo(recordsToInsert.get(i));
		}
	}

	@Test
	void insertReturnsFalseWhenPageIsFull() {
		Page page = new Page(testId, serializer.RECORD_SIZE);
		int pageSize = 4096;
		int recordsPerPage = pageSize / serializer.RECORD_SIZE;
		Record record = new Record(1, "Brielard", "bronson.com");
		byte[] serializedRecord = serializer.serialize(record);
		for (int i = 0; i < recordsPerPage; i++) {
			assertThat(page.insert(serializedRecord)).isTrue();
		}

		assertThat(page.insert(serializedRecord)).isFalse();
	}

	@Test
	void getRecordThrowsExceptionWhenInvalidIndex() {
		Page page = new Page(testId, serializer.RECORD_SIZE);
		List<Record> recordsToInsert = List.of(new Record(1, "Gabrielo", "gabrielodon@pescao.com"),
				new Record(2, "Brielingson", "brielingson@pescao.com"),
				new Record(3, "Gabrielin", "gabrielin@pescao.com"));

		recordsToInsert.forEach(record -> page.insert(serializer.serialize(record)));

		assertThatThrownBy(() -> page.getRecordAt(recordsToInsert.size()))
				.isInstanceOf(IndexOutOfBoundsException.class);
		assertThatThrownBy(() -> page.getRecordAt(-1)).isInstanceOf(IndexOutOfBoundsException.class);
	}

	@Test
	void pageIsNotDirtyByDefault() {
		Page page = new Page(testId, serializer.RECORD_SIZE);

		assertFalse(page.isDirty());
	}

	@Test
	void pageIsMarkedDirtyAfterInsert() {
		Page page = new Page(testId, serializer.RECORD_SIZE);
		Record record = new Record(1, "Brielard", "bronson.com");
		page.insert(serializer.serialize(record));

		assertTrue(page.isDirty());
	}
}