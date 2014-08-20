package com.gl.vn.me.ko.pies.app.echo;

import com.gl.vn.me.ko.pies.base.config.NoSuchPropertyException;
import com.gl.vn.me.ko.pies.base.config.PropertyName;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandlerResult;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents names of properties that are specified by the Echo Config.
 * <p>
 * Property MUST be considered mandatory unless otherwise is specified.
 */
@Immutable
public enum EchoConfigPropertyName implements PropertyName {
	/**
	 * This property specifies Internet address of a network interface the server uses to listen for echo requests.
	 * <p>
	 * Optional property. If the property isn't specified the value is determined automatically.
	 * <p>
	 * Name of this property is {@code "echoServer.socket.host"}.
	 */
	ECHO_HOST("echoServer.socket.host", true),
	/**
	 * This property specifies TCP port the server uses to listen for echo requests.
	 * <p>
	 * Name of this property is {@code "echoServer.socket.port"}.
	 */
	ECHO_PORT("echoServer.socket.port", false),
	/**
	 * This property specifies maximum number of threads that accept new TCP connections.
	 * <p>
	 * Name of this property is {@code "echoServer.threads.acceptors"}.
	 */
	ECHO_ACCEPTORS("echoServer.threads.acceptors", false),
	/**
	 * This property specifies maximum number of threads that process data received via the accepted TCP connections.
	 * <p>
	 * Name of this property is {@code "echoServer.threads.workers"}.
	 */
	ECHO_WORKERS("echoServer.threads.workers", false),
	/**
	 * This property specifies Internet address of a network interface the server uses to listen for control requests (REST).
	 * <p>
	 * Optional property. If the property isn't specified the value is determined automatically.
	 * <p>
	 * Name of this property is {@code "controlServer.socket.host"}.
	 */
	CONTROL_HOST("controlServer.socket.host", true),
	/**
	 * This property specifies TCP port the server uses to listen for control requests.
	 * <p>
	 * Name of this property is {@code "controlServer.socket.port"}.
	 */
	CONTROL_PORT("controlServer.socket.port", false),
	/**
	 * This property specifies maximum number of threads that accept new TCP connections.
	 * <p>
	 * Name of this property is {@code "controlServer.threads.acceptors"}.
	 */
	CONTROL_ACCEPTORS("controlServer.threads.acceptors", false),
	/**
	 * This property specifies maximum number of threads that process data received via the accepted TCP connections.
	 * <p>
	 * Name of this property is {@code "controlServer.threads.workers"}.
	 */
	CONTROL_WORKERS("controlServer.threads.workers", false),
	/**
	 * Maximum number of threads that perform {@linkplain RestRequestHandlerResult#getPostResponseAction() post-response actions}.
	 * <p>
	 * Name of this property is {@code "controlServer.threads.postResponseWorkers"}.
	 */
	CONTROL_POST_RESPONSE_WORKERS("controlServer.threads.postResponseWorkers", false);

	private static final Logger LOGGER = LoggerFactory.getLogger(EchoConfigPropertyName.class);

	/**
	 * Checks if the provided {@link PropsConfig} defines all {@link #isOptional() mandatory} properties
	 * specified in {@link EchoConfigPropertyName}.
	 *
	 * @param config
	 * {@link PropsConfig} to validate.
	 * @param configFileName
	 * Name of the file from which the {@code config} was loaded.
	 * This name is used to log information about properties defined by the {@code config}.
	 * @throws NoSuchPropertyException
	 * If some {@link #isOptional() mandatory} property isn't defined by {@code config}.
	 */
	static final void validate(final PropsConfig config, final String configFileName) throws NoSuchPropertyException {
		final String messageFormat = "The following property was read from the config {}: {}={}";
		for (final EchoConfigPropertyName propertyName : EchoConfigPropertyName.values()) {
			if (propertyName.isOptional()) {
				final Optional<String> optPropertyValue = config.getString(propertyName, null);
				if (optPropertyValue.isPresent()) {
					LOGGER.info(messageFormat, configFileName, propertyName, optPropertyValue.get());
				}
			} else {
				LOGGER.info(messageFormat, configFileName, propertyName, config.getString(propertyName));
			}
		}
	}

	private final String name;
	private final boolean optional;

	private EchoConfigPropertyName(final String name, final boolean optional) {
		this.name = name;
		this.optional = optional;
	}

	/**
	 * Specifies whether the property is optional or not.
	 *
	 * @return
	 * {@code true} if the property is optional, {@code false} otherwise.
	 */
	final boolean isOptional() {
		return optional;
	}

	@Override
	public final String toString() {
		return name;
	}
}
