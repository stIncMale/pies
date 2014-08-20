package com.gl.vn.me.ko.pies.base.thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.concurrent.ThreadFactory;
import org.junit.Test;

public final class TestThreadGuiceModule {
	private final Injector threadGuiceModuleInjector;

	public TestThreadGuiceModule() {
		threadGuiceModuleInjector = Guice.createInjector(new ThreadGuiceModule());
	}

	@Test
	public final void testSingletonThreadFactory() {
		final ThreadFactory threadFactory1 = threadGuiceModuleInjector.getInstance(ThreadFactory.class);
		assertTrue("Assert type", threadFactory1 instanceof ApplicationThreadFactory);
		final ThreadFactory threadFactory2 = threadGuiceModuleInjector.getInstance(ThreadFactory.class);
		assertEquals("Assert singleton", threadFactory1, threadFactory2);
	}
}
