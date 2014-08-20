package com.gl.vn.me.ko.pies.app.echo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChannelHandler} that responds with the same bytes that were read.
 */
@Sharable
final class EchoChannelHandler extends ChannelHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(EchoChannelHandler.class);

	/**
	 * Constructs a new instance of {@link EchoChannelHandler}.
	 */
	@Inject
	private EchoChannelHandler() {
	}

	@Override
	public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		ctx.writeAndFlush(msg);
	}

	@Override
	public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
		try {
			LOGGER.error("Exception caught", cause);
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
