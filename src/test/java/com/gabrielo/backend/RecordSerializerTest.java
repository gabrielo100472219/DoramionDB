package com.gabrielo.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RecordSerializerTest {

	@Test
	void deserializingSerializedRowIsEqual() {
		Record record = new Record(1, "Gabrielo", "gabrielodon@pescao.com");
		RecordSerializer serializer = new RecordSerializer();

		byte[] result = serializer.serialize(record);
		Record deserialized = serializer.deserialize(result);

		assertThat(deserialized).isEqualTo(record);
	}

	@Test
	void serializesMaxLengthString() {
		Record record = new Record(1, "12345678901234567890123456789012", "gabrielodon@pescao.com");
		RecordSerializer serializer = new RecordSerializer();

		byte[] result = serializer.serialize(record);
		Record deserialized = serializer.deserialize(result);

		assertThat(deserialized).isEqualTo(record);
	}

	@Test
	void serializesEmptyStrings() {
		Record record = new Record(1, "", "gabrielodon@pescao.com");
		RecordSerializer serializer = new RecordSerializer();

		byte[] result = serializer.serialize(record);
		Record deserialized = serializer.deserialize(result);

		assertThat(deserialized).isEqualTo(record);
	}

	@Test
	void serializesNegativeNumbers() {
		Record record = new Record(-1, "Brielo", "gabrielodon@pescao.com");
		RecordSerializer serializer = new RecordSerializer();

		byte[] result = serializer.serialize(record);
		Record deserialized = serializer.deserialize(result);

		assertThat(deserialized).isEqualTo(record);
	}

	@Test
	void serializesSpecialChars() {
		Record record = new Record(-1, "/!·$%&/()", "gabrielodon@pescao.com");
		RecordSerializer serializer = new RecordSerializer();

		byte[] result = serializer.serialize(record);
		Record deserialized = serializer.deserialize(result);

		assertThat(deserialized).isEqualTo(record);
	}
}
