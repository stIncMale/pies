package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Constant;
import com.gl.vn.me.ko.pies.base.constant.Message;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import javax.annotation.Nullable;
import javax.json.JsonStructure;

/**
 * Represents a JSON-based result of handling a {@link RestRequest} by a {@link RestRequestHandler}.
 */
public final class JsonRestRequestHandlerResult extends RestRequestHandlerResult {
	private static final String CONTENT_TYPE = "application/json; charset=" + Constant.CHARSET.name();
	@Nullable
	private final JsonStructure responseContent;
	private final String responseContentLanguage;

	/**
	 * Constructs a new instance of {@link JsonRestRequestHandlerResult}.
	 *
	 * @param httpResponseStatus
	 * A status of an {@linkplain #getHttpResponse() HTTP response}.
	 * @param httpResponseContent
	 * A {@link JsonStructure} that represents a responseContent of an HTTP response.
	 * If {@code null} is specified then HTTP response will have no responseContent.
	 * @param httpResponseContentLanguage
	 * Value of {@link io.netty.handler.codec.http.HttpHeaders.Names#CONTENT_LANGUAGE} header.
	 * @param postResponseAction
	 * A {@link Runnable} action that will be performed by {@link JsonRestServer}
	 * after sending an {@linkplain #getHttpResponse() HTTP response}.
	 * <p>
	 * This action is allowed to be blocking.
	 */
	public JsonRestRequestHandlerResult(
			final HttpResponseStatus httpResponseStatus,
			@Nullable final JsonStructure httpResponseContent,
			final String httpResponseContentLanguage,
			@Nullable final Runnable postResponseAction) {
		super(httpResponseStatus, postResponseAction);
		checkNotNull(httpResponseContentLanguage, Message.ARGUMENT_NULL, "third", "httpResponseContentLanguage");
		this.responseContent = httpResponseContent;
		this.responseContentLanguage = httpResponseContentLanguage;
	}

	@Override
	protected final FullHttpResponse getHttpResponse() {
		final FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, getResponseStatus());
		final Charset charset = Constant.CHARSET;
		if (responseContent != null) {
			result.content().writeBytes(responseContent.toString().getBytes(charset));
		}
		final HttpHeaders responseHeaders = result.headers();
		responseHeaders.set(HttpHeaders.Names.CONTENT_LANGUAGE, responseContentLanguage);
		responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
		return result;
	}

	/**
	 * Returns a description of the {@link JsonRestRequestHandlerResult}.
	 *
	 * @return
	 * A description of the {@link JsonRestRequestHandlerResult}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(getClass().getName())
				.append("(").append("responseStatus=").append(getResponseStatus())
				.append(", responseContent=").append(responseContent)
				.append(", responseContentLanguage=").append(responseContentLanguage)
				.append(", postResponseAction=")
				.append(getPostResponseAction().orElse(null)).append(')');
		final String result = sb.toString();
		return result;
	}
}
