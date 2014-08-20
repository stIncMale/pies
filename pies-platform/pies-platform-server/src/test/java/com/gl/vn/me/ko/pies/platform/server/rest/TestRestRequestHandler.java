package com.gl.vn.me.ko.pies.platform.server.rest;

import static com.gl.vn.me.ko.pies.platform.server.rest.RestRequest.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import org.junit.Test;

public final class TestRestRequestHandler {
	private static final class Handler extends RestRequestHandler<Object> {
		private static final RestRequest BINDING = RestRequest.valueOf("GET /b1/b2/");
		private static final Object VALUE = new Object();
		private int shutdownHookInvocationCount = 0;

		private Handler() {
			super(BINDING);
		}

		@Override
		public final CompletionStage<Object> handleRequest(final RestRequest request, ExecutorService executorService) {
			return CompletableFuture.completedFuture(VALUE);
		}

		@Override
		protected final void shutdownHook() {
			shutdownHookInvocationCount++;
		}
	}

	public TestRestRequestHandler() {
	}

	@Test
	public final void getBinding() {
		final Handler handler = new Handler();
		assertEquals("Assert that binding is correct", Handler.BINDING, handler.getBinding());
	}

	@Test
	public final void getParams() {
		final Handler handler = new Handler();
		final List<String> expectedParams = ImmutableList.of("p1", "p2");
		final RestRequest request
				= RestRequest.valueOf(Handler.BINDING + expectedParams.get(0) + "/" + expectedParams.get(1) + "/");
		final List<String> actualParams = handler.getArguments(request);
		assertEquals("Assert that params are correct", expectedParams, actualParams);
	}

	@Test
	public final void shutdown() {
		final Handler handler = new Handler();
		handler.shutdown();
		assertEquals("Assert handler isn't active", false, handler.getState());
	}

	@Test
	public final void shutdownIdempotence() {
		final Handler handler = new Handler();
		handler.shutdown();
		handler.shutdown();
		assertEquals("Assert shutdownHook was invoked exactly once", 1, handler.shutdownHookInvocationCount);
	}

	@Test(expected = IllegalStateException.class)
	public final void handleAfterShutdown() {
		final Handler handler = new Handler();
		handler.shutdown();
		handler.handle(valueOf("GET /"), mock(ExecutorService.class));
	}

	@Test(expected = IllegalStateException.class)
	public final void associateAfterShutdown() {
		final Handler handler = new Handler();
		handler.shutdown();
		handler.associate(new RestRequestDispatcher<>(mock(Server.class), mock(ExecutorService.class), null));
		handler.handle(valueOf("GET /"), mock(ExecutorService.class));
	}
}
