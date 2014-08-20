package com.gl.vn.me.ko.pies.platform.server.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.google.common.collect.ImmutableSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public final class TestRestRequestDispatcher {
	private static final class Handler1 extends RestRequestHandler<Object> {
		private static final RestRequest BINDING = RestRequest.valueOf("GET /one/two/");
		private static final Object VALUE = "header1Value_";

		private Handler1() {
			super(BINDING);
		}

		@Override
		public final CompletionStage<Object> handleRequest(final RestRequest request, final ExecutorService executorService) {
			return CompletableFuture.completedFuture(VALUE.toString() + request.toRawString());
		}
	}

	private static final class Handler2 extends RestRequestHandler<Object> {
		private static final RestRequest BINDING = RestRequest.valueOf("GET /one/two/thr%20ee/");
		private static final Object VALUE = "header2Value_";

		private Handler2() {
			super(BINDING);
		}

		@Override
		public final CompletionStage<Object> handleRequest(final RestRequest request, final ExecutorService executorService) {
			return CompletableFuture.completedFuture(VALUE.toString() + request.toRawString());
		}
	}

	private static final class Handler3 extends RestRequestHandler<Object> {
		private static final RestRequest BINDING = RestRequest.valueOf("PUT /one/two/");
		private static final Object VALUE = "header3Value_";

		private Handler3() {
			super(BINDING);
		}

		@Override
		public final CompletionStage<Object> handleRequest(final RestRequest request, final ExecutorService executorService) {
			return CompletableFuture.completedFuture(VALUE.toString() + request.toRawString());
		}
	}

	private static final class Handler4 extends RestRequestHandler<Object> {
		private static final RestRequest BINDING = RestRequest.valueOf("GET /one/two/thr/");
		private static final Object VALUE = "header4Value_";

		private Handler4() {
			super(BINDING);
		}

		@Override
		public final CompletionStage<Object> handleRequest(final RestRequest request, final ExecutorService executorService) {
			return CompletableFuture.completedFuture(VALUE.toString() + request.toRawString());
		}
	}

	private static final class ShutdownHandler extends RestRequestHandler<Object> {
		private static final RestRequest BINDING = RestRequest.valueOf("GET /");
		private int shutdownHookInvocationCount = 0;

		private ShutdownHandler() {
			super(BINDING);
		}

		@Override
		public final CompletionStage<Object> handleRequest(final RestRequest request, final ExecutorService executorService) {
			return CompletableFuture.completedFuture("");
		}

		@Override
		protected final void shutdownHook() {
			shutdownHookInvocationCount++;
		}
	}

	public TestRestRequestDispatcher() {
	}

	private final RestRequestDispatcher<Object> newDispatcher() {
		return new RestRequestDispatcher<>(
				mock(Server.class),
				mock(ExecutorService.class),
				ImmutableSet.of(
						new Handler1(),
						new Handler2(),
						new Handler3(),
						new Handler4())
		);
	}

	@Test
	public final void dispatch1() throws Exception {
		final String strRequest = Handler2.BINDING.toRawString() + "/va%20lue/another_value/";
		final RestRequest request = RestRequest.valueOf(strRequest);
		final Object value = newDispatcher().dispatch(request).toCompletableFuture().get();
		assertEquals("Assert correct handling result", Handler2.VALUE + strRequest, value);
	}

	@Test
	public final void dispatch2() throws Exception {
		final RestRequest request = Handler3.BINDING;
		final Object value = newDispatcher().dispatch(request).toCompletableFuture().get();
		assertEquals("Assert correct handling result", Handler3.VALUE + request.toRawString(), value);
	}

	@Test
	public final void dispatch3() throws Exception {
		final String strRequest = Handler4.BINDING.toRawString() + "/a/b/c/";
		final RestRequest request = RestRequest.valueOf(strRequest);
		final Object value = newDispatcher().dispatch(request).toCompletableFuture().get();
		assertEquals("Assert correct handling result", Handler4.VALUE + strRequest, value);
	}

	@Test(expected = BindingNotFoundException.class)
	public final void dispatchFailed() throws Exception {
		final String strRequest = "GET /one/t/";
		final RestRequest request = RestRequest.valueOf(strRequest);
		newDispatcher().dispatch(request).toCompletableFuture().get();
	}

	@Test
	public final void shutdown() {
		final ShutdownHandler handler = new ShutdownHandler();
		final RestRequestDispatcher<Object> dispatcher = new RestRequestDispatcher<>(
				mock(Server.class),
				mock(ExecutorService.class),
				ImmutableSet.of(handler)
		);
		dispatcher.shutdown();
		assertEquals("", false, ((AtomicBoolean)Whitebox.getInternalState(dispatcher, "active")).get());
	}

	@Test
	public final void shutdownIdempotence() {
		final ShutdownHandler handler = new ShutdownHandler();
		final RestRequestDispatcher<Object> dispatcher = new RestRequestDispatcher<>(
				mock(Server.class),
				mock(ExecutorService.class),
				ImmutableSet.of(handler)
		);
		dispatcher.shutdown();
		dispatcher.shutdown();
		assertEquals("Assert shutdownHook was invoked exactly once", 1, handler.shutdownHookInvocationCount);
	}

	@Test(expected = IllegalStateException.class)
	public final void dispatchAfterShutdown() {
		final RestRequestDispatcher<Object> dispatcher = newDispatcher();
		dispatcher.shutdown();
		dispatcher.dispatch(RestRequest.valueOf("GET /"));
	}
}
