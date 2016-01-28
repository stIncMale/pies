package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents response that {@link TcpSequentialClient} returns.
 *
 * @param <Response>
 * A type of response contained by {@link TcpResponse}.
 */
@ThreadSafe
public final class TcpResponse<Response> {
	private final Response response;
	private final TcpConnection<?, ?> connection;

	/**
	 * Constructs a new instance of {@link TcpResponse}.
	 *
	 * @param response
	 * Response contained by {@link TcpResponse}.
	 * @param connection
	 * {@link TcpConnection} that was used to send {@link TcpMessage} that has provoked this {@link TcpResponse}
	 * and in which the supplied {@code response} was received.
	 */
	TcpResponse(final Response response, final TcpConnection<?, ?> connection) {
		checkNotNull(response, Message.ARGUMENT_NULL, "first", "response");
		checkNotNull(connection, Message.ARGUMENT_NULL, "second", "connection");
		this.response = response;
		this.connection = connection;
	}

	/**
	 * Returns a response contained by this {@link TcpResponse}.
	 *
	 * @return
	 * A response contained by this {@link TcpResponse}.
	 */
	public final Response get() {
		return response;
	}

	/**
	 * Aborts a TCP connection that was used to get this {@link TcpResponse}.
	 * This method is idempotent.
	 */
	public final void abort() {
		connection.close();
	}

	/**
	 * Returns a description of the {@link TcpResponse}.
	 *
	 * @return
	 * A description of the {@link TcpResponse}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(response=").append(response).append(", ")
				.append("(connection=").append(connection).append(')');
		final String result = sb.toString();
		return result;
	}
}
