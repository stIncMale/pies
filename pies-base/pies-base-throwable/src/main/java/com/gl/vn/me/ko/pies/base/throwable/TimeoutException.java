package com.gl.vn.me.ko.pies.base.throwable;

import javax.annotation.Nullable;

/**
 * Indicates that some action wasn't completed due to timeout.
 */
public final class TimeoutException extends ExternallyVisibleException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates an instance of {@link TimeoutException}.
	 *
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 */
	public TimeoutException(final String externalMessage) {
		super(externalMessage);
	}

	/**
	 * Creates an instance of {@link TimeoutException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 */
	public TimeoutException(@Nullable final String message, final String externalMessage) {
		super(message, externalMessage);
	}

	/**
	 * Creates an instance of {@link TimeoutException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 */
	public TimeoutException(
			@Nullable final String message, @Nullable final Throwable cause, final String externalMessage) {
		super(message, cause, externalMessage);
	}

	/**
	 * Creates an instance of {@link TimeoutException}.
	 *
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public TimeoutException(final String externalMessage, @Nullable final Throwable cause) {
		super(externalMessage, cause);
	}
}
