package com.gabrielo.backend;

import com.gabrielo.storage.Record;

import java.nio.ByteBuffer;

public class RecordSerializer {

	private final int ID_SIZE = 4;
	private final int NAME_SIZE = 32;
	private final int EMAIL_SIZE = 32;
	private final int RECORD_SIZE = ID_SIZE + NAME_SIZE + EMAIL_SIZE;
	private final int NAME_OFFSET = ID_SIZE;
	private final int EMAIL_OFFSET = NAME_OFFSET + NAME_SIZE;

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
