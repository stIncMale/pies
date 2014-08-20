package com.gl.vn.me.ko.pies.base.feijoa;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class TestStringUtil {
	public TestStringUtil() {
	}

	@Test
	public final void valueOfArrayAwareNull() {
		assertEquals("Assert that string representation is correct", "null", StringUtil.valueOfArrayAware(null));
	}

	@Test
	public final void valueOfArrayAwareBooleanArray() {
		assertEquals("Assert that string representation is correct",
				"[false, true]", StringUtil.valueOfArrayAware(new boolean[] {false, true}));
	}

	@Test
	public final void valueOfArrayAwareByteArray() {
		assertEquals("Assert that string representation is correct", "[0, 1]", StringUtil.valueOfArrayAware(new byte[] {0, 1}));
	}

	@Test
	public final void valueOfArrayAwareCharArray() {
		assertEquals("Assert that string representation is correct",
				"[a, b]", StringUtil.valueOfArrayAware(new char[] {'a', 'b'}));
	}

	@Test
	public final void valueOfArrayAwareDoubleArray() {
		assertEquals("Assert that string representation is correct",
				"[0.0, 1.0]", StringUtil.valueOfArrayAware(new double[] {0.0, 1.0}));
	}

	@Test
	public final void valueOfArrayAwareFloatArray() {
		assertEquals("Assert that string representation is correct",
				"[0.0, 1.0]", StringUtil.valueOfArrayAware(new float[] {0.0f, 1.0f}));
	}

	@Test
	public final void valueOfArrayAwareIntArray() {
		assertEquals("Assert that string representation is correct", "[0, 1]", StringUtil.valueOfArrayAware(new int[] {0, 1}));
	}

	@Test
	public final void valueOfArrayAwareLongArray() {
		assertEquals("Assert that string representation is correct", "[0, 1]", StringUtil.valueOfArrayAware(new long[] {0, 1}));
	}

	@Test
	public final void valueOfArrayAwareShortArray() {
		assertEquals("Assert that string representation is correct", "[0, 1]", StringUtil.valueOfArrayAware(new short[] {0, 1}));
	}

	@Test
	public final void valueOfArrayAwareObject() {
		assertEquals("Assert that string representation is correct",
				"string object", StringUtil.valueOfArrayAware("string object"));
	}
}
