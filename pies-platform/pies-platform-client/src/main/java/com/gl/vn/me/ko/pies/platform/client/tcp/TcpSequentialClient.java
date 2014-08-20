package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_ILLEGAL;
import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL;
import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL_SINGLE;
import static com.gl.vn.me.ko.pies.base.constant.Message.SWALLOWED;
import static com.gl.vn.me.ko.pies.base.constant.Message.format;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.SECONDS;
import com.gl.vn.me.ko.pies.base.feijoa.ExecutorUtil;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.gl.vn.me.ko.pies.platform.client.Client;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP implementation of the {@link Client} interface that is suitable only to access TCP servers
 * that don't shuffle responses and can tolerate reuse of TCP connections by a client.
 * I.e. {@link TcpSequentialClient} can successfully operate with a server if the following is true:
 * <ul>
 * <li>if the server receives messages in the same TCP connection in the order {@code msg1}, {@code msg2}, {@code msg3},
 * it responds to the messages in the same order {@code msg1_response}, {@code msg3_response}
 * (some messages MAY not imply responses, so there is no {@code msg2_response} in this example)</li>
 * <li>the server can tolerate that client can reuse TCP connections to the server</li>
 * </ul>
 * Echo servers and HTTP servers are good examples of servers the {@link TcpSequentialClient} can operate with.
 *
 * @param <Message>
 * A type of data contained by {@link TcpMessage}.
 * @param <Response>
 * A type of data contained by {@link TcpResponse}.
 */
