package com.gl.vn.me.ko.pies.platform.server.tcp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.SECONDS;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP implementation of the {@link Server} interface.
 */
@ThreadSafe
public class TcpServer implements Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
	/**
	 * Requested maximum length of the queue of incoming connections.
	 * Note that exact semantics are implementation specific
	 * and value MAY be just ignored by the {@link ServerSocket} implementation.
	 */
	private static final Integer BACKLOG = Integer.valueOf(100);
	private static final long TERMINATION_TIMEOUT_SECS = 10;
	private static final ChannelInitializer<Channel> DEFAULT_CHANNEL_INITIALIZER;
	private static final ChannelHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
	private final Object mutexStartShutdown;
	private boolean active;
	private final ServerBootstrap serverBootstrap;
	private final InetSocketAddress address;
	private final String name;
	private final NioEventLoopGroup bossEventLoopGroup;
	private final NioEventLoopGroup workerEventLoopGroup;

	static {
		DEFAULT_CHANNEL_INITIALIZER = new ChannelInitializer<Channel>() {
			@Override
			protected final void initChannel(final Channel channel) throws Exception {
				final ChannelPipeline pipeline = channel.pipeline();
				pipeline.addFirst(LOGGING_HANDLER);
			}
		};
	}

	/**
	 * Constructs a new instance of {@link TcpServer}.
	 *
	 * @param address
	 * An {@link InetSocketAddress} the {@link TcpServer} will listen to.
	 * @param name
	 * A name of the {@link TcpServer}.
	 * @param maxBossThreads
	 * Maximum number of {@link Thread}s that accept new TCP connections.
	 * @param maxWorkerThreads
	 * Maximum number of {@link Thread}s that process data received via the accepted TCP connections.
	 * @param threadFactory
	 * A {@link ThreadFactory} that will be used to create boss and worker {@link Thread}s.
	 * {@link Thread} names MAY not be the same as the {@code threadFactory} generates.
	 * @param serverSocketChannelInitializer
	 * A {@link ChannelInitializer} that will be used to initialize a {@link ServerSocketChannel} that is
	 * bound to the {@code address}. If this argument is {@code null} then default {@link ChannelInitializer},
	 * that adds a {@link LoggingHandler} with {@link LogLevel#DEBUG}, will be used.
	 * @param workerSocketChannelInitializer
	 * A {@link ChannelInitializer} that will be used to initialize a {@link SocketChannel}s that are
	 * children of the {@link ServerSocketChannel}. If this argument is {@code null} then default
	 * {@link ChannelInitializer}, that adds a {@link LoggingHandler} with {@link LogLevel#DEBUG}, will be used.
	 */
	@Inject
	public TcpServer(
			@TcpServerAddress final InetSocketAddress address,
			@TcpServerName final String name,
			@TcpServerBoss final Integer maxBossThreads,
			@TcpServerWorker final Integer maxWorkerThreads,
			@TcpServerThreadFactory final ThreadFactory threadFactory,
			@TcpServerBoss @Nullable final ChannelInitializer<ServerSocketChannel> serverSocketChannelInitializer,
			@TcpServerWorker @Nullable final ChannelInitializer<SocketChannel> workerSocketChannelInitializer) {
		checkNotNull(address, Message.ARGUMENT_NULL, "first", "address");
		checkNotNull(name, Message.ARGUMENT_NULL, "second", "name");
		checkNotNull(maxBossThreads, Message.ARGUMENT_NULL, "third", "maxBossThreads");
		checkNotNull(maxWorkerThreads, Message.ARGUMENT_NULL, "fourth", "maxWorkerThreads");
		final int maxBosses = maxBossThreads.intValue();
		final int maxWorkers = maxWorkerThreads.intValue();
		checkArgument(maxBosses > 0, Message.ARGUMENT_ILLEGAL, maxBossThreads, "third", "maxBossThreads",
				"Expected value must be greater than 0");
		checkArgument(maxWorkers > 0, Message.ARGUMENT_ILLEGAL, maxWorkerThreads, "fourth", "maxWorkerThreads",
				"Expected value must be greater than 0");
		checkNotNull(threadFactory, Message.ARGUMENT_NULL, "fifth", "threadFactory");
		mutexStartShutdown = new Object();
		active = false;
		this.address = address;
		this.name = name;
		this.bossEventLoopGroup = createBossEventLoop(maxBosses, threadFactory);
		this.workerEventLoopGroup = createWorkerEventLoop(maxWorkers, threadFactory);
		serverBootstrap = new ServerBootstrap();
		serverBootstrap
				.group(this.bossEventLoopGroup, this.workerEventLoopGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, BACKLOG)
				.handler(serverSocketChannelInitializer == null
								? DEFAULT_CHANNEL_INITIALIZER : serverSocketChannelInitializer)
				.childHandler(workerSocketChannelInitializer == null
								? DEFAULT_CHANNEL_INITIALIZER : workerSocketChannelInitializer);
	}

	/**
	 * Constructs a new instance of {@link TcpServer}.
	 * This constructor MAY be used in tests and MUST NOT be used elsewhere.
	 *
	 * @param serverBootstrap
	 * A {@link ServerBootstrap} that will be used by {@link TcpServer}.
	 * @param address
	 * An {@link InetSocketAddress} that will be used by {@link TcpServer}.
	 * @param bossEventLoopGroup
	 * A boss {@link NioEventLoopGroup} that will be used by {@link TcpServer}.
	 * @param workerEventLoopGroup
	 * A worker {@link NioEventLoopGroup} that will be used by {@link TcpServer}.
	 */
	@VisibleForTesting
	TcpServer(
			final ServerBootstrap serverBootstrap,
			final InetSocketAddress address,
			final NioEventLoopGroup bossEventLoopGroup,
			final NioEventLoopGroup workerEventLoopGroup) {
		mutexStartShutdown = new Object();
		active = false;
		this.serverBootstrap = serverBootstrap;
		this.address = address;
		name = "For testing only";
		this.bossEventLoopGroup = bossEventLoopGroup;
		this.workerEventLoopGroup = workerEventLoopGroup;
	}

	private final NioEventLoopGroup createBossEventLoop(final int maxThreads, final ThreadFactory threadFactory) {
		final ThreadFactory bossThreadFactory = new ThreadFactoryBuilder()
				.setThreadFactory(threadFactory)
				.setNameFormat(name + "-boss-%d")
				.build();
		return new NioEventLoopGroup(maxThreads, bossThreadFactory);
	}

	private final NioEventLoopGroup createWorkerEventLoop(final int maxThreads, final ThreadFactory threadFactory) {
		final ThreadFactory workerThreadFactory = new ThreadFactoryBuilder()
				.setThreadFactory(threadFactory)
				.setNameFormat(name + "-worker-%d")
				.build();
		return new NioEventLoopGroup(maxThreads, workerThreadFactory);
	}

	/**
	 * Returns {@link ServerBootstrap}. Subclasses MAY use it to tune {@link TcpServer} before the first invocation of
	 * {@link #start()}. Generally this means that {@link ServerBootstrap} SHOULD only be used from constructors of
	 * subclasses.
	 *
	 * @return
	 * Returns {@link ServerBootstrap}.
	 */
	protected final ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see #shutdownHook()
	 */
	@Override
	public final void shutdown() {
		synchronized (mutexStartShutdown) {
			if (active) {
				final Future<?> bossShutdownFuture = bossEventLoopGroup.shutdownGracefully();
				final Future<?> workerShutdownFuture = workerEventLoopGroup.shutdownGracefully();
				bossShutdownFuture.awaitUninterruptibly(TERMINATION_TIMEOUT_SECS, SECONDS);
				workerShutdownFuture.awaitUninterruptibly(TERMINATION_TIMEOUT_SECS, SECONDS);
				shutdownHook();
				active = false;
				LOGGER.info("{} was shut down", this);
			}
		}
	}

	/**
	 * Subclasses MAY implement this method in order to perform shutdown actions; this implementation does nothing.
	 * This method is called from the {@link #shutdown()} method and MAY be not idempotent.
	 */
	protected void shutdownHook() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method MUST NOT be called more than once on the same instance of {@link TcpServer}.
	 */
	@Override
	public final Future<?> start() throws InterruptedException {
		final ChannelFuture result;
		synchronized (mutexStartShutdown) {
			checkState(active == false, "Server %s is already started", this);
			final ChannelFuture serverSocketChannelBindFuture;
			serverSocketChannelBindFuture = serverBootstrap.bind(address);
			serverSocketChannelBindFuture.await();
			if (serverSocketChannelBindFuture.isSuccess()) {
				LOGGER.info("{} is listening for requests", this);
				final Channel serverSocketChannel = serverSocketChannelBindFuture.channel();
				final ChannelFuture serverSocketChannelClosedFuture = serverSocketChannel.closeFuture();
				result = serverSocketChannelClosedFuture;
			} else {
				throw new ApplicationException(serverSocketChannelBindFuture.cause());
			}
			activate();
		}
		return result;
	}

	/**
	 * Sets {@link #active} state to {@code true}.
	 * This method is called from {@link #start()}.
	 * The method MAY be used in tests and MUST NOT be used elsewhere.
	 */
	@VisibleForTesting
	final void activate() {
		active = true;
	}

	/**
	 * Returns name of the {@link TcpServer}.
	 *
	 * @return
	 * Name of the {@link TcpServer}.
	 */
	protected final String getName() {
		return name;
	}

	/**
	 * Returns {@link InetSocketAddress} this {@link TcpServer} listens to.
	 *
	 * @return
	 * {@link InetSocketAddress} this {@link TcpServer} listens to.
	 */
	protected final InetSocketAddress getAddress() {
		return address;
	}

	/**
	 * Returns if the {@link TcpServer} active or wasn't {@linkplain #start() started}, or was {@linkplain #shutdown() shut down}.
	 *
	 * @return
	 * {@code true} if the {@link TcpServer} active, {@code false} otherwise.
	 */
	protected final boolean getState() {
		return active;
	}

	/**
	 * Returns a description of the {@link TcpServer}.
	 *
	 * @return
	 * A description of the {@link TcpServer}.
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(name)
				.append("(address=").append(address).append(", ")
				.append("active=").append(active).append(')');
		final String result = sb.toString();
		return result;
	}
}
