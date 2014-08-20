package com.gl.vn.me.ko.pies.platform.server.rest;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates that no suitable {@link RestRequestHandler} was found to handle a {@link RestRequest}.
 */
public final class BindingNotFoundException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link BindingNotFoundException}.
	 */
	BindingNotFoundException() {
	}

	/**
	 * Creates an instance of {@link BindingNotFoundException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	BindingNotFoundException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link BindingNotFoundException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	BindingNotFoundException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link BindingNotFoundException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	BindingNotFoundException(@Nullable final Throwable cause) {
		super(cause);
	}
}
