package com.gl.vn.me.ko.pies.base.feijoa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public final class TestThrowableUtil {
	public TestThrowableUtil() {
	}

	@Test
	public final void extractFirstOfMultiple() {
		final RuntimeException e2 = new RuntimeException("e2");
		final RuntimeException e1 = new RuntimeException("e1", e2);
		final Throwable t = new Exception(e1);
		assertEquals("Assert exception was extracted", e1, ThrowableUtil.extract(t, RuntimeException.class).get());
	}

	@Test
	public final void extractAbsent() {
		final Throwable t2 = new Exception();
		final Throwable t = new Exception(t2);
		assertFalse("Assert exception wasn't extracted", ThrowableUtil.extract(t, RuntimeException.class).isPresent());
	}

	@Test
	public final void extractSubclass() {
		final IndexOutOfBoundsException e = new IndexOutOfBoundsException("subclass");
		final Throwable t = new Exception(e);
		assertEquals("Assert subclass exception was extracted", e, ThrowableUtil.extract(t, RuntimeException.class).get());
	}
}
