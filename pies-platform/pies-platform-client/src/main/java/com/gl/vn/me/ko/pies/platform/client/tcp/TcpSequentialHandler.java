package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.gl.vn.me.ko.pies.base.throwable.TimeoutException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link TcpMessage} and {@link TcpResponse}
 * and MUST be the last in the {@link ChannelPipeline} for each {@link SocketChannel} created by {@link TcpSequentialClient}.
 *
 * @param <Message>
 * A type of message contained by {@link TcpMessage}.
 * @param <Response>
 * A type of response contained by {@link TcpResponse}.
 */
final class TcpSequentialHandler<Message, Response> extends ChannelHandlerAdapter {
	private static final class WaitingMessageCancellator implements Runnable {
		private static final Logger LOGGER = LoggerFactory.getLogger(WaitingMessageCancellator.class);

		private final TcpMessage<?, ?> message;

		private WaitingMessageCancellator(final TcpMessage<?, ?> message) {
			this.message = message;
		}

		@Override
		public final void run() {
			boolean closeConnection = false;
			try {
				closeConnection = message.getResponse().completeExceptionally(
						new TimeoutException((format("Can't get response to %s due to timeout", message)), "TCP response timeout"));
			} finally {
				if (closeConnection) {
					final TcpConnection<?, ?> connection = message.getConnection();
					connection.close();
					LOGGER.debug("{} was closed due to {}", connection, TimeoutException.class.getName());
				}
			}
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpSequentialHandler.class);

	private final Queue<TcpMessage<Message, Response>> messagesWaitingForResponse;
	private final ScheduledExecutorService scheduledExecutorService;

	/**
	 * Constructs a new instance of {@link TcpSequentialHandler}.
	 *
	 * @param scheduledExecutorService
	 * {@link ScheduledExecutorService} that will be used to enforce
	 * {@linkplain TcpMessage#getResponseTimeoutMillis() response timeout}.
	 */
	TcpSequentialHandler(final ScheduledExecutorService scheduledExecutorService) {
		messagesWaitingForResponse = new LinkedList<>();
		this.scheduledExecutorService = scheduledExecutorService;
	}

	@Override
	public final void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
		LOGGER.debug("Writing {} to {}", msg, ctx.channel());
		@SuppressWarnings("unchecked")
		final TcpMessage<Message, Response> message = (TcpMessage<Message, Response>)msg;
		scheduleCompletionByTimeout(message);
		ctx.write(message.get(), promise);
		if (message.isResponseExpected()) {
			messagesWaitingForResponse.add(message);
		}
	}

	/*
	 * This isn't the best implementation because it produced a new completion task for each message.
	 * While such approach is the most accurate in terms of compliance with timeout value,
	 * a better implementation would consider a coarsely quantized time
	 * and aggregate completion tasks that should be completed within the same time quantum (e.g. within the same 100ms).
	 */
	private final void scheduleCompletionByTimeout(final TcpMessage<?, ?> message) {
		if (message.isResponseExpected()) {
			scheduledExecutorService.schedule(
					new WaitingMessageCancellator(message), message.getResponseTimeoutMillis(), MILLISECONDS);
		}
	}

	@Override
	public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		LOGGER.debug("Reading {} from {}", msg, ctx.channel());
		@SuppressWarnings("unchecked")
		final Response response = (Response)msg;
		@Nullable
		final TcpMessage<Message, Response> message = messagesWaitingForResponse.poll();
		if (message != null) {
			message.getResponse().complete(Optional.of(new TcpResponse<>(response, message.getConnection())));
		} else {
			/*
			 * This exception can only occur if the decoder supplied to TcpSequentialClient has error in its logic.
			 */
			exceptionCaught(ctx, new ApplicationError(
					"Message is null. This exception is likely occur if the decoder above this handler has error in its logic"));
		}
	}

	@Override
	public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
		try {
			LOGGER.error("Exception caught", cause);
		} finally {
			try {
				final Channel channel = ctx.channel();
				channel.close();
			} finally {
				messagesWaitingForResponse.stream().forEach(message -> message.getResponse().completeExceptionally(cause));
			}
		}
	}

	@Override
	public final void channelInactive(final ChannelHandlerContext ctx) {
		final Channel channel = ctx.channel();
		try {
			ctx.fireChannelInactive();
			LOGGER.debug("{} is inactive", channel);
		} finally {
			cancelAllMessagesWaitingForResponse();
		}
	}

	@Override
	public final void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) {
		final Channel channel = ctx.channel();
		try {
			ctx.disconnect();
			LOGGER.debug("{} was disconnected", channel);
		} finally {
			cancelAllMessagesWaitingForResponse();
			channel.close();
		}
	}

	@Override
	public final void close(final ChannelHandlerContext ctx, final ChannelPromise promise) {
		try {
			final Channel channel = ctx.channel();
			ctx.close();
			LOGGER.debug("{} was closed", channel);
		} finally {
			cancelAllMessagesWaitingForResponse();
		}
	}

	private final void cancelAllMessagesWaitingForResponse() {
		messagesWaitingForResponse.stream().forEach(message -> message.getResponse().cancel(true));
	}
}
