package com.gl.vn.me.ko.pies.platform.server.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL_SINGLE;
import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles events on back-end {@link Channel}s.
 */
final class TcpReverseProxyBackEndChannelHandler extends ChannelHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpReverseProxyBackEndChannelHandler.class);

	private final Channel feChannel;

	/**
	 * Constructs a new instance of {@link TcpReverseProxyBackEndChannelHandler}.
	 *
	 * @param feChannel
	 * Front-end {@link Channel} this back-end handler is associated with.
	 */
	TcpReverseProxyBackEndChannelHandler(final Channel feChannel) {
		checkNotNull(feChannel, ARGUMENT_NULL_SINGLE, "feChannel");
		this.feChannel = feChannel;
	}

	@Override
	public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		LOGGER.debug("Writing {} to front-end {}", msg, feChannel);
		feChannel.writeAndFlush(msg).addListener((final ChannelFuture future) -> {
			if (!future.isSuccess()) {
				feChannel.close();
				throw new ApplicationException(
						Message.format("Can't write and flush to front-end %s", feChannel), future.cause());
			}
		});
	}

	@Override
	public final void channelInactive(final ChannelHandlerContext ctx) {
		if (feChannel.isActive()) {//flush feChannel and close
			feChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener((final ChannelFuture future) -> feChannel.close());
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
