package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL_SINGLE;
import static com.google.common.base.Preconditions.checkNotNull;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents TCP connection established by {@link TcpSequentialClient}.
 *
 * @param <Message>
 * A type of message contained by {@link TcpMessage}.
 * @param <Response>
 * A type of response contained by {@link TcpResponse}.
 */
@ThreadSafe
final class TcpConnection<Message, Response> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpConnection.class);

	/*
	 * SocketChannel is thread-safe though it's not specified in the Netty documentation.
	 */
	private final SocketChannel channel;

	TcpConnection(final SocketChannel channel) {
		checkNotNull(channel, ARGUMENT_NULL_SINGLE, "channel");
		this.channel = channel;
	}

	/**
	 * {@linkplain SocketChannel#writeAndFlush(Object) Sends} the specified {@code message}
	 * via the {@link SocketChannel} used to construct this {@link TcpConnection}.
	 *
	 * @param message
	 * A {@link TcpMessage} to send.
	 * @return
	 * A {@link CompletionStage} that represents asynchronous {@link Optional} result.
	 * {@link Optional} {@linkplain Optional#isPresent() is present} if the {@link TcpMessage} that has provoked
	 * the {@link TcpResponse} expects response (see {@link TcpMessage#TcpMessage(Object, long)} for details).
	 * Otherwise the {@link Optional} {@linkplain Optional#isPresent() isn't present}.
	 */
	final CompletionStage<Optional<TcpResponse<Response>>> send(final TcpMessage<Message, Response> message) {
		checkNotNull(message, ARGUMENT_NULL_SINGLE, "message");
		message.associate(this);
		LOGGER.debug("Writing {} to {}", message, channel);
		final ChannelFuture sendFuture = channel.writeAndFlush(message);
		if (!message.isResponseExpected()) {
			sendFuture.addListener((sendCompletionFuture) -> {
				if (sendCompletionFuture.isSuccess()) {
					message.getResponse().complete(Optional.empty());
				} else {
					try {
						message.getResponse().completeExceptionally(sendCompletionFuture.cause());
					} finally {
						close();
					}
				}
			});
		}
		return message.getResponse();
	}

	/**
	 * Checks if the {@link TcpConnection} is active.
	 *
	 * @return
	 * Returns {@code true} if the underlying {@link Channel} {@linkplain Channel#isActive() is active}.
	 */
	final boolean isActive() {
		return channel.isActive();
	}

	/**
	 * Closes the {@link TcpConnection} by {@linkplain Channel#close() closing} the underlying {@link Channel}.
	 * This method is idempotent.
	 */
	final void close() {
		channel.close();
		LOGGER.debug("{} was closed", channel);
	}

	/**
	 * Returns a description of the {@link TcpConnection}.
	 *
	 * @return
	 * A description of the {@link TcpConnection}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(channel=").append(channel).append(')');
		final String result = sb.toString();
		return result;
	}
}
