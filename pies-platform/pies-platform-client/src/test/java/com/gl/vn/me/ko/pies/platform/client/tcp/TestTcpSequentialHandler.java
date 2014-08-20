package com.gl.vn.me.ko.pies.platform.client.tcp;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public final class TestTcpSequentialHandler {
	private ScheduledExecutorService scheduledExecutorService;
	@SuppressFBWarnings(
			value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "The field is initialized in setUp() method")
	private EmbeddedChannel channel;

	public TestTcpSequentialHandler() {
	}

	@Before
	public final void setUp() {
		scheduledExecutorService = mock(ScheduledExecutorService.class);
		channel = new EmbeddedChannel(new TcpSequentialHandler<>(scheduledExecutorService));
	}

	@Test
	public final void writeResponseExpected() {
		testWrite(Long.MAX_VALUE);
	}

	@Test
	public final void writeNoResponseExpected() {
		testWrite(0);
	}

	@Test
	public final void channelRead() {
		final TcpMessage<ByteBuf, ByteBuf> tcpMessage;
		{//prepare channel for in data
			final ByteBuf message = Unpooled.buffer(4);
			message.writeInt(1);
			tcpMessage = new TcpMessage<>(message, Long.MAX_VALUE);
			tcpMessage.associate(new TcpConnection<>(mock(SocketChannel.class)));
			channel.writeOutbound(tcpMessage);
		}
		{//write in data
			final ByteBuf bufIn = Unpooled.buffer(4);
			bufIn.writeInt(2);
			final Object msgIn = bufIn;
			channel.writeInbound(msgIn);
		}
		{//test handled data
			final CompletableFuture<Optional<TcpResponse<ByteBuf>>> futureResponse = tcpMessage.getResponse();
			assertTrue("Assert response is ready", futureResponse.isDone());
		}
	}

	private final void testWrite(final long timeout) {
		final byte[] bytesOut;
		{//write out data
			final ByteBuf message = Unpooled.buffer(4);
			message.writeInt(1);
			bytesOut = byteBufToArray(message);
			final TcpMessage<ByteBuf, ByteBuf> tcpMessage = new TcpMessage<>(message, timeout);
			assertTrue("Assert success channel write out", channel.writeOutbound(tcpMessage));
		}
		{//test handled data
			final ByteBuf bufOutHandled = (ByteBuf)channel.readOutbound();
			final int bufOutHandledSize = bufOutHandled.readableBytes();
			final byte[] bytesOutHandled = new byte[bufOutHandledSize];
			bufOutHandled.readBytes(bytesOutHandled, 0, bufOutHandledSize);
			assertTrue("Assert message was correctly handled", Arrays.equals(bytesOut, bytesOutHandled));
			if (timeout == 0) {
				verify(scheduledExecutorService, times(0)).schedule(any(Runnable.class), eq(timeout), eq(TimeUnit.MILLISECONDS));
			} else {
				verify(scheduledExecutorService, times(1)).schedule(any(Runnable.class), eq(timeout), eq(TimeUnit.MILLISECONDS));
			}
		}
	}

	private final byte[] byteBufToArray(final ByteBuf buf) {
		final int size = buf.readableBytes();
		final byte[] result = new byte[size];
		buf.duplicate().readBytes(result);
		return result;
	}
}
