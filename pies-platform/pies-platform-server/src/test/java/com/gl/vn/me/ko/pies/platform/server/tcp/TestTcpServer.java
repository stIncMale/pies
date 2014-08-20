package com.gl.vn.me.ko.pies.platform.server.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class TestTcpServer {
	private static final class TcpServerExtended extends TcpServer {
		@SuppressWarnings("unchecked")
		private static final TcpServerExtended newInstance() {
			final Future<?> shutdownFuture = mock(Future.class);
			final NioEventLoopGroup bossEventLoopGroup = mock(NioEventLoopGroup.class);
			when(bossEventLoopGroup.shutdownGracefully()).thenReturn((Future)shutdownFuture);
			final NioEventLoopGroup workerEventLoopGroup = mock(NioEventLoopGroup.class);
			when(workerEventLoopGroup.shutdownGracefully()).thenReturn((Future)shutdownFuture);
			return new TcpServerExtended(
					new ServerBootstrap(), mock(InetSocketAddress.class), bossEventLoopGroup, workerEventLoopGroup);
		}
		private int shutdownHookInvocationCount = 0;

		private TcpServerExtended(
				final ServerBootstrap serverBootstrap,
				final InetSocketAddress address,
				final NioEventLoopGroup bossEventLoopGroup,
				final NioEventLoopGroup workerEventLoopGroup
		) {
			super(serverBootstrap, address, bossEventLoopGroup, workerEventLoopGroup);
		}

		@Override
		protected void shutdownHook() {
			shutdownHookInvocationCount++;
		}
	}
	private NioEventLoopGroup bossEventLoopGroup;
	private NioEventLoopGroup workerEventLoopGroup;
	@SuppressFBWarnings(
			value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "The field is initialized in setUp() method")
	private TcpServer server;

	public TestTcpServer() {
	}

	@Before
	@SuppressWarnings("unchecked")
	public final void setUp() {
		final Future<?> shutdownFuture = mock(Future.class);
		bossEventLoopGroup = mock(NioEventLoopGroup.class);
		when(bossEventLoopGroup.shutdownGracefully()).thenReturn((Future)shutdownFuture);
		workerEventLoopGroup = mock(NioEventLoopGroup.class);
		when(workerEventLoopGroup.shutdownGracefully()).thenReturn((Future)shutdownFuture);
		server = new TcpServer(new ServerBootstrap(), mock(InetSocketAddress.class), bossEventLoopGroup, workerEventLoopGroup);
	}

	@After
	public final void teadDown() {
		server.shutdown();
	}

	@Test
	public final void shutdown() {
		server.activate();
		server.shutdown();
		assertFalse("Assert that server isn't active", server.getState());
		verify(bossEventLoopGroup, times(1)).shutdownGracefully();
		verify(workerEventLoopGroup, times(1)).shutdownGracefully();
	}

	@Test
	public final void shutdownIdempotence() {
		final TcpServerExtended server = TcpServerExtended.newInstance();
		server.activate();
		server.shutdown();
		server.shutdown();
		assertEquals("Assert that shutdownHook was invoked exactly once", 1, server.shutdownHookInvocationCount);
	}
}
