package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Represents a result of handling a {@link RestRequest} by a {@link RestRequestHandler}.
 */
public abstract class RestRequestHandlerResult {
	private final HttpResponseStatus responseStatus;
	@Nullable
	private final Runnable postResponseAction;

	/**
	 * Constructor of {@link RestRequestHandlerResult}.
	 *
	 * @param httpResponseStatus
	 * A status of an {@linkplain #getHttpResponse() HTTP response}.
	 * @param postResponseAction
	 * A {@link Runnable} action that will be performed by {@link RestServer}
	 * after sending an {@linkplain #getHttpResponse() HTTP response}.
	 * <p>
	 * This action is allowed to be blocking.
	 */
	protected RestRequestHandlerResult(
			final HttpResponseStatus httpResponseStatus,
			@Nullable final Runnable postResponseAction) {
		checkNotNull(httpResponseStatus, Message.ARGUMENT_NULL, "first", "httpResponseStatus");
		this.responseStatus = httpResponseStatus;
		this.postResponseAction = postResponseAction;
	}

	/**
	 * Creates a new HTTP response that will be used by {@link RestServer}.
	 * Version of the returned HTTP response MUST be {@link HttpVersion#HTTP_1_1}.
	 *
	 * @return
	 * An HTTP response that will be used by {@link RestServer}.
	 */
	protected abstract FullHttpResponse getHttpResponse();

	/**
	 * Returns an {@link Optional} action that will be performed by {@link RestServer}
	 * after sending an {@linkplain #getHttpResponse() HTTP response}.
	 * Action is performed even if {@link RestServer} has failed to respond.
	 * <p>
	 * This action is allowed to be blocking.
	 *
	 * @return
	 * A {@link Runnable} action that will be performed by {@link RestServer}
	 * after sending an {@linkplain #getHttpResponse() HTTP response}.
	 */
	protected final Optional<Runnable> getPostResponseAction() {
		return Optional.ofNullable(postResponseAction);
	}

	/**
	 * Returns a status of an {@linkplain #getHttpResponse() HTTP response}.
	 *
	 * @return
	 * A status of an {@linkplain #getHttpResponse() HTTP response}.
	 */
	protected final HttpResponseStatus getResponseStatus() {
		return responseStatus;
	}

	/**
	 * Returns a description of the {@link RestRequestHandlerResult}.
	 *
	 * @return
	 * A description of the {@link RestRequestHandlerResult}.
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(responseStatus=").append(responseStatus)
				.append(", postResponseAction=").append(postResponseAction).append(')');
		final String result = sb.toString();
		return result;
	}
}
