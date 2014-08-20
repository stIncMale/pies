package com.gl.vn.me.ko.pies.base.throwable;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * This {@link ApplicationException} is intended to provide a error message
 * that MAY be made visible to a client of an Application.
 */
public class ExternallyVisibleException extends ApplicationException {
	private static final long serialVersionUID = 0;
	/**
	 * Message that MAY be made visible to a client of an Application.
	 *
	 * @serial
	 */
	private final String externalMessage;

	/**
	 * Creates an instance of {@link ExternallyVisibleException}.
	 *
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 */
	public ExternallyVisibleException(final String externalMessage) {
		super(externalMessage);
		checkNotNull(externalMessage, Message.ARGUMENT_NULL_SINGLE, "externalMessage");
		this.externalMessage = externalMessage;
	}

	/**
	 * Creates an instance of {@link ExternallyVisibleException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 */
	public ExternallyVisibleException(@Nullable final String message, final String externalMessage) {
		super(message);
		checkNotNull(externalMessage, Message.ARGUMENT_NULL, "second", "externalMessage");
		this.externalMessage = externalMessage;
	}

	/**
	 * Creates an instance of {@link ExternallyVisibleException}.
	 *
	 * @param message
	 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 */
	public ExternallyVisibleException(
			@Nullable final String message,
			@Nullable final Throwable cause,
			final String externalMessage) {
		super(message, cause);
		checkNotNull(externalMessage, Message.ARGUMENT_NULL, "third", "externalMessage");
		this.externalMessage = externalMessage;
	}

	/**
	 * Creates an instance of {@link ExternallyVisibleException}.
	 *
	 * @param externalMessage
	 * Message that MAY be made visible to a client of an Application.
	 * @param cause
	 * The cause which is saved for later retrieval by the {@link #getCause()} method.
	 */
	public ExternallyVisibleException(final String externalMessage, @Nullable final Throwable cause) {
		super(externalMessage, cause);
		checkNotNull(externalMessage, Message.ARGUMENT_NULL, "second", "externalMessage");
		this.externalMessage = externalMessage;
	}

	/**
	 * Returns message that MAY be made visible to a client of an Application.
	 *
	 * @return
	 * Message that MAY be made visible to a client of an Application.
	 */
	public final String getExternalMessage() {
		return externalMessage;
	}

	/**
	 * A special deserialization method.
	 * Required because the class is stateful, extendable and {@link Serializable}.
	 *
	 * @throws InvalidObjectException
	 * Always.
	 */
	private final void readObjectNoData() throws InvalidObjectException {
		throw new InvalidObjectException("Stream data required");
	}

	/**
	 * A special serialization method.
	 *
	 * @param out
	 * {@link ObjectOutputStream}.
	 * @throws IOException
	 * If I/O error occur.
	 * @serialData
	 * Writes data by using {@link ObjectOutputStream#defaultWriteObject()}.
	 */
	private final void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	/**
	 * A special deserialization method.
	 *
	 * @param in
	 * {@link ObjectInputStream}.
	 * @throws InvalidObjectException
	 * If {@link #externalMessage} is {@code null}.
	 * @throws ClassNotFoundException
	 * If the class of a {@link Serializable} object could not be found.
	 */
	private final void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException, InvalidObjectException {
		in.defaultReadObject();
		if (externalMessage == null) {
			throw new InvalidObjectException("External message must not be null");
		}
	}
}
