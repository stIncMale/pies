package com.gl.vn.me.ko.pies.app.initiator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.gl.vn.me.ko.pies.platform.client.tcp.TcpSequentialClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a {@code byte[]} to {@code byte[]} codec for echo {@link TcpSequentialClient}.
 * This {@link ByteToMessageCodec} fails to encode {@code null} and empty arrays.
 */
final class EchoCodec extends ByteToMessageCodec<byte[]> {
	private static final Logger LOGGER = LoggerFactory.getLogger(EchoCodec.class);

	private final Queue<Integer> expectedResponsesLengts;

	/**
	 * Constructs a new instance of {@link EchoCodec}.
	 */
	EchoCodec() {
		super(byte[].class);
		expectedResponsesLengts = new LinkedList<>();
	}

	private final void performDecode(
			final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out, final boolean last) {
		while (true) {
			@Nullable
			final Integer expectedResponseLength = expectedResponsesLengts.peek();
			final int readableBytes = in.readableBytes();
			final Channel channel = ctx.channel();
			LOGGER.debug("Total {} responses are expected from {}. Expected responses lengths {}",
					expectedResponsesLengts.size(), channel, expectedResponsesLengts);
			if (expectedResponseLength != null) {
				final int expectedLength = expectedResponseLength.intValue();//can't be 0, see checks in the method encode(...)
				checkState(expectedLength > 0, "Implementation error. Excepted length must be greater than 0");
				LOGGER.debug("Expecting response of length {} from {}. Response of length {} was received",
						expectedLength, channel, readableBytes);
				if (readableBytes >= expectedLength) {
					expectedResponsesLengts.remove();
					final byte[] response = new byte[expectedLength];
					in.readBytes(response, 0, expectedLength);
					out.add(response);
				} else if (last) {
					throw new ApplicationException(Message.format("%s more bytes were expected from %s",
							Integer.valueOf(expectedLength - readableBytes), channel));
				} else {
					LOGGER.debug("Response of length {} was ignored. Waiting for more data from {}", readableBytes, channel);
					break;
				}
			} else if (readableBytes > 0) {
				throw new ApplicationException(Message.format("No bytes were expected, but %s bytes were received from %s",
						Integer.valueOf(readableBytes), channel));
			} else {
				LOGGER.debug("Empty response from {} was ignored", channel);
				break;
			}
		}
	}

	@Override
	protected final void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
		performDecode(ctx, in, out, false);
	}

	@Override
	protected final void decodeLast(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
		performDecode(ctx, in, out, true);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) {
		checkNotNull(msg, Message.ARGUMENT_NULL, "second", "msg");
		checkArgument(msg.length > 0, Message.ARGUMENT_ILLEGAL, msg, "second", "msg",
				"Expected array must have at least one element");
		out.writeBytes(msg);
		expectedResponsesLengts.add(msg.length);
		LOGGER.debug("Request of length {} was send to {}. Total {} responses are expected",
				msg.length, ctx.channel(), expectedResponsesLengts.size());
	}
}
