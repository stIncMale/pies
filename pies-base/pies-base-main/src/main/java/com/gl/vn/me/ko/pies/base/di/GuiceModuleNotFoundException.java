package com.gl.vn.me.ko.pies.base.di;

import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import javax.annotation.Nullable;

/**
 * Indicates that lookup of a {@link GuiceModule} is failed.
 */
public final class GuiceModuleNotFoundException extends ApplicationException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link GuiceModuleNotFoundException}.
	 */
	public GuiceModuleNotFoundException() {
	}

	/**
	 * Creates an instance of {@link GuiceModuleNotFoundException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public GuiceModuleNotFoundException(@Nullable final String message) {
		super(message);
	}

	/**
	 * Creates an instance of {@link GuiceModuleNotFoundException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public GuiceModuleNotFoundException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an instance of {@link GuiceModuleNotFoundException}.
	 *
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public GuiceModuleNotFoundException(@Nullable final Throwable cause) {
		super(cause);
	}
}
