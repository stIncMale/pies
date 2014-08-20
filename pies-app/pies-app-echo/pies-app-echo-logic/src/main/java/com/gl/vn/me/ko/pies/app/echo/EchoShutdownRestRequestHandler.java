package com.gl.vn.me.ko.pies.app.echo;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.platform.server.Server;
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
 * This {@link RestRequestHandler} is intended to perform shutdown of all {@link Server}s.
 * Firstly it shuts down Echo Server and then responds with {@link HttpResponseStatus#ACCEPTED} before shutting down the
 * Control Server it belongs to.
 * <p>
 * Example of HTTP response content:
 * <pre><code>
 * {
 * 	"httpReasonPhrase": "Echo Server was stopped. Shutdown command for Control Server is accepted",
 * 	"echoServer": "Echo Server(address=localhost/127.0.0.1:7000, active=false)"
 * }
 * </code></pre>
 * Note that value for name {@code echoServer} is just descriptive and MAY be changed.
 * This {@link RestRequestHandler} is bound to {@code "PUT /shutdown/"}.
 */
final class EchoShutdownRestRequestHandler extends RestRequestHandler<JsonRestRequestHandlerResult> {
	private static final Logger LOGGER = LoggerFactory.getLogger(EchoShutdownRestRequestHandler.class);
	private static final String JSON_RESPONSE_ECHO_SERVER_NVNAME = "echoServer";
	private final Server echoServer;
	private final JsonBuilderFactory jsonBuilderFactory;

	/**
	 * Constructs a new instance of {@link EchoShutdownRestRequestHandler}.
	 *
	 * @param echoServer
	 * A {@link Server} that this {@link RestRequestHandler} will shut down.
	 * @param jsonBuilderFactory
	 * An implementation of {@link JsonBuilderFactory}.
	 */
	EchoShutdownRestRequestHandler(final Server echoServer, final JsonBuilderFactory jsonBuilderFactory) {
		super(RestRequest.valueOf("PUT /shutdown/"));
		checkNotNull(echoServer, Message.ARGUMENT_NULL, "first", "echoServer");
		checkNotNull(jsonBuilderFactory, Message.ARGUMENT_NULL, "second", "jsonBuilderFactory");
		this.echoServer = echoServer;
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
				echoServer.shutdown();
				final JsonObjectBuilder jsonBuilder = jsonBuilderFactory.createObjectBuilder();
				jsonBuilder.add(JsonRestServer.JSON_RESPONSE_REASON_PHRASE_NVNAME,
						"Echo Server was stopped. Shutdown command for Control Server is accepted");
				jsonBuilder.add(JSON_RESPONSE_ECHO_SERVER_NVNAME, echoServer.toString());
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
	 * Shuts down {@link Server} that was specified to construct this {@link EchoShutdownRestRequestHandler}.
	 */
	@Override
	protected final void shutdownHook() {
		echoServer.shutdown();
	}
}
