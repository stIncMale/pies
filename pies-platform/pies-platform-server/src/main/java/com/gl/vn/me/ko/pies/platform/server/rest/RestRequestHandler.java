package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.platform.server.Server;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents object that is able handleRequest a {@link RestRequest}.
 *
 * @param <T>
 * Represents type of a result of the {@link #handleRequest(RestRequest, ExecutorService)} method.
 */
@ThreadSafe
public abstract class RestRequestHandler<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestRequestHandler.class);
	private final RestRequest binding;
	/**
	 * {@link RestRequestHandler} MUST access {@link RestRequestDispatcher} very carefully:
	 * any new access MUST be analyzed on thread-safety. See code of
	 * {@link RestRequestDispatcher#RestRequestDispatcher(Server, ExecutorService, Collection)}
	 * for details.
	 */
	private final AtomicReference<RestRequestDispatcher<?>> dispatcher;
	private final AtomicBoolean active;

	/**
	 * Constructor of {@link RestRequestHandler}.
	 *
	 * @param binding
	 * {@link RestRequest} that represents a family of {@link RestRequest}s
	 * this {@link RestRequestHandler} is able to handleRequest (see {@link #getBinding()} for details).
	 */
	protected RestRequestHandler(final RestRequest binding) {
		checkNotNull(binding, Message.ARGUMENT_NULL_SINGLE, "binding");
		this.binding = binding;
		dispatcher = new AtomicReference<>(null);
		active = new AtomicBoolean(true);
	}

	/**
	 * Associates a {@link RestRequestHandler} with the provided {@code dispatcher}.
	 * This method is called from constructors of {@link RestRequestDispatcher}.
	 *
	 * @param dispatcher
	 * A {@link RestRequestDispatcher} with which to associate the {@link RestRequestHandler}.
	 * @throws IllegalStateException
	 * If {@link RestRequestHandler} is already associated with some {@link RestRequestDispatcher}.
	 */
	final void associate(final RestRequestDispatcher<?> dispatcher) {
		checkNotNull(dispatcher, Message.ARGUMENT_NULL_SINGLE, "dispatcher");
		checkState(active.get(), "%s isn't active", this);
		if (!this.dispatcher.compareAndSet(null, dispatcher)) {
			throw new IllegalStateException(
					Message.format("Handler %s is already associated with %s", this, dispatcher));
		}
	}

	/**
	 * Returns {@code true} if and only if {@code object} is an instance of {@link RestRequestHandler} and
	 * {@linkplain #getBinding() is bound} to the same family of {@link RestRequest}s
	 * as this {@link RestRequestHandler}.
	 *
	 * @param object
	 * An {@link Object} with which to compare.
	 * @return
	 * {@code true} if this {@link RestRequestHandler} is equal to {@code object} and {@code false} otherwise.
	 */
	@SuppressFBWarnings(
			value = "NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION", justification = "Object.equals(...) allows null arguments")
	@Override
	public final boolean equals(@Nullable final Object object) {
		final boolean result;
		if (object instanceof RestRequestHandler) {
			final RestRequestHandler<?> handler = (RestRequestHandler<?>)object;
			result = binding.equals(handler.binding);
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Returns arguments of the provided {@code request}. E.g. if the {@code request} is {@code "POST /b1/a1/a2/"} and
	 * {@linkplain #getBinding() binding} is {@code "POST /b1/"} then arguments are {@code "a1"} and {@code "a2"}.
	 *
	 * @param request
	 * {@link RestRequest} dispatched to this {@link RestRequestHandler}.
	 * @return
	 * {@link List} that contains {@link String} arguments of the provided {@code request}.
	 * The returned {@link List} is {@linkplain List#isEmpty() empty} if there are no arguments.
	 */
	public final List<String> getArguments(final RestRequest request) {
		final List<String> requestUriNodes = request.getUriNodes();
		final List<String> bindingUriNodes = binding.getUriNodes();
		final int resultSize = requestUriNodes.size() - bindingUriNodes.size();
		final List<String> result;
		if (resultSize > 0) {
			result = new ArrayList<>(resultSize);
			for (int i = bindingUriNodes.size(); i < requestUriNodes.size(); i++) {
				result.add(requestUriNodes.get(i));
			}
		} else {
			final List<String> empty = Collections.<String>emptyList();
			result = empty;
		}
		return result;
	}

	/**
	 * Returns {@linkplain RestRequest#toString() string representation} of the family of {@link RestRequest}s
	 * this {@link RestRequestHandler} is able to handleRequest. Such a family of {@link RestRequest}s is also a
	 * {@link RestRequest} and is called a binding.
	 * More strictly binding is a {@link RestRequest} that represents the
	 * biggest common part of all {@link RestRequest}s this {@link RestRequestHandler} is able to handleRequest.
	 * E.g. if the {@link RestRequestHandler} is able to handleRequest requests {@code "POST /elements/by_date/2014/01/30/"}
	 * and {@code "POST /elements/by_date/1985/"} then binding is {@code "POST /elements/by_date/"}.
	 *
	 * @return
	 * Returns {@linkplain RestRequest#toString() string representation} of the binding.
	 */
	public final RestRequest getBinding() {
		return binding;
	}

	/**
	 * Returns a {@link Server} this {@link RestRequestHandler} belongs to.
	 *
	 * @return
	 * A {@link Server} this {@link RestRequestHandler} belongs to.
	 * @throws IllegalStateException
	 * If {@link RestRequestHandler} doesn't belong to any {@link Server}.
	 */
	protected final Server getServer() {
		@Nullable
		final RestRequestDispatcher<?> dispatcher = this.dispatcher.get();
		final Server result;
		if (dispatcher != null) {
			result = dispatcher.getServer();
		} else {
			throw new IllegalStateException(
					Message.format("Handler %s isn't associated with any REST request dispatcher", this));
		}
		return result;
	}

	/**
	 * This method invokes {@link #handleRequest(RestRequest, ExecutorService)}.
	 * It's guaranteed that this method doesn't throw any {@link Exception} except {@link IllegalStateException}.
	 *
	 * @param request
	 * {@link RestRequest} to handleRequest.
	 * @param executorService
	 * {@link ExecutorService} to use to handleRequest the {@code request}.
	 * @return
	 * Result of the {@link #handleRequest(RestRequest, ExecutorService)} method.
	 * @throws IllegalStateException
	 * If {@link #shutdown()} was invoked at least once.
	 */
	final CompletionStage<T> handle(RestRequest request, ExecutorService executorService) throws IllegalStateException {
		checkNotNull(request, Message.ARGUMENT_NULL, "first", "request");
		checkNotNull(executorService, Message.ARGUMENT_NULL, "second", "executorService");
		checkState(active.get(), "%s isn't active", this);
		CompletionStage<T> result;
		try {
			result = handleRequest(request, executorService);
		} catch (final Exception e) {
			final CompletableFuture<T> exceptionallyCompleted = new CompletableFuture<>();
			exceptionallyCompleted.completeExceptionally(e);
			result = exceptionallyCompleted;
		}
		return result;
	}

	/**
	 * Handles the specified {@code request}.
	 * This method MUST NOT perform blocking operations.
	 *
	 * @param request
	 * {@link RestRequest} to handleRequest.
	 * @param executorService
	 * {@link ExecutorService} to use to handleRequest the {@code request}.
	 * Implementation MUST NOT {@link ExecutorService#shutdown() shut down} this {@link ExecutorService}.
	 * @return
	 * {@link CompletionStage} that represents result of request handling.
	 * The returned {@link CompletionStage} MUST be completed exceptionally as follows:
	 * <ul>
	 * <li>with {@link RestRequestHandlingException} if handling of the {@code request} has failed</li>
	 * <li>with {@link BadRestRequestException} if the {@code request} is incorrect</li>
	 * </ul>
	 */
	protected abstract CompletionStage<T> handleRequest(RestRequest request, ExecutorService executorService);

	/**
	 * Returns if the {@link RestRequestHandler} active or was {@linkplain #shutdown() shut down}.
	 *
	 * @return
	 * {@code true} if the {@link RestRequestHandler} active, {@code false} otherwise.
	 */
	protected final boolean getState() {
		return active.get();
	}

	@Override
	public final int hashCode() {
		return binding.hashCode();
	}

	/**
	 * Returns a description of the {@link RestRequestHandler}.
	 *
	 * @return
	 * A description of the {@link RestRequestHandler}.
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(binding=").append(getBinding())
				.append(", active=").append(active.get()).append(')');
		final String result = sb.toString();
		return result;
	}

	/**
	 * This method invokes {@link #shutdownHook()}.
	 * Once this method is invoked {@link RestRequestHandler} can't
	 * {@linkplain #handle(RestRequest, ExecutorService) handle} requests
	 * and can't be {@linkplain #associate(RestRequestDispatcher) associated} with a {@link RestRequestDispatcher}.
	 * The method is idempotent and is called from {@link RestRequestDispatcher#shutdown()}.
	 */
	final void shutdown() {
		if (active.compareAndSet(true, false)) {
			shutdownHook();
			LOGGER.info("{} was shut down", this);
		}
	}

	/**
	 * Subclasses MAY implement this method in order to perform shutdown actions; this implementation does nothing.
	 * This method is called from the {@link #shutdown()} method and MAY be not idempotent.
	 */
	protected void shutdownHook() {
	}
}
