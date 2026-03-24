package com.gabrielo.backend.pager;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PageTest {

	int testId = 0;

	@Test
	void pageHasCorrectId() {
		Page page = new Page(5);

		assertThat(page.getId()).isEqualTo(5);
	}

	@Test
	void pageBufferIsInitializedToZeros() {
		Page page = new Page(testId);

		byte[] allBytes = page.getBuffer().array();
		for (byte b : allBytes) {
			assertThat(b).isZero();
		}
	}

	@Test
	void pageBufferHasCorrectSize() {
		Page page = new Page(testId);

		assertThat(page.getBuffer().capacity()).isEqualTo(4096);
	}

	@Test
	void pageIsNotDirtyByDefault() {
		Page page = new Page(testId);

		assertFalse(page.isDirty());
	}

	@Test
	void pageIsMarkedDirtyAfterMarkDirty() {
		Page page = new Page(testId);

		page.markDirty();

		assertThat(page.isDirty()).isTrue();
	}

	@Test
	void bufferCanBeWrittenToAndReadFrom() {
		Page page = new Page(testId);

		page.getBuffer().putInt(0, 42);

		assertThat(page.getBuffer().getInt(0)).isEqualTo(42);
	}
}
