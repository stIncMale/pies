package com.gl.vn.me.ko.pies.base.feijoa;

import com.gl.vn.me.ko.pies.base.constant.Message;
import java.util.Arrays;
import javax.annotation.Nullable;

/**
 * This class provides various utility methods related to {@link String}s.
 */
public final class StringUtil {
	/**
	 * This method if a combination of {@link String#valueOf(java.lang.Object)} method
	 * and {@link Arrays#toString(java.lang.Object[])} methods.
	 * If {@code obj} is an array the result of the method will be the same as the result of the corresponding
	 * {@link Arrays#toString(java.lang.Object[])}
	 * (i.e. it's not always exactly {@link Arrays#toString(java.lang.Object[])},
	 * it can be {@link Arrays#toString(boolean[])} or any other).
	 * If {@code obj} isn't an array the result of the method will be {@link String#valueOf(java.lang.Object)}.
	 *
	 * @param obj
	 * An {@link Object} to represent as a {@link String}.
	 * @return
	 * A {@link String} representation of the {@code obj}.
	 */
	public static final String valueOfArrayAware(@Nullable final Object obj) {
		final String result;
		if (obj instanceof boolean[]) {
			result = Arrays.toString((boolean[])obj);
		} else if (obj instanceof byte[]) {
			result = Arrays.toString((byte[])obj);
		} else if (obj instanceof char[]) {
			result = Arrays.toString((char[])obj);
		} else if (obj instanceof double[]) {
			result = Arrays.toString((double[])obj);
		} else if (obj instanceof float[]) {
			result = Arrays.toString((float[])obj);
		} else if (obj instanceof int[]) {
			result = Arrays.toString((int[])obj);
		} else if (obj instanceof long[]) {
			result = Arrays.toString((long[])obj);
		} else if (obj instanceof short[]) {
			result = Arrays.toString((short[])obj);
		} else if (obj instanceof Object[]) {
			result = Arrays.toString((Object[])obj);
		} else {
			result = String.valueOf(obj);
		}
		return result;
	}

	private StringUtil() {
		throw new UnsupportedOperationException(Message.INSTANTIATION_NOT_SUPPORTED);
	}
}