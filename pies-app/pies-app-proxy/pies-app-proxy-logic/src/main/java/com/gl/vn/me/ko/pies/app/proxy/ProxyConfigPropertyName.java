package com.gl.vn.me.ko.pies.app.proxy;

import com.gl.vn.me.ko.pies.base.config.NoSuchPropertyException;
import com.gl.vn.me.ko.pies.base.config.PropertyName;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandlerResult;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents names of properties that are specified by the Proxy Config.
 * <p>
 * Property MUST be considered mandatory unless otherwise is specified.
 */
@Immutable
public enum ProxyConfigPropertyName implements PropertyName {
	/**
	 * This property specifies Internet address of a network interface the server uses to listen for incoming requests.
	 * <p>
	 * Optional property. If the property isn't specified the value is determined automatically.
	 * <p>
	 * Name of this property is {@code "proxyServer.frontEnd.socket.host"}.
	 */
	PROXY_FE_HOST("proxyServer.frontEnd.socket.host", true),
	/**
	 * This property specifies TCP port the server uses to listen for incoming requests.
	 * <p>
	 * Name of this property is {@code "proxyServer.frontEnd.socket.port"}.
	 */
	PROXY_FE_PORT("proxyServer.frontEnd.socket.port", false),
	/**
	 * This property specifies Internet address of a network interface the server uses to forward incoming requests.
	 * <p>
	 * Name of this property is {@code "proxyServer.backEnd.socket.host"}.
	 */
	PROXY_BE_HOST("proxyServer.backEnd.socket.host", false),
	/**
	 * This property specifies TCP port the server uses to forward incoming requests.
	 * <p>
	 * Name of this property is {@code "proxyServer.backEnd.socket.port"}.
	 */
	PROXY_BE_PORT("proxyServer.backEnd.socket.port", false),
	/**
	 * This property specifies maximum number of threads that accept new TCP connections.
	 * <p>
	 * Name of this property is {@code "proxyServer.threads.acceptors"}.
	 */
	PROXY_ACCEPTORS("proxyServer.threads.acceptors", false),
	/**
	 * This property specifies maximum number of threads that process data received via the accepted TCP connections.
	 * <p>
	 * Name of this property is {@code "proxyServer.threads.workers"}.
	 */
	PROXY_WORKERS("proxyServer.threads.workers", false),
	/**
	 * This property specifies amount of time in milliseconds to wait for completion of I/O operations.
	 * E.g. wait for connect to back-end.
	 * Value of this property MUST be positive.
	 * <p>
	 * Name of this property is {@code "proxyServer.timeouts.ioTimeoutMillis"}.
	 */
	PROXY_IO_TIMEOUT_MILLIS("proxyServer.timeouts.ioTimeoutMillis", false),
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

	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigPropertyName.class);

	/**
	 * Checks if the provided {@link PropsConfig} defines all {@link #isOptional() mandatory} properties
	 * specified in {@link ProxyConfigPropertyName}.
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
		for (final ProxyConfigPropertyName propertyName : ProxyConfigPropertyName.values()) {
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

	private ProxyConfigPropertyName(final String name, final boolean optional) {
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
