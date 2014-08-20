package com.gl.vn.me.ko.pies.platform.server.rest;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Represents pair of {@link HttpRequest} and corresponding {@link RestRequest}.
 */
final class RestHttpRequestsAlloy {
	private final RestRequest restRequest;
	private final HttpRequest httpRequest;

	/**
	 * Constructs a new instance of {@link RestHttpRequestsAlloy}.
	 *
	 * @param restRequest
	 * A {@link RestRequest} constructed from the {@code httpRequest}.
	 * @param httpRequest
	 * An {@link HttpRequest} used to construct {@code restRequest}.
	 */
	RestHttpRequestsAlloy(final RestRequest restRequest, final HttpRequest httpRequest) {
		this.restRequest = restRequest;
		this.httpRequest = httpRequest;
	}

	/**
	 * Returns contained {@link RestRequest}.
	 *
	 * @return
	 * Contained {@link RestRequest}.
	 */
	final RestRequest getRestRequest() {
		return restRequest;
	}

	/**
	 * Returns contained {@link HttpRequest}.
	 *
	 * @return
	 * Contained {@link HttpRequest}.
	 */
	final HttpRequest getHttpRequest() {
		return httpRequest;
	}

	/**
	 * Returns a description of the {@link RestHttpRequestsAlloy}.
	 *
	 * @return
	 * A description of the {@link RestHttpRequestsAlloy}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(restRequest=").append(restRequest)
				.append(", httpRequest=").append(httpRequest).append(')');
		final String result = sb.toString();
		return result;
	}
}
