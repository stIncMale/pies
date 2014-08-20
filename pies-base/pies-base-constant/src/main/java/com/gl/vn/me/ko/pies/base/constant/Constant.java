package com.gl.vn.me.ko.pies.base.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Provides Application-wide constants such as {@link java.nio.charset.Charset}, {@link java.util.Locale} and so on.
 */
public final class Constant {
	/**
	 * {@link Locale} that SHOULD be used to represent all not user-specific data.
	 * <p>
	 * Value of this constant is {@link Locale#ROOT}.
	 */
	public static final Locale LOCALE = Locale.ROOT;
	/**
	 * {@link Charset} that should be used unless some external things enforce usage of other {@link Charset}.
	 * <p>
	 * Value of this constant is {@link StandardCharsets#UTF_8}.
	 */
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	/**
	 * Represents Java process exit status (aka return code) that corresponds to successful execution.
	 * <p>
	 * Value of this constant is {@value}.
	 */
	public static final int EXIT_STATUS_SUCCESS = 0;
	/**
	 * Represents Java process exit status (aka return code) that corresponds to unsuccessful execution.
	 * <p>
	 * Value of this constant is {@value}.
	 */
	public static final int EXIT_STATUS_ERROR = 1;

	private Constant() {
		throw new UnsupportedOperationException(Message.INSTANTIATION_NOT_SUPPORTED);
	}
}
