package com.gl.vn.me.ko.pies.platform.server.rest;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates attempt to create an instance of {@link RestRequest} from elements that violate {@link RestRequest} syntax.
 */
public final class RestRequestSyntaxException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link RestRequestSyntaxException}.
	 */
	RestRequestSyntaxException() {
	}

	/**
	 * Creates an instance of {@link RestRequestSyntaxException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	RestRequestSyntaxException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link RestRequestSyntaxException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	RestRequestSyntaxException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link RestRequestSyntaxException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	RestRequestSyntaxException(@Nullable final Throwable cause) {
		super(cause);
	}
}
