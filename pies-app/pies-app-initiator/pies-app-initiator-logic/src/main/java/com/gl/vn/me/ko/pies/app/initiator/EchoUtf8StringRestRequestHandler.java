package com.gl.vn.me.ko.pies.app.initiator;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_ILLEGAL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.TRUE;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ExternallyVisibleException;
import com.gl.vn.me.ko.pies.platform.client.tcp.TcpMessage;
import com.gl.vn.me.ko.pies.platform.client.tcp.TcpResponse;
import com.gl.vn.me.ko.pies.platform.client.tcp.TcpSequentialClient;
import com.gl.vn.me.ko.pies.platform.server.rest.BadRestRequestException;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestRequestHandlerResult;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestServer;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequest;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandler;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandlingException;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link RestRequestHandler} sends {@link String#getBytes(Charset) binary data} of a UTF-8 {@link String}
 * which is the only expected {@link #getArguments(RestRequest) argument} of a {@link RestRequest}.
 * The {@link RestRequestHandler} responds with {@link HttpResponseStatus#OK} if handling was completed successfully.
 * <p>
 * Example of HTTP response content:
 * <pre><code>
 * {
 * 	"httpReasonPhrase": "Echo completed"
 * }
 * </code></pre>
 * This {@link RestRequestHandler} is bound to {@code "POST /utf8string/"}.
 */
final class EchoUtf8StringRestRequestHandler extends RestRequestHandler<JsonRestRequestHandlerResult> {
	private static final Logger LOGGER = LoggerFactory.getLogger(EchoUtf8StringRestRequestHandler.class);
	private final TcpSequentialClient<byte[], byte[]> echoClient;
	private final boolean validateResponse;
	private final long echoResponseTimeoutMillis;
	private final JsonBuilderFactory jsonBuilderFactory;

	/**
	 * Constructs a new instance of {@link InitiateUtf8StringRestRequestHandler}.
	 *
	 * @param echoClient
	 * A {@link TcpSequentialClient} that will be used to send/receive echo messages/responses.
	 * {@link InitiateUtf8StringRestRequestHandler} performs shutdown of {@code echoClient}
	 * once {@link #shutdownHook()} is invoked.
	 * @param validateResponse
	 * Specifies if echo response MUST be validated, i.e. that received data are equal to sent.
	 * @param echoResponseTimeoutMillis
	 * Amount of time in milliseconds to wait for response after sending an echo message.
	 * This argument MUST be positive.
	 * @param jsonBuilderFactory
	 * An implementation of {@link JsonBuilderFactory}.
	 */
	EchoUtf8StringRestRequestHandler(
			final TcpSequentialClient<byte[], byte[]> echoClient,
			final boolean validateResponse,
			final long echoResponseTimeoutMillis,
			final JsonBuilderFactory jsonBuilderFactory) {
		super(RestRequest.valueOf("POST /utf8string/"));
		checkNotNull(echoClient, Message.ARGUMENT_NULL, "first", "echoClient");
		checkArgument(echoResponseTimeoutMillis > 0, ARGUMENT_ILLEGAL, echoResponseTimeoutMillis,
				"third", "echoResponseTimeoutMillis", "Expected value must be positive");
		checkNotNull(jsonBuilderFactory, Message.ARGUMENT_NULL, "fourth", "jsonBuilderFactory");
		this.echoClient = echoClient;
		this.validateResponse = validateResponse;
		this.echoResponseTimeoutMillis = echoResponseTimeoutMillis;
		this.jsonBuilderFactory = jsonBuilderFactory;
	}

	/**
	 * Sends {bytesToSend} via {@link #echoClient} and validates echo response if {@link validateResponse} is {@code true}.
	 *
	 * @param bytesToSend
	 * Bytes to send via {@link #echoClient}.
	 * @param executorService
	 * {@link ExecutorService} to use to handle result of the {@link TcpSequentialClient#send(TcpMessage)} method.
	 * @return
	 * {@link CompletionStage} that represents success ({@code true}) or failure ({@code false})
	 * of validation of echo response.
	 */
	private final CompletionStage<Boolean> echo(final byte[] bytesToSend, final ExecutorService executorService) {
		final CompletionStage<Optional<TcpResponse<byte[]>>> echoResponse
				= echoClient.send(new TcpMessage<>(bytesToSend, echoResponseTimeoutMillis));
		return echoResponse.thenApplyAsync((response) -> {
			final TcpResponse<byte[]> tcpResponse = response.get();
			final byte[] respondedBytes = tcpResponse.get();
			final Boolean result = validateResponse ? Arrays.equals(bytesToSend, respondedBytes) : TRUE;
			if (!result) {
				tcpResponse.abort();
			}
			return result;
		}, executorService);
	}

	/**
	 * Extracts and validates {@code request} arguments.
	 *
	 * @param request
	 * {@link RestRequest} from which to get arguments.
	 * @return
	 * Validated {@code request} arguments.
	 * @throws BadRestRequestException
	 * If arguments are invalid.
	 */
	private final List<String> getValidArguments(final RestRequest request) throws BadRestRequestException {
		final List<String> requestParams = getArguments(request);
		if (requestParams.size() != 1) {
			throw new BadRestRequestException(Message.format("Request %s... must specify exactly one argument", getBinding()));
		}
		return requestParams;
	}

	@Override
	public final CompletionStage<JsonRestRequestHandlerResult> handleRequest(
			final RestRequest request, final ExecutorService executorService) {
		CompletionStage<JsonRestRequestHandlerResult> result;
		try {
			LOGGER.debug("Handling request {}", request);
			final List<String> requestArguments = getValidArguments(request);
			final String stringToSend = requestArguments.get(0);
			final byte[] bytesToSend = stringToSend.getBytes(StandardCharsets.UTF_8);
			final CompletionStage<Boolean> echoSuccessCompletionStage = echo(bytesToSend, executorService);
			result = echoSuccessCompletionStage.thenApplyAsync((echoSuccess) -> {
				final JsonRestRequestHandlerResult restResult;
				if (echoSuccess.booleanValue()) {
					restResult = createSuccessResult();
				} else {
					throw new ExternallyVisibleException("Echo response is invalid");
				}
				return restResult;
			}, executorService);
		} catch (final RestRequestHandlingException | BadRestRequestException e) {
			final CompletableFuture<JsonRestRequestHandlerResult> completedExceptionally = new CompletableFuture<>();
			completedExceptionally.completeExceptionally(e);
			result = completedExceptionally;
		} catch (final Exception e) {
			final CompletableFuture<JsonRestRequestHandlerResult> completedExceptionally = new CompletableFuture<>();
			completedExceptionally.completeExceptionally(new RestRequestHandlingException(e));
			result = completedExceptionally;
		}
		return result;
	}

	private final JsonRestRequestHandlerResult createSuccessResult() {
		final JsonObjectBuilder jsonBuilder = jsonBuilderFactory.createObjectBuilder();
		jsonBuilder.add(JsonRestServer.JSON_RESPONSE_REASON_PHRASE_NVNAME, "Echo completed");
		final JsonObject httpResponseContent = jsonBuilder.build();
		return new JsonRestRequestHandlerResult(HttpResponseStatus.OK, httpResponseContent, "en", null);
	}

	/**
	 * Shuts down {@link TcpSequentialClient} that was specified to construct this {@link EchoUtf8StringRestRequestHandler}.
	 */
	@Override
	protected final void shutdownHook() {
		echoClient.shutdown();
	}
}
