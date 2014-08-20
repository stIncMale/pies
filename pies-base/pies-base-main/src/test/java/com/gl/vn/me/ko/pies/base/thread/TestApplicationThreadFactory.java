package com.gl.vn.me.ko.pies.base.thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.gl.vn.me.ko.pies.base.constant.Message;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.regex.Pattern;
import org.junit.Test;

public final class TestApplicationThreadFactory {
	final ApplicationThreadFactory factory;
	final Runnable dummyRunnable;

	public TestApplicationThreadFactory() {
		factory = new ApplicationThreadFactory();
		dummyRunnable = () -> {
		};
	}

	@Test
	public final void newThreadDaemon() {
		final ApplicationThread thread = factory.newThread(dummyRunnable);
		assertFalse("Assert that thread daemon status is correct", thread.isDaemon());
	}

	@Test
	public final void newThreadName() {
		final ApplicationThread thread = factory.newThread(dummyRunnable);
		final String name = thread.getName();
		assertTrue(
				Message.format("Assert that thread name %s is correct", name),
				Pattern.matches("^pies-thread-[0-9]+$", name));
	}

	@Test
	public final void newThreadNullRunnable() throws InterruptedException {
		final ApplicationThread thread = factory.newThread(null);
		thread.start();
		thread.join();
	}

	@Test
	public final void newThreadPriority() {
		final ApplicationThread thread = factory.newThread(dummyRunnable);
		assertEquals("Assert that thread priority is correct", Thread.NORM_PRIORITY, thread.getPriority());
	}

	@Test
	public final void newThreadUncaughtExceptionHandler() {
		final ApplicationThread thread = factory.newThread(dummyRunnable);
		final UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
		assertTrue(
				Message.format("Assert that thread uncaught exception handler %s is correct", handler),
				handler instanceof ApplicationUncaughtExceptionHandler);
	}
}
