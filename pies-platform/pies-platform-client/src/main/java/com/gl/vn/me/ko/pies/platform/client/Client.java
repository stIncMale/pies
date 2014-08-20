package com.gl.vn.me.ko.pies.platform.client;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents an I/O client.
 *
 * @param <Message>
 * A type of message that {@link Client} is able to send.
 * @param <Response>
 * A type of response that {@link Client} returns.
 */
@ThreadSafe
public interface Client<Message, Response> {
	/**
	 * Sends the supplied {@code message}.
	 *
	 * @param message
	 * Message to send.
	 * @return
	 * A {@link CompletionStage} that represents asynchronous {@link Optional} result of the method.
	 * Implementation MUST specify when {@link Optional} {@linkplain Optional#isPresent() is present} and when it is not.
	 */
	CompletionStage<Optional<Response>> send(Message message);

	/**
	 * Performs a shutdown procedure.
	 * Once this method is invoked {@link Client} can't {@linkplain #send(Object) send} data anymore.
	 * This method MUST be idempotent.
	 */
	void shutdown();
}
