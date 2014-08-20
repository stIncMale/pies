package com.gl.vn.me.ko.pies.base.feijoa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public final class TestExecutorUtil {
	public TestExecutorUtil() {
	}

	@Test(expected = RuntimeException.class)
	public final void unshutdownableShutdown() {
		final ExecutorService unshutdownableExecutorService = ExecutorUtil.unshutdownable(Executors.newSingleThreadExecutor());
		unshutdownableExecutorService.shutdown();
	}

	@Test(expected = RuntimeException.class)
	public final void unshutdownableShutdownNow() {
		final ExecutorService unshutdownableExecutorService = ExecutorUtil.unshutdownable(Executors.newSingleThreadExecutor());
		unshutdownableExecutorService.shutdownNow();
	}

	@Test
	@SuppressWarnings("SleepWhileInLoop")
	@SuppressFBWarnings(
			value = {"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", "DLS_DEAD_LOCAL_STORE"}, justification = "This is just a test")
	public final void shutdownGracefullyWithUninterruptibleTask() {
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		final AtomicBoolean stopUninterruptible = new AtomicBoolean(false);
		{//submit tasks
			final CountDownLatch interruptibleTasksSubmitLatch = new CountDownLatch(1);
			{//submit uninterruptible task that will consume one thread forever
				executorService.submit(() -> {
					interruptibleTasksSubmitLatch.countDown();
					while (!stopUninterruptible.get()) {
						try {
							Thread.sleep(10);
						} catch (final InterruptedException e) {
							//swallow
						}
					}
				});
			}
			{//submit interruptible tasks
				try {
					interruptibleTasksSubmitLatch.await();//wait till the uninterruptible task will be started
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					fail("Unexpected interruption");
				}
				for (int i = 1; i < 10; i++) {
					final int sleepTime = i * 1000;//sleep varies from 1 second to 10 seconds
					executorService.submit(() -> {
						try {
							Thread.sleep(sleepTime);
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					});
				}
			}
		}
		ExecutorUtil.shutdownGracefully(executorService, 100, 900, TimeUnit.MILLISECONDS);
		assertFalse("Assert that executor service wasn't terminated",
				executorService.isTerminated());//because of uninterruptible task
		stopUninterruptible.set(true);
		ExecutorUtil.shutdownGracefully(executorService, 0, 100, TimeUnit.MILLISECONDS);
		assertTrue("Assert that executor service was terminated", executorService.isTerminated());
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "This is just a test")
	public final void shutdownGracefullyWithoutUninterruptibleTasks() {
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		{//submit task
			executorService.submit(() -> {
				try {
					Thread.sleep(10_000);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
		}
		ExecutorUtil.shutdownGracefully(executorService, 100, 900, TimeUnit.MILLISECONDS);
		assertTrue("Assert that executor service was terminated", executorService.isTerminated());
	}
}
