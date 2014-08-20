package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_ILLEGAL;
import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL;
import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL_SINGLE;
import static com.gl.vn.me.ko.pies.base.constant.Message.format;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.feijoa.StringUtil;
import com.gl.vn.me.ko.pies.base.throwable.TimeoutException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * Represents message that {@link TcpSequentialClient} is able to send.
 *
 * @param <Message>
 * A type of message contained by {@link TcpMessage}.
 * @param <Response>
 * A type of response contained by {@link TcpResponse}.
 */
public final class TcpMessage<Message, Response> {
	private final Message message;
	private final long responseTimeoutMillis;
	private final CompletableFuture<Optional<TcpResponse<Response>>> responseFuture;
	private AtomicReference<TcpConnection<?, ?>> connectionRef;

	/**
	 * Constructs a new instance of {@link TcpMessage}.
	 *
	 * @param message
	 * Message contained by {@link TcpMessage}.
	 * @param responseTimeoutMillis
	 * Amount of time in milliseconds a {@link TcpSequentialClient} will wait for {@linkplain TcpResponse response}
	 * after sending the {@link TcpMessage}.
	 * When this duration expired the {@link CompletionStage} returned by the {@link TcpSequentialClient#send(TcpMessage)}
	 * method is completed with {@link TimeoutException}. This argument MUST NOT be negative.
	 * <p>
	 * If this argument is {@code 0} then a {@link TcpSequentialClient} doesn't wait for and doesn't expect a
	 * {@linkplain TcpResponse response}, and completes {@link CompletionStage}
	 * returned by the {@link TcpSequentialClient#send(TcpMessage)} method once sending of the {@link TcpMessage} is finished.
	 */
	public TcpMessage(final Message message, final long responseTimeoutMillis) {
		checkNotNull(message, ARGUMENT_NULL, "first", "message");
		checkArgument(responseTimeoutMillis >= 0,
				ARGUMENT_ILLEGAL, responseTimeoutMillis, "second", "responseTimeoutMillis", "Expected value must be nonnegative");
		this.message = message;
		this.responseTimeoutMillis = responseTimeoutMillis;
		responseFuture = new CompletableFuture<>();
		connectionRef = new AtomicReference<>(null);
	}

	/**
	 * Constructs a new instance of {@link TcpMessage}.
	 * Invocation of this constructor is equivalent to invocation of the {@link #TcpMessage(Object, long)}
	 * with {@code 0} specified as the second argument.
	 *
	 * @param message
	 * Message contained by {@link TcpMessage}.
	 */
	public TcpMessage(final Message message) {
		this(message, 0);
	}

	/**
	 * Returns message contained by the {@link TcpMessage}.
	 *
	 * @return
	 * A message contained by the {@link TcpMessage}.
	 */
	final Message get() {
		return message;
	}

	/**
	 * Returns {@link CompletableFuture} that represents asynchronous response returned by the
	 * {@link TcpSequentialClient#send(TcpMessage)} method.
	 * {@link TcpSequentialClient} MUST complete this {@link CompletableFuture} either normally or exceptionally.
	 *
	 * @return
	 * Asynchronous response returned by the {@link TcpSequentialClient#send(TcpMessage)} method.
	 */
	final CompletableFuture<Optional<TcpResponse<Response>>> getResponse() {
		return responseFuture;
	}

	/**
	 * Checks if a {@link TcpSequentialClient} MUST expect a response to the {@link TcpMessage}.
	 * If this method returns {@code false} then the {@link TcpSequentialClient} MUST NOT expect any response to the
	 * {@link TcpMessage}.
	 *
	 * @return
	 * {@code true} if a {@link TcpSequentialClient} MUST expect a response to the {@link TcpMessage} and {@code false}
	 * otherwise.
	 */
	final boolean isResponseExpected() {
		return responseTimeoutMillis != 0;
	}

	/**
	 * Returns an amount of time a {@link TcpSequentialClient} MUST wait for response after sending the {@link TcpMessage}.
	 *
	 * @return
	 * An amount of time a {@link TcpSequentialClient} MUST wait for response after sending the {@link TcpMessage}.
	 * @throws IllegalStateException
	 * If {@link #isResponseExpected()} returns {@code false}.
	 */
	final long getResponseTimeoutMillis() throws IllegalStateException {
		checkState(isResponseExpected(), "%s doesn't expect response", this);
		return responseTimeoutMillis;
	}

	/**
	 * Associates the {@link TcpMessage} with {@link TcpConnection} that will be used to send this {@link TcpMessage}.
	 *
	 * @param connection
	 * The {@link TcpConnection} that will be used to send this {@link TcpMessage}.
	 * @throws IllegalStateException
	 * If the method was already invoked with non-{@code null} {@code connection}.
	 * @see #getConnection()
	 */
	final void associate(final TcpConnection<?, ?> connection) throws IllegalStateException {
		checkNotNull(connection, ARGUMENT_NULL_SINGLE, "connection");
		final boolean success = connectionRef.compareAndSet(null, connection);
		checkState(success, "Connection was already set for %s", this);
	}

	/**
	 * Returns {@link TcpConnection} which this {@link TcpMessage} is {@linkplain #associate(TcpConnection) associated} with.
	 *
	 * @return
	 * {@link TcpConnection} that was specified via the {@link #associate(TcpConnection)} method.
	 * @throws IllegalStateException
	 * If {@link TcpConnection} wasn't {@link #associate(TcpConnection) set} for this {@link TcpMessage}.
	 * @see #associate(TcpConnection)
	 */
	final TcpConnection<?, ?> getConnection() {
		@Nullable
		TcpConnection<?, ?> connection = connectionRef.get();
		final TcpConnection<?, ?> result;
		if (connection != null) {
			result = connection;
		} else {
			throw new IllegalStateException(format("Connection wasn't set for %s", this));
		}
		return result;
	}

	/**
	 * Returns a description of the {@link TcpMessage}.
	 *
	 * @return
	 * A description of the {@link TcpMessage}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(message=").append(StringUtil.valueOfArrayAware(message)).append(", ")
				.append("responseTimeoutMillis=").append(responseTimeoutMillis).append(", ")
				.append("responseFuture=").append(responseFuture).append(')');
		final String result = sb.toString();
		return result;
	}
}
