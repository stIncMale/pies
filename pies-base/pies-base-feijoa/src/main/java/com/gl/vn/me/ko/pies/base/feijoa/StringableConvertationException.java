package com.gl.vn.me.ko.pies.base.feijoa;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates that convertation from {@link String} to {@link Stringable} failed.
 */
public final class StringableConvertationException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link StringableConvertationException}.
	 */
	public StringableConvertationException() {
	}

	/**
	 * Creates an instance of {@link StringableConvertationException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public StringableConvertationException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link StringableConvertationException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public StringableConvertationException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link StringableConvertationException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public StringableConvertationException(@Nullable final Throwable cause) {
		super(cause);
	}
}