@ThreadSafe
public final class TcpSequentialClient<Message, Response>
		implements Client<TcpMessage<Message, Response>, TcpResponse<Response>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpSequentialClient.class);
	private static final int CONNECTION_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS = 30_000;
	private static final long TERMINATION_TIMEOUT_SECS = 10;

	private final String name;
	private final InetSocketAddress address;
	private final NioEventLoopGroup workerEventLoopGroup;
	private final ScheduledExecutorService scheduledExecutorService;
	private final ObjectPool<TcpConnection<Message, Response>> connectionPool;
	private final AtomicBoolean active;

	/**
	 * Constructs a new instance of {@link TcpSequentialClient}.
	 *
	 * @param address
	 * An {@link InetSocketAddress} the {@link TcpSequentialClient} will connect to.
	 * @param name
	 * A name of the {@link TcpSequentialClient}.
	 * @param maxWorkerThreads
	 * Maximum number of {@link Thread}s that process data send and received via the established TCP connections.
	 * @param threadFactory
	 * A {@link ThreadFactory} that will be used to create {@link Thread}s.
	 * {@link Thread} names MAY not be the same as the {@code threadFactory} generates.
	 * @param workerSocketChannelInitializer
	 * A {@link TcpChannelInitializer} that will be used to initialize {@link SocketChannel}s.
	 * Additional {@link ChannelHandler}s MAY be added
	 * {@linkplain ChannelPipeline#addLast(ChannelHandler...) at the end} of a {@link ChannelPipeline}
	 * by {@link TcpSequentialClient}.
	 * {@code workerSocketChannelInitializer} SHOULD provide at least
	 * encoder for {@code Message} (e.g. {@link MessageToByteEncoder}{@code <Message>})
	 * and decoder for {@code Response} (e.g. {@link ByteToMessageDecoder}{@code <Response>}) objects
	 * (such encoder and decoder MAY be omitted if {@code Message} and {@code Response} are {@link ByteBuf}).
	 * @param connectTimeoutMillis
	 * Amount of time in milliseconds to wait for connecting to the {@code address}.
	 * This argument MUST be positive.
	 */
	@Inject
	public TcpSequentialClient(
			@TcpSequentialClientAddress final InetSocketAddress address,
			@TcpSequentialClientName final String name,
			@TcpSequentialClientWorker final Integer maxWorkerThreads,
			@TcpSequentialClientThreadFactory final ThreadFactory threadFactory,
			@TcpSequentialClientWorker final TcpChannelInitializer workerSocketChannelInitializer,
			@TcpSequentialClientConnectTimeout final Integer connectTimeoutMillis) {
		checkNotNull(address, ARGUMENT_NULL, "first", "address");
		checkNotNull(name, ARGUMENT_NULL, "second", "name");
		checkNotNull(maxWorkerThreads, ARGUMENT_NULL, "third", "maxWorkerThreads");
		checkNotNull(threadFactory, ARGUMENT_NULL, "fourth", "threadFactory");
		checkNotNull(workerSocketChannelInitializer, ARGUMENT_NULL, "fifth", "workerSocketChannelInitializer");
		checkNotNull(connectTimeoutMillis, ARGUMENT_NULL, "sixth", "connectTimeoutMillis");
		checkArgument(connectTimeoutMillis.longValue() > 0,
				ARGUMENT_ILLEGAL, connectTimeoutMillis, "sixth", "connectTimeoutMillis", "Expected value must be positive");
		this.name = name;
		this.address = address;
		workerEventLoopGroup = createWorkerEventLoop(maxWorkerThreads.intValue(), threadFactory);
		connectionPool = createConnectionPool(
				maxWorkerThreads.intValue(),
				workerEventLoopGroup,
				workerSocketChannelInitializer,
				connectTimeoutMillis);
		scheduledExecutorService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
				.setThreadFactory(threadFactory)
				.setNameFormat(name + "-responseTimeoutCancellator-%d")
				.build());
		active = new AtomicBoolean(true);
	}

	/**
	 * Constructs a new instance of {@link TcpSequentialClient}.
	 * This constructor MAY be used in tests and MUST NOT be used elsewhere.
	 *
	 * @param address
	 * An {@link InetSocketAddress} that will be used by {@link TcpSequentialClient}.
	 * @param workerEventLoopGroup
	 * A {@link NioEventLoopGroup} that will be used by {@link TcpSequentialClient}.
	 * @param scheduledExecutorService
	 * A {@link ScheduledExecutorService} that will be used by {@link TcpSequentialClient}.
	 * @param connectionPool
	 * An {@link ObjectPool} that will be used by {@link TcpSequentialClient}.
	 */
	@VisibleForTesting
	TcpSequentialClient(
			final InetSocketAddress address,
			final NioEventLoopGroup workerEventLoopGroup,
			final ScheduledExecutorService scheduledExecutorService,
			final ObjectPool<TcpConnection<Message, Response>> connectionPool) {
		this.name = "For testing only";
		this.address = address;
		this.workerEventLoopGroup = workerEventLoopGroup;
		this.scheduledExecutorService = scheduledExecutorService;
		this.connectionPool = connectionPool;
		this.active = new AtomicBoolean(true);
	}

	private final NioEventLoopGroup createWorkerEventLoop(final int maxThreads, final ThreadFactory threadFactory) {
		final ThreadFactory workerThreadFactory = new ThreadFactoryBuilder()
				.setThreadFactory(threadFactory)
				.setNameFormat(name + "-worker-%d")
				.build();
		return new NioEventLoopGroup(maxThreads, workerThreadFactory);
	}

	private final ObjectPool<TcpConnection<Message, Response>> createConnectionPool(
			final int maxWorkerThreads,
			final NioEventLoopGroup workerEventLoopGroup,
			final TcpChannelInitializer workerSocketChannelInitializer,
			Integer connectTimeoutMillis) {
		final Bootstrap bootstrap = new Bootstrap()
				.group(workerEventLoopGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected final void initChannel(final SocketChannel channel) throws Exception {
						workerSocketChannelInitializer.initChannel(channel);
						channel.pipeline().addLast(new TcpSequentialHandler<Message, Response>(scheduledExecutorService));
					}
				});
		final GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		{//initialize poolConfig
			final int maxPooledConnections = maxWorkerThreads;
			poolConfig.setBlockWhenExhausted(false);
			poolConfig.setMaxTotal(maxPooledConnections);
			poolConfig.setMaxIdle(maxPooledConnections);
			poolConfig.setMinIdle(0);
			poolConfig.setMinEvictableIdleTimeMillis(CONNECTION_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS);
			poolConfig.setNumTestsPerEvictionRun(-2);//test each 2nd idle connection
			poolConfig.setTestOnCreate(true);
			poolConfig.setTestOnBorrow(true);
			poolConfig.setTestWhileIdle(true);
			poolConfig.setTimeBetweenEvictionRunsMillis(CONNECTION_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS);
		}
		final GenericObjectPool<TcpConnection<Message, Response>> result = new GenericObjectPool<>(
				new PooledTcpConnectionFactory<Message, Response>(
						new ConnectedSocketChannelFactory(bootstrap, address)), poolConfig);
		result.setSwallowedExceptionListener(e -> LOGGER.error(SWALLOWED, e));
		return result;
	}

	/**
	 * Sends the supplied {@code message}.
	 *
	 * @param message
	 * Message to send.
	 * @return
	 * A {@link CompletionStage} that represents asynchronous {@link Optional} result of the method.
	 * {@link Optional} {@linkplain Optional#isPresent() is present} if the {@code message}
	 * expects response (see {@link TcpMessage#TcpMessage(Object, long)} for details).
	 * Otherwise the {@link Optional} {@linkplain Optional#isPresent() isn't present}.
	 */
	@Override
	public final CompletionStage<Optional<TcpResponse<Response>>> send(TcpMessage<Message, Response> message) {
		checkNotNull(message, ARGUMENT_NULL_SINGLE, "message");
		checkState(active.get(), "%s isn't active", this);
		CompletionStage<Optional<TcpResponse<Response>>> result;
		try {
			@Nullable
			TcpConnection<Message, Response> connection;
			try {
				connection = connectionPool.borrowObject();
			} catch (final Exception e) {
				throw new ApplicationException(format("Can't obtain instance of the %s from pool", TcpConnection.class), e);
			}
			try {
				result = connection.send(message);
			} catch (final Exception e) {
				try {
					final TcpConnection<Message, Response> localConnection = connection;
					connectionPool.invalidateObject(localConnection);
					connection = null;
				} catch (final Exception swallowed) {
					/*
					 * We can't throw Exception because it will suppress the original one which is more important.
					 * So we log the exception.
					 */
					LOGGER.error(SWALLOWED, swallowed);
				}
				throw new ApplicationException(e);
			} finally {
				try {
					if (connection != null) {//connections wasn't invalidated
						connectionPool.returnObject(connection);
					}
				} catch (final Exception swallowed) {
					/*
					 * We can't throw Exception because it will suppress the original one (if there is one)
					 * which is more important. So we log the exception.
					 */
					LOGGER.error(SWALLOWED, swallowed);
				}
			}
		} catch (final RuntimeException e) {
			final CompletableFuture<Optional<TcpResponse<Response>>> exceptionallyCompleted = new CompletableFuture<>();
			exceptionallyCompleted.completeExceptionally(new ApplicationException(e));
			result = exceptionallyCompleted;
		}
		return result;
	}

	@Override
	public void shutdown() {
		if (active.compareAndSet(true, false)) {
			workerEventLoopGroup.shutdownGracefully().awaitUninterruptibly(TERMINATION_TIMEOUT_SECS, SECONDS);
			ExecutorUtil.shutdownGracefully(scheduledExecutorService);
			connectionPool.close();
			LOGGER.info("{} was shut down", this);
		}
	}

	/**
	 * Returns a description of the {@link TcpSequentialClient}.
	 *
	 * @return
	 * A description of the {@link TcpSequentialClient}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(name)
				.append("(").append("address=").append(address).append(", ")
				.append("active=").append(active).append(')');
		final String result = sb.toString();
		return result;
	}
}
