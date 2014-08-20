package com.gl.vn.me.ko.pies.platform.server.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_ILLEGAL;
import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.gl.vn.me.ko.pies.base.throwable.TimeoutException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles events on front-end {@link Channel}s.
 */
final class TcpReverseProxyFrontEndChannelHandler extends ChannelHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpReverseProxyFrontEndChannelHandler.class);

	private final InetSocketAddress beAddress;
	private final int connectTimeoutMillis;
	private final EventLoopGroup workerEventLoopGroup;
	@Nullable
	private Channel beChannel;

	/**
	 * Constructs a new instance of {@link TcpReverseProxyFrontEndChannelHandler}.
	 *
	 * @param beAddress
	 * Back-end {@link InetSocketAddress}.
	 * @param connectTimeoutMillis
	 * Amount of time in milliseconds to wait for connecting to the {@code beAddress}.
	 * This argument MUST be positive.
	 * @param workerEventLoopGroup
	 * {@Link EventLoopGroup} to use to process events on back-end {@link Channel}s.
	 */
	TcpReverseProxyFrontEndChannelHandler(
			final InetSocketAddress beAddress, final int connectTimeoutMillis, final EventLoopGroup workerEventLoopGroup) {
		checkNotNull(beAddress, ARGUMENT_NULL, "first", "beAddress");
		checkArgument(connectTimeoutMillis > 0, ARGUMENT_ILLEGAL, connectTimeoutMillis,
				"second", "connectTimeoutMillis", "Expected value must be positive");
		checkNotNull(workerEventLoopGroup, ARGUMENT_NULL, "third", "workerEventLoopGroup");
		this.beAddress = beAddress;
		this.connectTimeoutMillis = connectTimeoutMillis;
		this.workerEventLoopGroup = workerEventLoopGroup;
	}

	@Override
	public final void channelActive(final ChannelHandlerContext ctx) {
		final Channel feChannel = ctx.channel();
		final Bootstrap beBootstrap = new Bootstrap().group(workerEventLoopGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
				.handler(new TcpReverseProxyBackEndChannelHandler(feChannel));
		final ChannelFuture beConnectFuture = beBootstrap.connect(beAddress);
		beChannel = beConnectFuture.channel();
		LOGGER.debug("A new back-end channel {} was created and was associated with front-end {}", beChannel, feChannel);
		beConnectFuture.addListener((final ChannelFuture future) -> {
			if (future.isSuccess()) {
				LOGGER.debug("Back-end {} was connected", beChannel);
				feChannel.read();
			} else {
				feChannel.close();
				final Throwable cause = future.cause();
				final String internalMsg = Message.format("Can't connect to back-end %s", beAddress);
				if (cause instanceof ConnectTimeoutException) {
					throw new TimeoutException(internalMsg, cause, "TCP connect timeout");
				} else {
					throw new ApplicationException(internalMsg, cause);
				}
			}
		});
	}

	@Override
	public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		final Channel feChannel = ctx.channel();
		if (beChannel.isActive()) {//can't be null because feChannel.read() is only called if beChannel successfully initialized
			LOGGER.debug("Writing {} from front-end {} to back-end {}", msg, feChannel, beChannel);
			beChannel.writeAndFlush(msg).addListener((final ChannelFuture future) -> {
				if (future.isSuccess()) {
					feChannel.read();
				} else {
					beChannel.close();
					throw new ApplicationException(
							Message.format("Can't write and flush to back-end %s", beChannel), future.cause());
				}
			});
		} else {
			LOGGER.debug("Data {} from front-end {} was ignored because back-end {} isn't active", msg, feChannel, beChannel);
		}
	}

	@Override
	public final void channelInactive(final ChannelHandlerContext ctx) {
		if (beChannel != null && beChannel.isActive()) {//flush beChannel and close
			beChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener((final ChannelFuture future) -> beChannel.close());
		}
		ctx.fireChannelInactive();
	}

	@Override
	public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable e) {
		try {
			LOGGER.error("Exception caught", e);
		} finally {
			final Channel channel = ctx.channel();
			channel.close();
		}
	}

	@Override
	public final void close(final ChannelHandlerContext ctx, final ChannelPromise promise) {
		final Channel channel = ctx.channel();
		ctx.close();
		LOGGER.debug("{} was closed", channel);
	}
}
