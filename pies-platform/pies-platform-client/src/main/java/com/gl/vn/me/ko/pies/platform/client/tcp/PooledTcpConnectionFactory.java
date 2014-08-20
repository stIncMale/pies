package com.gl.vn.me.ko.pies.platform.client.tcp;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_NULL_SINGLE;
import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import io.netty.channel.socket.SocketChannel;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that defines life-cycle methods for {@link TcpConnection}s to be served by {@link ObjectPool}.
 *
 * @param <Message>
 * A type of message contained by {@link TcpMessage}.
 * @param <Response>
 * A type of response contained by {@link TcpResponse}.
 */
@ThreadSafe
final class PooledTcpConnectionFactory<Message, Response>
		extends BasePooledObjectFactory<TcpConnection<Message, Response>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PooledTcpConnectionFactory.class);
	private final ConnectedSocketChannelFactory channelFactory;

	/**
	 * Constructs a new instance of {@link PooledTcpConnectionFactory}.
	 *
	 * @param channelFactory
	 * {@link ConnectedSocketChannelFactory} that will be used to create new connected {@link SocketChannel}s.
	 */
	PooledTcpConnectionFactory(final ConnectedSocketChannelFactory channelFactory) {
		checkNotNull(channelFactory, ARGUMENT_NULL_SINGLE, "channelFactory");
		this.channelFactory = channelFactory;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws InterruptedException
	 * If the current {@link Thread} is interrupted.
	 * @throws ApplicationException
	 * If a new {@link TcpConnection} can't be created.
	 */
	@Override
	public final TcpConnection<Message, Response> create() throws InterruptedException, ApplicationException {
		final TcpConnection<Message, Response> result = new TcpConnection<>(channelFactory.get());
		LOGGER.debug("A new {} was created", result);
		return result;
	}

	@Override
	public final PooledObject<TcpConnection<Message, Response>> wrap(TcpConnection<Message, Response> connection) {
		return new DefaultPooledObject<>(connection);
	}

	/**
	 * Ensures that the {@code pooledConnection} is safe to be returned by the pool.
	 *
	 * @param pooledConnection
	 * {@link PooledObject} wrapping the instance to be validated.
	 * @return
	 * {@code false} if {@code pooledConnection} isn't valid and should be dropped from the pool, {@code true} otherwise.
	 */
	@Override
	public final boolean validateObject(final PooledObject<TcpConnection<Message, Response>> pooledConnection) {
		final TcpConnection<Message, Response> connection = pooledConnection.getObject();
		final boolean result = connection.isActive();
		if (!result) {
			LOGGER.debug("{} isn't active", connection);
		}
		return result;
	}

	/**
	 * Destroys an instance no longer needed by the pool.
	 *
	 * @param pooledConnection
	 * {@link PooledObject} wrapping the instance to be destroyed.
	 */
	@Override
	public final void destroyObject(PooledObject<TcpConnection<Message, Response>> pooledConnection) {
		final TcpConnection<Message, Response> connection = pooledConnection.getObject();
		connection.close();
		LOGGER.debug("{} was closed", connection);
	}
}
