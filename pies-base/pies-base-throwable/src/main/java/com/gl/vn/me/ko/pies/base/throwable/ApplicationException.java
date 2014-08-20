package com.gl.vn.me.ko.pies.base.throwable;

import javax.annotation.Nullable;

/**
 * All custom Application exceptions SHOULD extend this class.
 */
public class ApplicationException extends RuntimeException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link ApplicationException}.
	 */
	public ApplicationException() {
	}

	/**
	 * Creates an instance of {@link ApplicationException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ApplicationException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link ApplicationException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public ApplicationException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link ApplicationException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public ApplicationException(@Nullable final Throwable cause) {
		super(cause);
	}
}
