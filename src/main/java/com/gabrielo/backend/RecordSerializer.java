package com.gabrielo.backend;

import java.nio.ByteBuffer;

import static com.gabrielo.backend.Schema.*;

public class RecordSerializer {

	public byte[] serialize(Record record) {
		ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
		buffer.putInt(record.id());
		byte[] nameBytes = record.name().getBytes();
		byte[] emailBytes = record.email().getBytes();
		buffer.position(NAME_OFFSET);
		buffer.put(nameBytes, 0, nameBytes.length);
		buffer.position(EMAIL_OFFSET);
		buffer.put(emailBytes, 0, emailBytes.length);
		return buffer.array();
	}

	public Record deserialize(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		int id = buffer.getInt();
		String name = deserializeString(buffer, NAME_OFFSET, NAME_SIZE);
		String email = deserializeString(buffer, EMAIL_OFFSET, EMAIL_SIZE);
		return new Record(id, name, email);
	}

	private String deserializeString(ByteBuffer buffer, int offset, int size) {
		buffer.position(offset);
		byte[] resultBytes = new byte[size];
		buffer.get(resultBytes, 0, size);
		int realSize = 0;
		while (realSize < size && resultBytes[realSize] != 0) {
			realSize++;
		}
		return new String(resultBytes, 0, realSize);
	}
}
