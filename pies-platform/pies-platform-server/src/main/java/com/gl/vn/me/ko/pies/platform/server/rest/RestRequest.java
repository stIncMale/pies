package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.feijoa.Stringable;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a REST request.
 * Such request has two informational parts: {@linkplain #getMethod() method} and {@linkplain #getUri() URI}.
 */
@Immutable
public final class RestRequest implements Stringable, Comparable<RestRequest> {
	/**
	 * Returns {@link RestRequest} that corresponds to the provided {@code stringValue}.
	 *
	 * @param stringValue
	 * A {@link String} that represents {@link RestRequest} as {@code method + " " + uri},
	 * where {@code method} and {@code uri} are strings as specified by {@link #valueOf(String, String)}.
	 * @return
	 * {@link RestRequest} that corresponds to the provided {@code stringValue}.
	 * @throws RestRequestSyntaxException
	 * If arguments violate {@link RestRequest} syntax.
	 */
	public static final RestRequest valueOf(final String stringValue) throws RestRequestSyntaxException {
		checkNotNull(stringValue, Message.ARGUMENT_NULL_SINGLE, "stringValue");
		final String[] parts = stringValue.split(" ");
		if (parts.length != 2) {
			throw new RestRequestSyntaxException(Message.format(Message.ARGUMENT_ILLEGAL_SINGLE, stringValue,
					"stringValue", "Expected value must contain exactly one space (' ', U+0020) character "
					+ "that separates REST method from URI"));
		}
		final String method = parts[0];
		final String uri = parts[1];
		return valueOf(method, uri);
	}

	/**
	 * Returns {@link RestRequest} that corresponds to the provided {@code method} and {@code uri}.
	 *
	 * @param method
	 * A {@link String} that represents {@link RestMethod}.
	 * @param uri
	 * A {@link String} that represents {@link URI} part of a {@link RestRequest}.
	 * Value of this parameter MUST obey rules specified in the {@link URI#URI(String)} constructor.
	 * The leading character MUST be solidus ('/', {@code U+002F}).
	 * <p>
	 * All parts of {@code uri} that aren't related to {@linkplain URI#getPath()} are ignored, but can be
	 * accessed via the {@link #getUri()} method.
	 * @throws RestRequestSyntaxException
	 * If arguments violate {@link RestRequest} syntax.
	 * @return
	 * {@link RestRequest} that corresponds to the provided {@code method} and {@code uri}.
	 */
	public static final RestRequest valueOf(final String method, String uri) throws RestRequestSyntaxException {
		checkNotNull(method, Message.ARGUMENT_NULL, "first", "method");
		checkNotNull(uri, Message.ARGUMENT_NULL, "second", "uri");
		if (!uri.startsWith("/")) {
			throw new RestRequestSyntaxException(Message.format(Message.ARGUMENT_ILLEGAL, uri, "second", "uri",
					"Expected value must start with solidus ('/', U+002F) character"));
		}
		if (!uri.endsWith("/")) {
			uri += "/";
		}
		final RestMethod restMethod;
		try {
			restMethod = RestMethod.valueOf(method);
		} catch (final IllegalArgumentException e) {
			throw new RestRequestSyntaxException(e);
		}
		final URI parsedUri;
		try {
			parsedUri = new URI(uri);
		} catch (final URISyntaxException e) {
			throw new RestRequestSyntaxException(e);
		}
		return new RestRequest(restMethod, parsedUri);
	}
	private final URI uri;
	private final RestMethod method;
	private final ImmutableList<String> uriNodes;
	private final String stringValue;
	private final String rawStringValue;

	/**
	 * Creates a new instance of {@link RestRequest}.
	 *
	 * @param method
	 * A {@link RestMethod} of the {@link RestRequest}.
	 * @param uri
	 * A {@link URI} of the {@link RestRequest}. {@code uri.}{@link URI#toString() toString()} MUST
	 * start and end with solidus ('/', {@code U+002F}) character.
	 * <p>
	 * All parts of {@code uri} that aren't related to {@linkplain URI#getPath()} are ignored, but can be
	 * accessed via the {@link #getUri()} method.
	 */
	private RestRequest(final RestMethod method, final URI uri) {
		this.method = method;
		final String strUri = uri.getPath();
		final String[] parts = strUri.split("/");
		@Nullable
		final String[] nodes = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : null;
		this.uriNodes = nodes != null ? ImmutableList.copyOf(nodes) : ImmutableList.of("");
		this.uri = uri;
		final StringBuilder sb = new StringBuilder();
		final String strValue
				= sb.append(this.method).append(" ").append(this.uri.getPath()).toString();
		sb.setLength(0);
		final String rawStrValue
				= sb.append(this.method).append(" ").append(this.uri.getRawPath()).toString();
		stringValue = strValue;
		rawStringValue = rawStrValue;
	}

	/**
	 * Compares two {@link RestRequest}s by comparing theirs {@link #toRawString()} value.
	 *
	 * @return
	 * {@code int} that is equal to {@code toRawString().compareTo(request.toRawString())}.
	 */
	@Override
	public final int compareTo(final RestRequest request) {
		checkNotNull(request, Message.ARGUMENT_NULL_SINGLE, "request");
		return toRawString().compareTo(request.toRawString());
	}

	/**
	 * Returns {@code true} if and only if {@code object} is an instance of {@link RestRequest} and
	 * {@code toRawString().equals(obj.toRawString()) == true}.
	 *
	 * @param object
	 * An {@link Object} with which to compare.
	 * @return
	 * Returns {@code true} if this {@link RestRequest} is equal to {@code object} and {@code false} otherwise.
	 */
	@SuppressFBWarnings(
			value = "NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION", justification = "Object.equals(...) allows null arguments")
	@Override
	public final boolean equals(@Nullable final Object object) {
		final boolean result;
		if (object instanceof RestRequest) {
			final RestRequest request = (RestRequest)object;
			result = this.toRawString().equals(request.toRawString());
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Returns a {@link RestMethod} of the {@link RestRequest}.
	 *
	 * @return
	 * A {@link RestMethod} of the {@link RestRequest}.
	 */
	public final RestMethod getMethod() {
		return method;
	}

	/**
	 * Returns a {@link URI} of the {@link RestRequest}.
	 *
	 * @return
	 * A {@link URI} of the {@link RestRequest}.
	 * @see #getUriNodes()
	 */
	public final URI getUri() {
		return uri;
	}

	/**
	 * Returns nodes of a {@link URI} of the {@link RestRequest}.
	 * Each node MAY contain any characters except solidus ('/', {@code U+002F}) character. Nodes are calculated from a
	 * {@link URI} by splitting (from left to right) {@linkplain URI#getPath() path} of the {@link URI}.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>request with URI {@code "/some/element/by%20this_id/"} contains 3 nodes {@code "some"}, {@code "element"} and
	 * {@code "by this_id"}</li>
	 * <li>request with URI {@code "/"} contains 1 node {@code ""}</li>
	 * </ul>
	 *
	 * @return
	 * Unmodifiable {@link List} of nodes of the {@link RestRequest} that contains at least one element.
	 * @see #getUri()
	 */
	public final List<String> getUriNodes() {
		return uriNodes;
	}

	@Override
	public final int hashCode() {
		return toRawString().hashCode();
	}

	/**
	 * Returns {@link String} that is equal to {@code getMethod() + " " + getUri().getRawPath()}.
	 * <p>
	 * Examples: {@code "GET /some/element/"}, {@code "PUT /some/with%20space/"}.
	 *
	 * @return A raw (aka encoded) {@link String} representation of {@link RestRequest}.
	 */
	public final String toRawString() {
		return rawStringValue;
	}

	/**
	 * Returns {@link String} that is equal to {@code getMethod() + " " + getUri().getPath()}.
	 * <p>
	 * Examples: {@code "GET /some/element/"}, {@code "PUT /some/with space/"}.
	 *
	 * @return
	 * A decoded {@link String} representation of {@link RestRequest}.
	 */
	@Override
	public final String toString() {
		return stringValue;
	}
}
