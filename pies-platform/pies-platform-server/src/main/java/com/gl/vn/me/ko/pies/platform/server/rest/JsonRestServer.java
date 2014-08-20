package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Constant;
import com.gl.vn.me.ko.pies.base.constant.Message;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RestServer} that responds by using JSON only.
 * <p>
 * HTTP responses MAY specify
 * <a href="http://tools.ietf.org/html/rfc2616#section-6.1.1">HTTP reason phrase</a> in the following format
 * (see {@link #JSON_RESPONSE_REASON_PHRASE_NVNAME}):
 * <pre><code>
 * {
 * 	"httpReasonPhrase": "a reason phrase"
 * }
 * </code></pre>
 */
@ThreadSafe
public final class JsonRestServer extends RestServer<JsonRestRequestHandlerResult> {
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonRestServer.class);
	/**
	 * Name of a name-value JSON pair that specifies an
	 * <a href="http://tools.ietf.org/html/rfc2616#section-6.1.1">HTTP reason phrase</a>.
	 * <p>
	 * Value of this constant is {@value}.
	 */
	public static final String JSON_RESPONSE_REASON_PHRASE_NVNAME = "httpReasonPhrase";
	private final JsonBuilderFactory jsonBuilderFactory;

	/**
	 * Constructs a new instance of {@link JsonRestServer}.
	 * See {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}
	 * for details.
	 *
	 * @param address
	 * See {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param name
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param maxBossThreads
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param maxWorkerThreads
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param maxPostResponseWorkerThreads
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param threadFactory
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param serverSocketChannelInitializer
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param restHandlers
	 * {@link RestServer#RestServer(
	 * InetSocketAddress, String, Integer, Integer, Integer, ThreadFactory, ChannelInitializer, Collection)}.
	 * @param jsonBuilderFactory
	 * An implementation of {@link JsonBuilderFactory}.
	 */
	@Inject
	public JsonRestServer(
			@RestServerAddress final InetSocketAddress address,
			@RestServerName final String name,
			@RestServerBoss final Integer maxBossThreads,
			@RestServerWorker final Integer maxWorkerThreads,
			@RestServerRequestHandling final Integer maxPostResponseWorkerThreads,
			@RestServerThreadFactory final ThreadFactory threadFactory,
			@RestServerBoss @Nullable final ChannelInitializer<ServerSocketChannel> serverSocketChannelInitializer,
			@RestServerRequestHandling @Nullable
			final Collection<? extends RestRequestHandler<? extends JsonRestRequestHandlerResult>> restHandlers,
			final JsonBuilderFactory jsonBuilderFactory) {
		super(
				address,
				name,
				maxBossThreads,
				maxWorkerThreads,
				maxPostResponseWorkerThreads,
				threadFactory,
				serverSocketChannelInitializer,
				restHandlers
		);
		checkNotNull(jsonBuilderFactory, Message.ARGUMENT_NULL, "tenth", "jsonBuilderFactory");
		this.jsonBuilderFactory = jsonBuilderFactory;
	}

	@Override
	protected final FullHttpResponse createServiceHttpResponse(
			final HttpResponseStatus httpResponseStatus,
			@Nullable final String httpReasonPhrase) {
		final FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
		if (httpReasonPhrase != null) {
			final JsonObjectBuilder jsonBuilder = jsonBuilderFactory.createObjectBuilder();
			jsonBuilder.add(JSON_RESPONSE_REASON_PHRASE_NVNAME, httpReasonPhrase);
			final JsonObject httpResponseContent = jsonBuilder.build();
			result.content().writeBytes(httpResponseContent.toString().getBytes(Constant.CHARSET));
		}
		final HttpHeaders responseHeaders = result.headers();
		responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=" + Constant.CHARSET.name());
		return result;
	}
}
