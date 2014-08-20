package com.gl.vn.me.ko.pies.base.thread;

import io.netty.util.internal.ThreadLocalRandom;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * All Application threads SHOULD be instances of this class unless a {@link java.util.concurrent.ForkJoinWorkerThread}
 * is
 * required. {@link ApplicationThreadFactory} MUST be used to obtain instance of this class.
 */
@ThreadSafe
public class ApplicationThread extends Thread {
	/**
	 * Constructs an instance of {@link ApplicationThread}.
	 *
	 * @param target
	 * Object which {@link Runnable#run()} method is invoked when this thread is started.
	 * If {@code target} is {@code null} then the {@link ApplicationThread#run()} method of the constructed
	 * {@link ApplicationThread} does nothing.
	 */
	ApplicationThread(@Nullable final Runnable target) {
		super(target);
	}

	/**
	 * Acts like {@link Thread#run()} method, but executes WA for
	 * <a href="https://github.com/netty/netty/issues/2170">Netty ThreadLocalRandom bug</a> before invocation of the
	 * {@link Thread#run()} method.
	 */
	@Override
	public final void run() {
		/*
		 * This is a WA for Netty ThreadLocalRandom bug https://github.com/netty/netty/issues/2170.
		 * TODO remove this WA when bug will be fixed
		 * (changes are in master and most likely will be published in the 5.0.0.Alpha2).
		 */
		ThreadLocalRandom.current();
		super.run();
	}

	/**
	 * Returns a description of the {@link ApplicationThread}.
	 * This description is prefixed with {@code "ApplicationThread"} and contains thread name, thread priority,
	 * and thread group name, e.g. {@code "ApplicationThread[pies-thread-main, 5, main]"}.
	 *
	 * @return
	 * A description of the {@link ApplicationThread}.
	 */
	@Override
	public final String toString() {
		final ThreadGroup group = getThreadGroup();
		final String groupName = group != null ? group.getName() : null;
		final StringBuilder sb = new StringBuilder(getClass().getName())
				.append("(name=").append(getName())
				.append(", priority=").append(getPriority())
				.append(", group=").append(groupName).append(')');
		final String result = sb.toString();
		return result;
	}
}
