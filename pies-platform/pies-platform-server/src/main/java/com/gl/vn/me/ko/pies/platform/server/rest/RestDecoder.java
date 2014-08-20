package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes {@link HttpRequest} and {@link HttpContent} objects and constructs corresponding {@link RestRequest}s,
 * and {@linkplain ChannelHandlerContext#fireChannelRead(Object) fires channel read} event
 * with {@link RestHttpRequestsAlloy} object.
 * In some situations {@link RestDecoder} doesn't
 * {@linkplain ChannelHandlerContext#fireChannelRead(Object) fires channel read} event
 * and {@linkplain ChannelHandlerContext#writeAndFlush(Object) responds} with HTTP responses
 * constructed by using the {@link RestServer.ServiceHttpResponseConstructor#create(HttpResponseStatus, String)} method.
 * <p>
 * {@link RestDecoder} MUST be preceeded by the {@link HttpServerCodec} in the {@link ChannelPipeline}.
 */
final class RestDecoder extends SimpleChannelInboundHandler<Object> {
	private static final class State {
		@Nullable
		HttpRequest httpRequest;
		@Nullable
		RestRequest restRequest;

		private State() {
			clear();
		}

		private final void clear() {
			httpRequest = null;
			restRequest = null;
		}

		private final boolean isClear() {
			return (httpRequest == null) && (restRequest == null);
		}

		private final RestHttpRequestsAlloy getAlloy() throws IllegalStateException {
			checkState(httpRequest != null, "% is null", "httpRequest");
			checkState(restRequest != null, "% is null", "restRequest");
			return new RestHttpRequestsAlloy(restRequest, httpRequest);
		}

		@Override
		public final String toString() {
			final StringBuilder sb = new StringBuilder(getClass().getName())
					.append("(httpRequest=").append(httpRequest)
					.append(", restRequest=").append(restRequest).append(')');
			final String result = sb.toString();
			return result;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(RestDecoder.class);
	private final RestServer.ServiceHttpResponseConstructor serviceHttpResponseConstructor;
	private final RestServer.ExceptionHandler exceptionHandler;
	private final State state;

	/**
	 * Constructs a new instance of {@link RestDecoder}.
	 *
	 * @param serviceHttpResponseConstructor
	 * An instance of the {@link RestServer.ServiceHttpResponseConstructor}.
	 * @param keepAliveHandler
	 * An instance of the {@link RestServer.ExceptionHandler}.
	 *
	 */
	RestDecoder(
			final RestServer.ServiceHttpResponseConstructor serviceHttpResponseConstructor,
			final RestServer.ExceptionHandler exceptionHandler) {
		this.serviceHttpResponseConstructor = serviceHttpResponseConstructor;
		this.exceptionHandler = exceptionHandler;
		state = new State();
	}

	@Override
	public final void channelInactive(final ChannelHandlerContext ctx) {
		state.clear();
		ctx.fireChannelInactive();
	}

	@Override
	public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
		exceptionCaught(ctx, cause, null);
	}

	private final void exceptionCaught(
			final ChannelHandlerContext ctx, final Throwable cause, @Nullable final HttpRequest httpRequest) {
		final FullHttpResponse httpResponse = exceptionHandler.handle(cause, httpRequest);
		respond(ctx, httpResponse, true);
	}

	@Override
	protected final void messageReceived(final ChannelHandlerContext ctx, final Object msg) {
		if (msg instanceof HttpRequest) {
			if (!state.isClear()) {
				exceptionCaught(ctx, new ApplicationError(Message.format("State %s isn't clean", state)), state.httpRequest);
			} else {
				final HttpRequest localHttpRequest = state.httpRequest = (HttpRequest)msg;
				if (HttpHeaders.is100ContinueExpected(localHttpRequest)) {
					respond(ctx, RestServer.HTTP_RESPONSE_STATUS_CONTINUE, null, false);
				} else {
					final String restMethod = localHttpRequest.getMethod().toString();
					String restUri = localHttpRequest.getUri();
					try {
						state.restRequest = RestRequest.valueOf(restMethod, restUri);
					} catch (final RestRequestSyntaxException e) {
						LOGGER.debug("Unable to construct a REST request", e);
						respond(ctx, RestServer.HTTP_RESPONSE_STATUS_BAD_REQUEST, "Incorrect REST request syntax", true);
					}
				}
			}
		} else if (msg instanceof HttpContent) {
			if (msg instanceof LastHttpContent) {
				final LastHttpContent lastHttpContent = (LastHttpContent)msg;
				if (lastHttpContent.getDecoderResult().isSuccess()) {
					try {
						final RestHttpRequestsAlloy restHttpAlloy = state.getAlloy();
						ctx.fireChannelRead(restHttpAlloy);
						state.clear();
					} catch (final IllegalStateException e) {
						exceptionCaught(ctx, new ApplicationError(e), state.httpRequest);
					}
				} else {
					LOGGER.debug("Unable to decode content {} of a request", lastHttpContent);
					respond(ctx, RestServer.HTTP_RESPONSE_STATUS_BAD_REQUEST, "Unable to decode request content", true);
				}
			}
		}
	}

	private final void respond(
			final ChannelHandlerContext ctx,
			final FullHttpResponse finalHttpResponse,
			final boolean resetState) {
		try {
			ctx.writeAndFlush(finalHttpResponse);
		} finally {
			if (resetState) {
				state.clear();
			}
		}
	}

	private final void respond(
			final ChannelHandlerContext ctx,
			final HttpResponseStatus httpResponseStatus,
			@Nullable final String httpReasonPhrase,
			final boolean resetState) {
		final FullHttpResponse httpResponse
				= serviceHttpResponseConstructor.create(httpResponseStatus, httpReasonPhrase);
		RestServer.handleKeepAlive(state.httpRequest, httpResponse);
		respond(ctx, httpResponse, resetState);
	}

	@Override
	public final void close(final ChannelHandlerContext ctx, final ChannelPromise promise) {
		final Channel channel = ctx.channel();
		ctx.close();
		LOGGER.debug("{} was closed", channel);
	}
}
