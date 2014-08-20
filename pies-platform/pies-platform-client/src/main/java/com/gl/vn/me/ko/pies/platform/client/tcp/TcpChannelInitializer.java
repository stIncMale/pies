package com.gl.vn.me.ko.pies.platform.client.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Performs initialization of new {@link SocketChannel}s created by {@link TcpSequentialClient}.
 * <p>
 * This interface is similar to {@link ChannelInitializer} class
 * except it doesn't implement {@link ChannelHandler}
 * and the method {@link #initChannel(SocketChannel)} is accessible from {@link TcpSequentialClient}.
 */
@FunctionalInterface
public interface TcpChannelInitializer {
	/**
	 * This method will be called once the {@link Channel} was registered.
	 *
	 * @param channel
	 * The {@link SocketChannel} which was registered.
	 * @throws Exception
	 * If an {@link Exception} occurs. In that case the {@code channel} will be closed.
	 */
	void initChannel(SocketChannel channel) throws Exception;
}
