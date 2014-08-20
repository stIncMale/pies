package com.gl.vn.me.ko.pies.base.config;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates that the property is undefined
 * (that is not specified in the configuration or its value is {@code null}).
 */
public final class NoSuchPropertyException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link NoSuchPropertyException}.
	 */
	NoSuchPropertyException() {
	}

	/**
	 * Creates an instance of {@link NoSuchPropertyException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	NoSuchPropertyException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link NoSuchPropertyException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	NoSuchPropertyException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link NoSuchPropertyException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	NoSuchPropertyException(@Nullable final Throwable cause) {
		super(cause);
	}
}
