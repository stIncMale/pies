package com.gl.vn.me.ko.pies.platform.server.rest;

import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link RestRequest}s and {@linkplain ChannelHandlerContext#writeAndFlush(Object) responds}
 * with HTTP responses constructed by corresponding {@link RestRequestHandler}s.
 * In some situations {@link RestChannelHandler}
 * {@linkplain ChannelHandlerContext#writeAndFlush(Object) responds} with HTTP responses
 * constructed by using the {@link RestServer.ServiceHttpResponseConstructor#create(HttpResponseStatus, String)} method.
 * <p>
 * {@link RestChannelHandler} MUST be preceeded by the {@link RestDecoder} in the {@link ChannelPipeline}.
 */
@Sharable
final class RestChannelHandler extends ChannelHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestChannelHandler.class);
	private final RestRequestDispatcher<? extends RestRequestHandlerResult> restDispatcher;
	private final ExecutorService postResponseBlockingExecutorService;
	private final RestServer.ServiceHttpResponseConstructor serviceHttpResponseConstructor;
	private final RestServer.ExceptionHandler exceptionHandler;

	RestChannelHandler(
			final RestRequestDispatcher<? extends RestRequestHandlerResult> restDispatcher,
			final ExecutorService postResponseBlockingExecutorService,
			final RestServer.ServiceHttpResponseConstructor serviceHttpResponseConstructor,
			final RestServer.ExceptionHandler exceptionHandler) {
		this.restDispatcher = restDispatcher;
		this.postResponseBlockingExecutorService = postResponseBlockingExecutorService;
		this.serviceHttpResponseConstructor = serviceHttpResponseConstructor;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
		exceptionCaught(ctx, cause, null);
	}

	private final void exceptionCaught(
			final ChannelHandlerContext ctx, final Throwable cause, @Nullable final HttpRequest httpRequest) {
		final FullHttpResponse httpResponse = exceptionHandler.handle(cause, httpRequest);
		respond(ctx, httpRequest, httpResponse);
	}

	@Override
	public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		final RestHttpRequestsAlloy restHttpAlloy = (RestHttpRequestsAlloy)msg;
		final CompletionStage<? extends RestRequestHandlerResult> restResultCompletionStage;
		try {
			restResultCompletionStage = restDispatcher.dispatch(restHttpAlloy.getRestRequest());
			restResultCompletionStage.handleAsync((restResult, restException) -> restRequestHandled(
					ctx, restResult, restException, restHttpAlloy.getHttpRequest()), ctx.executor());
		} catch (final BindingNotFoundException e) {
			LOGGER.debug("Unable to dispatch a REST request", e);
			respond(ctx, restHttpAlloy.getHttpRequest(), RestServer.HTTP_RESPONSE_STATUS_BAD_REQUEST, "Unknown REST request");
		}
	}

	private final Void restRequestHandled(
			final ChannelHandlerContext ctx,
			@Nullable
			final RestRequestHandlerResult restResult,
			@Nullable
			final Throwable restException,
			@Nullable
			final HttpRequest httpRequest) {
		if (restResult != null) {
			final FullHttpResponse httpResponse = restResult.getHttpResponse();
			try {
				respond(ctx, httpRequest, httpResponse);
			} finally {
				restResult.getPostResponseAction()
						.ifPresent((postResponseAction) -> postResponseBlockingExecutorService.execute(postResponseAction));
			}
		} else if (restException != null) {
			exceptionCaught(ctx, new ApplicationException(restException));
		} else {
			exceptionCaught(ctx, new ApplicationError(Message.CAN_NEVER_HAPPEN));
		}
		return null;
	}

	private final void respond(
			final ChannelHandlerContext ctx, @Nullable final HttpRequest httpRequest, final FullHttpResponse httpResponse) {
		RestServer.handleKeepAlive(httpRequest, httpResponse);
		ctx.writeAndFlush(httpResponse);
	}

	private final void respond(
			final ChannelHandlerContext ctx,
			final HttpRequest httpRequest,
			final HttpResponseStatus httpResponseStatus,
			@Nullable
			final String httpReasonPhrase) {
		final FullHttpResponse httpResponse
				= serviceHttpResponseConstructor.create(httpResponseStatus, httpReasonPhrase);
		respond(ctx, httpRequest, httpResponse);
	}
}
