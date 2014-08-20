package com.gl.vn.me.ko.pies.platform.client.tcp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.pool2.ObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public final class TestTcpSequentialClient {
	private NioEventLoopGroup workerEventLoopGroup;
	private ScheduledExecutorService scheduledExecutorService;
	@SuppressFBWarnings(
			value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "The field is initialized in setUp() method")
	private TcpSequentialClient<byte[], byte[]> client;

	public TestTcpSequentialClient() {
	}

	@Before
	@SuppressWarnings("unchecked")
	public final void setUp() {
		final InetSocketAddress address = InetSocketAddress.createUnresolved("", 0);
		final Future<?> shutdownFuture = mock(Future.class);
		workerEventLoopGroup = mock(NioEventLoopGroup.class);
		when(workerEventLoopGroup.shutdownGracefully()).thenAnswer((invocation) -> {
			return (Future)shutdownFuture;
		});
		scheduledExecutorService = mock(ScheduledExecutorService.class);
		final ObjectPool<TcpConnection<byte[], byte[]>> connectionPool = mock(ObjectPool.class);
		client = new TcpSequentialClient<>(
				address, workerEventLoopGroup, scheduledExecutorService, connectionPool);
	}

	@Test
	public final void shutdown() {
		client.shutdown();
		verify(workerEventLoopGroup, times(1)).shutdownGracefully();
		verify(scheduledExecutorService, times(1)).shutdown();
	}

	@Test
	public final void shutdownIdempotence() {
		client.shutdown();
		final boolean clientState1 = ((AtomicBoolean)Whitebox.getInternalState(client, "active")).get();
		client.shutdown();
		final boolean clientState2 = ((AtomicBoolean)Whitebox.getInternalState(client, "active")).get();
		assertEquals("Assert that client states are equal", clientState1, clientState2);
	}

	@Test(expected = IllegalStateException.class)
	public final void sendAfterShutdown() {
		client.shutdown();
		client.send(new TcpMessage<>(new byte[] {}));
	}
}
