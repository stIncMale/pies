package com.gl.vn.me.ko.pies.base.thread;

import com.gl.vn.me.ko.pies.base.constant.Message;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This factory SHOULD be used by the Application whenever it needs a {@link ThreadFactory} or needs to create
 * a new {@link Thread} unless a {@link java.util.concurrent.ForkJoinWorkerThread} is required.
 * <p>
 * If you need a factory that creates threads with other name convention for example, consider using
 * {@link com.google.common.util.concurrent.ThreadFactoryBuilder}.
 */
@Singleton
@ThreadSafe
public final class ApplicationThreadFactory implements ThreadFactory {
	private static final AtomicLong INDEX = new AtomicLong();

	@Inject
	ApplicationThreadFactory() {
	}

	/**
	 * Creates a new instance of {@link ApplicationThread}.
	 * The created {@link ApplicationThread} is not a daemon, has a {@link Thread#NORM_PRIORITY} priority,
	 * {@link ApplicationUncaughtExceptionHandler} as uncaught exception handler and {@code "pies-thread-%d"} as a name,
	 * where {@code %d} is replaced with an index (starting from 1) of this method invocation among all objects of type
	 * {@link ApplicationThreadFactory}.
	 *
	 * @param target
	 * Object which {@link Runnable#run()} method is invoked when the {@link ApplicationThread} is
	 * started. If {@code target} is {@code null} then the {@link ApplicationThread#run()} method of the
	 * constructed {@link ApplicationThread} does nothing.
	 * @return
	 * Constructed {@link ApplicationThread}.
	 */
	@Override
	public final ApplicationThread newThread(@Nullable final Runnable target) {
		final ApplicationThread result = new ApplicationThread(target);
		result.setDaemon(false);
		result.setPriority(Thread.NORM_PRIORITY);
		result.setUncaughtExceptionHandler(ApplicationUncaughtExceptionHandler.getInstance());
		result.setName(Message.format("pies-thread-%d", Long.valueOf(INDEX.incrementAndGet())));
		return result;
	}
}
