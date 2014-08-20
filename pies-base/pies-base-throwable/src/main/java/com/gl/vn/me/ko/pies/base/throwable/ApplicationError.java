package com.gl.vn.me.ko.pies.base.throwable;

import javax.annotation.Nullable;

/**
 * Indicates a error in the Application logic and the only way to handle it is to fix the Application.
 */
public final class ApplicationError extends RuntimeException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link ApplicationError}.
	 */
	public ApplicationError() {
	}

	/**
	 * Creates an instance of {@link ApplicationError}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ApplicationError(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link ApplicationError}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public ApplicationError(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link ApplicationError}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public ApplicationError(@Nullable final Throwable cause) {
		super(cause);
	}
}
