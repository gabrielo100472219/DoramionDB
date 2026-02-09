package com.gabrielo.backend;

/*
In the future this will be a general schema class.
For now only this schema is supported.
 */
public final class Schema {
	public static final int ID_SIZE = 4;
	public static final int NAME_SIZE = 32;
	public static final int EMAIL_SIZE = 32;
	public static final int RECORD_SIZE = ID_SIZE + NAME_SIZE + EMAIL_SIZE;
	public static final int NAME_OFFSET = ID_SIZE;
	public static final int EMAIL_OFFSET = NAME_OFFSET + NAME_SIZE;

	private Schema() {
	}
}
