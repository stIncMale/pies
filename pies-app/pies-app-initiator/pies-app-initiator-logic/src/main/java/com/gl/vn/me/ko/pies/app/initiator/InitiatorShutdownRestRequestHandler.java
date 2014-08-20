package com.gl.vn.me.ko.pies.app.initiator;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.platform.client.Client;
import com.gl.vn.me.ko.pies.platform.server.rest.BadRestRequestException;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestRequestHandlerResult;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestServer;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequest;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandler;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandlingException;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link RestRequestHandler} is intended to perform shutdown of a Control Server.
 * The {@link RestRequestHandler} responds with {@link HttpResponseStatus#ACCEPTED}
 * before shutting down the Control Server it belongs to.
 * <p>
 * Example of HTTP response content:
 * <pre><code>
 * {
 * 	"httpReasonPhrase": "Initiator Client was stopped. Shutdown command for Control Server is accepted",
 * 	"initiatorClient": "Initiator Client(address=localhost/127.0.0.1:7000, active=false)"
 * }
 * </code></pre>
 * This {@link RestRequestHandler} is bound to {@code "PUT /shutdown/"}
 * and doesn't expect any {@link #getArguments(RestRequest) arguments}.
 */
final class InitiatorShutdownRestRequestHandler extends RestRequestHandler<JsonRestRequestHandlerResult> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorShutdownRestRequestHandler.class);
	private static final String JSON_RESPONSE_INITIATOR_CLIENT_NVNAME = "initiatorClient";
	private final Client<?, ?> initiatorClient;
	private final JsonBuilderFactory jsonBuilderFactory;

	/**
	 * Constructs a new instance of {@link InitiatorShutdownRestRequestHandler}.
	 *
	 * @param initiatorClient
	 * A {@link Client} that this {@link RestRequestHandler} will shut down.
	 * @param jsonBuilderFactory
	 * An implementation of {@link JsonBuilderFactory}.
	 */
	InitiatorShutdownRestRequestHandler(final Client<?, ?> initiatorClient, final JsonBuilderFactory jsonBuilderFactory) {
		super(RestRequest.valueOf("PUT /shutdown/"));
		checkNotNull(initiatorClient, Message.ARGUMENT_NULL, "first", "clientToShutdown");
		checkNotNull(jsonBuilderFactory, Message.ARGUMENT_NULL, "second", "jsonBuilderFactory");
		this.initiatorClient = initiatorClient;
		this.jsonBuilderFactory = jsonBuilderFactory;
	}

	@Override
	public final CompletionStage<JsonRestRequestHandlerResult> handleRequest(
			final RestRequest request, final ExecutorService executorService) {
		return CompletableFuture.supplyAsync(() -> {
			final JsonRestRequestHandlerResult result;
			try {
				LOGGER.debug("Handling request {}", request);
				if (!getArguments(request).isEmpty()) {
					throw new BadRestRequestException(
							Message.format("Request %s... must not specify any arguments", getBinding()));
				}
				initiatorClient.shutdown();
				final JsonObjectBuilder jsonBuilder = jsonBuilderFactory.createObjectBuilder();
				jsonBuilder.add(JsonRestServer.JSON_RESPONSE_REASON_PHRASE_NVNAME,
						"Initiator Client was stopped. Shutdown command for Control Server is accepted");
				jsonBuilder.add(JSON_RESPONSE_INITIATOR_CLIENT_NVNAME, initiatorClient.toString());
				final JsonObject httpResponseContent = jsonBuilder.build();
				/*
				 * getServer().shutdown() will also shut down ExecutorService in which action is executed
				 */
				result = new JsonRestRequestHandlerResult(
						HttpResponseStatus.ACCEPTED, httpResponseContent, "en", getServer()::shutdown);
			} catch (final RestRequestHandlingException | BadRestRequestException e) {
				throw e;
			} catch (final Exception e) {
				throw new RestRequestHandlingException(e);
			}
			return result;
		}, executorService);
	}

	/**
	 * Shuts down {@link Client} that was specified to construct this {@link InitiatorShutdownRestRequestHandler}.
	 */
	@Override
	protected final void shutdownHook() {
		initiatorClient.shutdown();
	}
}
