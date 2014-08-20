package com.gl.vn.me.ko.pies.app.initiator;

import com.gl.vn.me.ko.pies.base.config.NoSuchPropertyException;
import com.gl.vn.me.ko.pies.base.config.PropertyName;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandlerResult;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents names of properties that are specified by the Initiator Config.
 * <p>
 * Property MUST be considered mandatory unless otherwise is specified.
 */
@Immutable
public enum InitiatorConfigPropertyName implements PropertyName {
	/**
	 * This property specifies Internet address of a network interface where to send echo requests.
	 * <p>
	 * Name of this property is {@code "initiatorClient.socket.host"}.
	 */
	INITIATOR_CLIENT_HOST("initiatorClient.socket.host", false),
	/**
	 * This property specifies TCP port where to send echo requests.
	 * <p>
	 * Name of this property is {@code "initiatorClient.socket.port"}.
	 */
	INITIATOR_CLIENT_PORT("initiatorClient.socket.port", false),
	/**
	 * This property specifies maximum number of threads that process data sent/received via TCP connections.
	 * <p>
	 * Name of this property is {@code "initiatorClient.threads.workers"}.
	 */
	INITIATOR_CLIENT_WORKERS("initiatorClient.threads.workers", false),
	/**
	 * This property specifies amount of time in milliseconds to wait for completion of I/O operations.
	 * E.g. wait for connect, or wait for echo response.
	 * Value of this property MUST be positive.
	 * <p>
	 * Name of this property is {@code "initiatorClient.timeouts.ioTimeoutMillis"}.
	 */
	INITIATOR_CLIENT_IO_TIMEOUT_MILLIS("initiatorClient.timeouts.ioTimeoutMillis", false),
	/**
	 * This property specifies if echo response will be validated, i.e. that received data are equal to sent.
	 * <p>
	 * Name of this property is {@code "initiatorClient.validateResponse"}.
	 */
	INITIATOR_CLIENT_VALIDATE_RESPONSE("initiatorClient.validateResponse", false),
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

	private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorConfigPropertyName.class);

	/**
	 * Checks if the provided {@link PropsConfig} defines all {@link #isOptional() mandatory} properties
	 * specified in {@link InitiatorConfigPropertyName}.
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
		for (final InitiatorConfigPropertyName propertyName : InitiatorConfigPropertyName.values()) {
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

	private InitiatorConfigPropertyName(final String name, final boolean optional) {
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
