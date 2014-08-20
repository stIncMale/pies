package com.gl.vn.me.ko.pies.platform.server.rest;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates that handling of a {@link RestRequest} has failed.
 */
public final class RestRequestHandlingException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link RestRequestHandlingException}.
	 */
	public RestRequestHandlingException() {
	}

	/**
	 * Creates an instance of {@link RestRequestHandlingException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public RestRequestHandlingException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link RestRequestHandlingException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public RestRequestHandlingException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link RestRequestHandlingException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public RestRequestHandlingException(@Nullable final Throwable cause) {
		super(cause);
	}
}
