package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.feijoa.ExecutorUtil;
import com.gl.vn.me.ko.pies.platform.server.Server;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher of {@link RestRequest}s.
 *
 * @param <T>
 * Represents type of a result of the {@link RestRequestHandler#handle(RestRequest)} method of
 * {@link RestRequestHandler}s this dispatcher uses.
 */
@ThreadSafe
final class RestRequestDispatcher<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestRequestDispatcher.class);
	private final NavigableMap<RestRequest, RestRequestHandler<? extends T>> handlers;
	private final ExecutorService unshutdownableExecutorService;
	private final Server server;
	private final AtomicBoolean active;

	/**
	 * Constructs a new instance of {@link RestRequestDispatcher}.
	 *
	 * @param server
	 * A {@link Server} this {@link RestRequestDispatcher} belongs to.
	 * @param executorService
	 * {@link ExecutorService} that will be used to handleRequest {@link RestRequest}s by
	 * {@link #dispatch(RestRequest)} method.
	 * @param handlers
	 * {@link Collection} of {@link RestRequestHandler}s that will be associated with this
	 * {@link RestRequestDispatcher}. Any {@link RestRequestHandler} MAY only be associated with a single
	 * {@link RestRequestHandler}.
	 */
	@SuppressWarnings("LeakingThisInConstructor")
	RestRequestDispatcher(
			final Server server,
			final ExecutorService executorService,
			@Nullable final Collection<? extends RestRequestHandler<? extends T>> handlers) {
		checkNotNull(server, Message.ARGUMENT_NULL, "first", "server");
		checkNotNull(executorService, Message.ARGUMENT_NULL, "second", "executorService");
		this.server = server;
		this.unshutdownableExecutorService = ExecutorUtil.unshutdownable(executorService);
		this.handlers = new TreeMap<>();
		if (handlers != null) {
			handlers.forEach((handler) -> {
				this.handlers.put(handler.getBinding(), handler);
				/*
				 * In order this leak to not break thread-safety RestRequestHandler MUST never access
				 * this.handlers and any handler in this.handlers.
				 * Currently RestRequestHandler only reads this.server and it's OK,
				 * because write to this.server happens-before associate()
				 * which happens-before read of this.server in RestRequestHandler
				 */
				handler.associate(this);
			});
		}
		active = new AtomicBoolean(true);
	}

	/**
	 * Determines the most suitable {@link RestRequestHandler} bound to the specified {@code request} and initiates
	 * handling of the {@code request}.
	 *
	 * @param request
	 * {@link RestRequest} that needs to be handled.
	 * @return
	 * A {@link CompletionStage} that represents an asynchronous result of {@code request} handling.
	 * @throws BindingNotFoundException
	 * If no suitable {@link RestRequestHandler} was found to handleRequest the {@code request}.
	 * @throws IllegalStateException
	 * If {@link #shutdown()} was invoked at least once.
	 */
	final CompletionStage<? extends T> dispatch(final RestRequest request)
			throws BindingNotFoundException, IllegalStateException {
		checkNotNull(request, Message.ARGUMENT_NULL_SINGLE, "request");
		checkState(active.get(), "%s isn't active", this);
		final RestRequestHandler<? extends T> handler = getSuitableHandler(request);
		LOGGER.debug("Request {} is going to be dispatched to {}", request, handler);
		return handler.handle(request, unshutdownableExecutorService);
	}

	/**
	 * Returns a {@link Server} this {@link RestRequestDispatcher} belongs to.
	 *
	 * @return
	 * A {@link Server} this {@link RestRequestDispatcher} belongs to.
	 */
	final Server getServer() {
		return server;
	}

	/**
	 * Returns the most suitable {@link RestRequestHandler} bound to the specified {@code request}.
	 *
	 * @param request
	 * {@link RestRequest} for which to find a {@link RestRequestHandler}.
	 * @return
	 * The most suitable {@link RestRequestHandler} bound to the specified {@code request}.
	 * @throws BindingNotFoundException
	 * If no suitable {@link RestRequestHandler} was found to handleRequest the {@code request}.
	 */
	private final RestRequestHandler<? extends T> getSuitableHandler(final RestRequest request)
			throws BindingNotFoundException {
		@Nullable
		RestRequestHandler<? extends T> result = null;
		@Nullable
		final Map.Entry<RestRequest, RestRequestHandler<? extends T>> handlerEntry
				= handlers.floorEntry(request);
		if (handlerEntry != null) {
			final RestRequestHandler<? extends T> handler = handlerEntry.getValue();
			final RestRequest binding = handler.getBinding();
			if (request.toRawString().startsWith(binding.toRawString())) {
				result = handler;
			}
		}
		if (result == null) {
			throw new BindingNotFoundException(
					Message.format("Handler suitable for the request %s wasn't found among handlers %s",
							request, handlers.values()));
		}
		return result;
	}

	/**
	 * Returns a description of the {@link RestRequestDispatcher}.
	 *
	 * @return
	 * A description of the {@link RestRequestDispatcher}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(handlers=").append(handlers.values()).append(", ")
				.append("server=").append(server).append(", ")
				.append("active=").append(active.get()).append(')');
		final String result = sb.toString();
		return result;
	}

	/**
	 * Performs a shutdown procedure.
	 * Once this method is invoked {@link RestRequestDispatcher} can't {@linkplain #dispatch(RestRequest) dispatch} requests.
	 * This method invokes {@link RestRequestHandler#shutdown()} on each associated {@link RestRequestHandler} instance.
	 * The method is idempotent and is called from {@link RestServer#shutdown()}.
	 */
	final void shutdown() {
		if (active.compareAndSet(true, false)) {
			handlers.values().forEach(RestRequestHandler::shutdown);
		}
	}
}
