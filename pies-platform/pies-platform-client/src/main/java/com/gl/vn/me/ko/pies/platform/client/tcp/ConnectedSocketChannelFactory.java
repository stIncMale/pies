package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.gl.vn.me.ko.pies.base.throwable.TimeoutException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.socket.SocketChannel;
import java.net.InetSocketAddress;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A factory that creates new connected {@link SocketChannel}s for {@link TcpSequentialClient}.
 */
@ThreadSafe
final class ConnectedSocketChannelFactory {
	/*
	 * It's not documented, but according to implementation Bootstrap seems to be a thread-safe class.
	 */
	private final Bootstrap bootstrap;
	private final InetSocketAddress address;

	/**
	 * Constructs a new instance of {@link ConnectedSocketChannelFactory}.
	 *
	 * @param bootstrap
	 * {@link Bootstrap} that represents {@link TcpSequentialClient} configuration.
	 * @param address
	 * {@link InetSocketAddress} to connect new {@link SocketChannel} to.
	 */
	ConnectedSocketChannelFactory(final Bootstrap bootstrap, final InetSocketAddress address) {
		checkNotNull(bootstrap, Message.ARGUMENT_NULL, "first", "bootstrap");
		checkNotNull(address, Message.ARGUMENT_NULL, "second", "address");
		this.address = address;
		this.bootstrap = bootstrap;
	}

	/**
	 * Creates a new {@link SocketChannel}
	 * that is connected to {@link InetSocketAddress} used to construct {@link ConnectedSocketChannelFactory}.
	 *
	 * @return
	 * @throws InterruptedException
	 * If the current {@link Thread} is interrupted.
	 * @throws ApplicationException
	 * If a new TCP connection can't be established.
	 */
	final SocketChannel get() throws InterruptedException, ApplicationException {
		final SocketChannel result;
		final ChannelFuture connectFuture = bootstrap.connect(address);
		connectFuture.await();
		if (connectFuture.isSuccess()) {
			result = (SocketChannel)connectFuture.channel();
		} else {
			final Throwable cause = connectFuture.cause();
			final String internalMsg = Message.format("Can't connect to %s", address);
			if (cause instanceof ConnectTimeoutException) {
				throw new TimeoutException(internalMsg, cause, "TCP connect timeout");
			} else {
				throw new ApplicationException(internalMsg, cause);
			}
		}
		return result;
	}
}
