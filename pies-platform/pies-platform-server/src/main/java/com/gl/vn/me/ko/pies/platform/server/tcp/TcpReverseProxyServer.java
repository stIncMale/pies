package com.gl.vn.me.ko.pies.platform.server.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_ILLEGAL;
import static com.google.common.base.Preconditions.checkArgument;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.slf4j.LoggerFactory;

/**
 * A reverse TCP proxy.
 * This proxy simply transfers data (requests) from clients (front-ends) to servers (back-ends)
 * and transfers data back (responses) from back-ends to front-ends,
 * thus hiding back-ends from front-ends and front-ends from back-ends.
 */
@ThreadSafe
public final class TcpReverseProxyServer extends TcpServer {
	private static final class ServerChannelInitializer extends ChannelInitializer<ServerSocketChannel> {
		private ServerChannelInitializer() {
		}

		@Override
		protected final void initChannel(final ServerSocketChannel channel) throws Exception {
			final ChannelPipeline pipeline = channel.pipeline();
			final ChannelHandler channelHandler;
			if (LoggerFactory.getLogger(TcpReverseProxyServer.class).isDebugEnabled()) {
				channelHandler = new LoggingHandler(LogLevel.DEBUG);
			} else {
				channelHandler = new LoggingHandler(LogLevel.INFO);
			}
			pipeline.addLast(channelHandler);
		}
	}

	private static final class WorkerChannelInitializer extends ChannelInitializer<SocketChannel> {
		private final InetSocketAddress beAddress;
		private final int connectTimeoutMillis;
		private final EventLoopGroup workerEventLoopGroup;

		private WorkerChannelInitializer(
				final InetSocketAddress beAddress,
				final int connectTimeoutMillis,
				final EventLoopGroup workerEventLoopGroup) {
			this.beAddress = beAddress;
			this.connectTimeoutMillis = connectTimeoutMillis;
			this.workerEventLoopGroup = workerEventLoopGroup;
		}

		@Override
		protected final void initChannel(final SocketChannel channel) throws Exception {
			final ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast(new TcpReverseProxyFrontEndChannelHandler(beAddress, connectTimeoutMillis, workerEventLoopGroup));
		}

	}

	/**
	 * Constructs a new instance of {@link TcpReverseProxyServer}.
	 *
	 * @param feAddress
	 * {@link InetSocketAddress} the {@link TcpReverseProxyServer} will listen to (front-end address).
	 * @param beAddress
	 * {@link InetSocketAddress} the {@link TcpReverseProxyServer} will forward requests to (back-end address).
	 * @param name
	 * A name of the {@link TcpReverseProxyServer}.
	 * @param maxBossThreads
	 * Maximum number of {@link Thread}s that accept new TCP connections.
	 * @param maxWorkerThreads
	 * Maximum number of {@link Thread}s that process data received via the accepted TCP connections.
	 * @param threadFactory
	 * A {@link ThreadFactory} that will be used to create boss and worker {@link Thread}s.
	 * {@link Thread} names MAY not be the same as the {@code threadFactory} generates.
	 * @param connectTimeoutMillis
	 * Amount of time in milliseconds to wait for connecting to the {@code beAddress}.
	 * This argument MUST be positive.
	 */
	@Inject
	public TcpReverseProxyServer(
			@TcpReverseProxyServerFrontEndAddress final InetSocketAddress feAddress,
			@TcpReverseProxyServerBackEndAddress final InetSocketAddress beAddress,
			@TcpReverseProxyServerName final String name,
			@TcpReverseProxyServerBoss final Integer maxBossThreads,
			@TcpReverseProxyServerWorker final Integer maxWorkerThreads,
			@TcpReverseProxyServerThreadFactory final ThreadFactory threadFactory,
			@TcpReverseProxyServerConnectTimeout final Integer connectTimeoutMillis) {
		super(feAddress, name, maxBossThreads, maxWorkerThreads, threadFactory, new ServerChannelInitializer(), null);
		checkArgument(connectTimeoutMillis.longValue() > 0,
				ARGUMENT_ILLEGAL, connectTimeoutMillis, "seventh", "connectTimeoutMillis", "Expected value must be positive");
		getServerBootstrap().childHandler(
				new WorkerChannelInitializer(beAddress, connectTimeoutMillis.intValue(), getServerBootstrap().childGroup()));
		getServerBootstrap().childOption(ChannelOption.AUTO_READ, false);
	}
}
