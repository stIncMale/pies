package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import com.gl.vn.me.ko.pies.base.constant.Constant;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.feijoa.ExecutorUtil;
import com.gl.vn.me.ko.pies.base.feijoa.ThrowableUtil;
import com.gl.vn.me.ko.pies.base.throwable.ExternallyVisibleException;
import com.gl.vn.me.ko.pies.base.throwable.TimeoutException;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpServer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A REST HTTP over TCP implementation of the {@link Server} interface.
 * <p>
 * The {@link RestServer} responds with HTTP responses of version {@link HttpVersion#HTTP_1_1}
 * and the following
 * <a href="http://tools.ietf.org/html/rfc2616#section-6.1.1">HTTP status codes</a>:
 * <ul>
 * <li><a href="http://tools.ietf.org/html/rfc2616#section-10.1.1">100 Continue</a> if HTTP request contains
 * {@code "Expect: 100-continue"} header.</li>
 * <li><a href="http://tools.ietf.org/html/rfc2616#section-10.4.1">400 Bad Request</a> if HTTP request doesn't represent
 * a correct {@link RestRequest}, if there is no suitable {@link RestRequestHandler} to handle a request, if content of
 * HTTP request can't be correctly decoded, if {@link BindingNotFoundException} was thrown while handling a {@link RestRequest},
 * if {@link BadRestRequestException} was thrown while handling a {@link RestRequest} (this part MAY be changed by overriding
 * {@link #handleException(Throwable, HttpRequest)} method).</li>
 * <li><a href="http://tools.ietf.org/html/rfc2616#section-10.4.1">408 Request Timeout</a> if {@link TimeoutException}
 * was thrown while handling a {@link RestRequest}
 * (MAY be changed by overriding {@link #handleException(Throwable, HttpRequest)} method)</li>
 * <li><a href="http://tools.ietf.org/html/rfc2616#section-10.5.1">500 Internal Server Error</a> if any other unhandled
 * {@link Throwable} was thrown while handling an HTTP request
 * (MAY be changed by overriding {@link #handleException(Throwable, HttpRequest)} method),
 * e.g. if {@link RestRequestHandler}
 * throws an {@link RestRequestHandlingException} while handling a {@link RestRequest}.</li>
 * <li>Any other status code specified by {@link RestRequestHandlerResult}.</li>
 * </ul>
 *
 * @param <T>
 * Type of {@link RestRequestHandlerResult} applicable for the {@link RestServer}.
 */
@ThreadSafe
public class RestServer<T extends RestRequestHandlerResult> extends TcpServer {
	private static final class WorkerSocketChannelinitializer extends ChannelInitializer<SocketChannel> {
		private final ServiceHttpResponseConstructor serviceHttpResponseConstructor;
		private final ExceptionHandler exceptionHandler;
		private final RestChannelHandler restChannelHandler;

		private WorkerSocketChannelinitializer(
				final RestRequestDispatcher<? extends RestRequestHandlerResult> restDispatcher,
				final ExecutorService postResponseBlockingExecutorService,
				final ServiceHttpResponseConstructor serviceHttpResponseConstructor,
				final ExceptionHandler exceptionHandler) {
			this.serviceHttpResponseConstructor = serviceHttpResponseConstructor;
			this.exceptionHandler = exceptionHandler;
			restChannelHandler = new RestChannelHandler(restDispatcher, postResponseBlockingExecutorService,
					serviceHttpResponseConstructor, exceptionHandler);
		}

		@Override
		protected final void initChannel(final SocketChannel channel) {
			final ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast(
					new HttpServerCodec(),
					new RestDecoder(serviceHttpResponseConstructor, exceptionHandler),
					restChannelHandler);
		}
	}

	/**
	 * See {@link RestServer#createServiceHttpResponse(HttpResponseStatus, String)}.
	 */
	@ThreadSafe
	@FunctionalInterface
	static interface ServiceHttpResponseConstructor {
		/**
		 * See {@link RestServer#createServiceHttpResponse(HttpResponseStatus, String)}.
		 *
		 * @param httpResponseStatus
		 * See {@link RestServer#createServiceHttpResponse(HttpResponseStatus, String)}.
		 * @param httpReasonPhrase
		 * See {@link RestServer#createServiceHttpResponse(HttpResponseStatus, String)}.
		 * @return
		 * See {@link RestServer#createServiceHttpResponse(HttpResponseStatus, String)}.
		 */
		FullHttpResponse create(HttpResponseStatus httpResponseStatus, @Nullable String httpReasonPhrase);
	}

	/**
	 * See {@link RestServer#handleException(Throwable, HttpRequest)}.
	 */
	@ThreadSafe
	@FunctionalInterface
	static interface ExceptionHandler {
		/**
		 * See {@link RestServer#handleException(Throwable, HttpRequest)}.
		 *
		 * @param e
		 * See {@link RestServer#handleException(Throwable, HttpRequest)}.
		 * @param httpRequest
		 * See {@link RestServer#handleException(Throwable, HttpRequest)}.
		 * @return
		 * See {@link RestServer#handleException(Throwable, HttpRequest)}.
		 */
		FullHttpResponse handle(Throwable e, @Nullable HttpRequest httpRequest);
	}
	private static final Logger LOGGER = LoggerFactory.getLogger(RestServer.class);
	static final HttpResponseStatus HTTP_RESPONSE_STATUS_CONTINUE = HttpResponseStatus.CONTINUE;
	static final HttpResponseStatus HTTP_RESPONSE_STATUS_BAD_REQUEST = HttpResponseStatus.BAD_REQUEST;
	private static final HttpResponseStatus HTTP_RESPONSE_STATUS_INTERNAL_SERVER_ERROR
			= HttpResponseStatus.INTERNAL_SERVER_ERROR;
	private final ExecutorService postResponseBlockingExecutorService;
	private final RestRequestDispatcher<? extends RestRequestHandlerResult> restRequestDispatcher;

	/**
	 * Constructs a new instance of {@link RestServer}.
	 *
	 * @param address
	 * An {@link InetSocketAddress} the {@link RestServer} will listen to.
	 * @param name
	 * A name of the {@link RestServer}.
	 * @param maxBossThreads
	 * Maximum number of {@link Thread}s that accept new TCP connections.
	 * @param maxWorkerThreads
	 * Maximum number of {@link Thread}s that process data received via the accepted TCP connections.
	 * @param maxPostResponseWorkerThreads
	 * Maximum number of {@link Thread}s that handle REST requests by using {@code restHandlers}.
	 * @param threadFactory
	 * A {@link ThreadFactory} that will be used to create boss, worker and REST request handler
	 * {@link Thread}s. {@link Thread} names MAY not be the same as the {@code threadFactory} generates.
	 * @param serverSocketChannelInitializer
	 * A {@link ChannelInitializer} that will be used to initialize a {@link ServerSocketChannel} that is
	 * bound to the {@code address}. If this argument is {@code null} then default {@link ChannelInitializer}
	 * , that adds a {@link LoggingHandler} with {@link LogLevel#DEBUG}, will be used.
	 * @param restHandlers
	 * {@link RestRequestHandler}s that will be used to handle REST requests.
	 * Each {@link RestRequestHandler} can only be associated with a single instance of {@link RestServer}.
	 */
	@Inject
	public RestServer(
			@RestServerAddress final InetSocketAddress address,
			@RestServerName final String name,
			@RestServerBoss final Integer maxBossThreads,
			@RestServerWorker final Integer maxWorkerThreads,
			@RestServerRequestHandling final Integer maxPostResponseWorkerThreads,
			@RestServerThreadFactory final ThreadFactory threadFactory,
			@RestServerBoss @Nullable final ChannelInitializer<ServerSocketChannel> serverSocketChannelInitializer,
			@RestServerRequestHandling @Nullable
			final Collection<? extends RestRequestHandler<? extends T>> restHandlers) {
		super(
				address,
				name,
				maxBossThreads,
				maxWorkerThreads,
				threadFactory,
				serverSocketChannelInitializer,
				null);
		final ExecutorService executorService
				= Executors.newFixedThreadPool(
						maxPostResponseWorkerThreads.intValue(),
						new ThreadFactoryBuilder()
						.setThreadFactory(threadFactory)
						.setNameFormat(name + "-restPostResponseExecutor-%d")
						.build());
		postResponseBlockingExecutorService = executorService;
		restRequestDispatcher = new RestRequestDispatcher<>(this, getServerBootstrap().childGroup(), restHandlers);
		getServerBootstrap().childHandler(new WorkerSocketChannelinitializer(
				restRequestDispatcher, postResponseBlockingExecutorService,
				this::createServiceHttpResponse, this::handleException));
	}

	/**
	 * Creates a new HTTP response that will be used by {@link RestServer}
	 * when there is no {@link RestRequestHandlerResult} available.
	 * <p>
	 * Subclasses MAY override this method.
	 * This method MUST support concurrent invocations.
	 * Version of the returned HTTP response MUST be {@link HttpVersion#HTTP_1_1}.
	 * <p>
	 * Default implementation in {@link RestServer} constructs the simplest HTTP response with
	 * <a href="http://tools.ietf.org/html/rfc2616#section-14.17">{@code "Content-Type"}</a>
	 * {@code "text/plain"} and {@link Constant#CHARSET} {@link Charset}.
	 *
	 * @param httpResponseStatus
	 * An <a href="http://tools.ietf.org/html/rfc2616#section-10">HTTP status code</a>.
	 * @param httpReasonPhrase
	 * An <a href="http://tools.ietf.org/html/rfc2616#section-6.1.1">HTTP reason phrase</a>.
	 * @return
	 * A new HTTP response.
	 */
	protected FullHttpResponse createServiceHttpResponse(
			final HttpResponseStatus httpResponseStatus,
			@Nullable final String httpReasonPhrase) {
		checkNotNull(httpResponseStatus, Message.ARGUMENT_NULL, "first", "httpResponseStatus");
		final FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
		if (httpReasonPhrase != null) {
			result.content().writeBytes(httpReasonPhrase.getBytes(Constant.CHARSET));
		}
		final HttpHeaders responseHeaders = result.headers();
		responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=" + Constant.CHARSET.name());
		return result;
	}

	/**
	 * Checks if the {@code httpRequest} requires {@linkplain HttpHeaders#isKeepAlive(HttpMessage) keep-alive}
	 * and modifies the {@code httpResponse} accordingly:
	 * adds {@link HttpHeaders.Names#CONTENT_LENGTH} and {@link HttpHeaders.Names#CONNECTION} HTTP headers.
	 * Adds only {@link HttpHeaders.Names#CONTENT_LENGTH} if {@code httpRequest} is {@code null}.
	 *
	 * @param httpRequest
	 * An {@link HttpRequest} that caused the {@code httpResponse}.
	 * @param httpResponse
	 * A {@link FullHttpResponse} to modify if required by {@code httpRequest}. {@linkplain FullHttpResponse#content() Content}
	 * of the {@code httpResponse} MUST be already prepared.
	 */
	static final void handleKeepAlive(final @Nullable HttpRequest httpRequest, final FullHttpResponse httpResponse) {
		checkNotNull(httpResponse, Message.ARGUMENT_NULL, "second", "httpResponse");
		final boolean keepAlive = httpRequest == null ? false : HttpHeaders.isKeepAlive(httpRequest);
		if (keepAlive) {
			/*
			 * Add "keep-alive" header as per
			 * http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			 */
			httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}
		httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
				Integer.valueOf(httpResponse.content().readableBytes()));
	}

	/**
	 * Handles uncaught {@link Throwable}s.
	 *
	 * @param e
	 * A {@link Throwable} occurred while processing {@code httpRequest}.
	 * MAY also be an {@link ExternallyVisibleException}, a {@link BadRestRequestException}, a {@link TimeoutException}.
	 * @param httpRequest
	 * An {@link HttpRequest} that caused the {@link Throwable} {@code e}.
	 * @return
	 * A {@link FullHttpResponse} that will be responded as a result of the uncaught {@link Throwable} {@code e}.
	 * This response MUST be created via the {@link #createServiceHttpResponse(HttpResponseStatus, String)} method.
	 */
	protected FullHttpResponse handleException(final Throwable e, @Nullable final HttpRequest httpRequest) {
		checkNotNull(e, Message.ARGUMENT_NULL_SINGLE, "e");
		final Optional<ExternallyVisibleException> optExternallyVisibleException
				= ThrowableUtil.extract(e, ExternallyVisibleException.class);
		@Nullable
		final String httpReasonPhrase;
		final HttpResponseStatus httpResponseStatus;
		final String logMessageFormat = "Exception caught, %s will be responded";
		if (optExternallyVisibleException.isPresent()) {
			final ExternallyVisibleException externallyVisibleException = optExternallyVisibleException.get();
			httpReasonPhrase = externallyVisibleException.getExternalMessage();
			if (externallyVisibleException instanceof BadRestRequestException) {
				httpResponseStatus = HTTP_RESPONSE_STATUS_BAD_REQUEST;
			} else if (externallyVisibleException instanceof TimeoutException) {
				httpResponseStatus = REQUEST_TIMEOUT;
			} else {
				httpResponseStatus = HTTP_RESPONSE_STATUS_INTERNAL_SERVER_ERROR;
			}
			LOGGER.warn(Message.format(logMessageFormat, httpResponseStatus), e);
		} else {
			httpReasonPhrase = null;
			httpResponseStatus = HTTP_RESPONSE_STATUS_INTERNAL_SERVER_ERROR;
			LOGGER.error(Message.format(logMessageFormat, httpResponseStatus), e);
		}
		return createServiceHttpResponse(httpResponseStatus, httpReasonPhrase);
	}

	@Override
	protected final void shutdownHook() {
		restRequestDispatcher.shutdown();
		ExecutorUtil.shutdownGracefully(postResponseBlockingExecutorService);
	}
}
