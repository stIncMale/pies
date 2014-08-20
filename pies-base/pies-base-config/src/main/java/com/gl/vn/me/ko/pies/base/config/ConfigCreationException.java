package com.gl.vn.me.ko.pies.base.config;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates that creation of configuration is failed.
 */
public final class ConfigCreationException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link ConfigCreationException}.
	 */
	ConfigCreationException() {
	}

	/**
	 * Creates an instance of {@link ConfigCreationException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	ConfigCreationException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link ConfigCreationException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	ConfigCreationException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link ConfigCreationException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	ConfigCreationException(@Nullable final Throwable cause) {
		super(cause);
	}
}
