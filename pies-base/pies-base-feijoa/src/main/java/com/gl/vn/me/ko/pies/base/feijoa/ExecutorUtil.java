package com.gl.vn.me.ko.pies.base.feijoa;

import static com.gl.vn.me.ko.pies.base.constant.Message.ARGUMENT_ILLEGAL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class provides various utility methods related to {@link Executor}s.
 */
public final class ExecutorUtil {
	/**
	 * Performs a graceful shutdown of the supplied {@code executorService}.
	 *
	 * @param executorService
	 * An {@link ExecutorService} to shutdown.
	 * @param quietTime
	 * During this amount of time {@code executorService} will continue to execute previously submitted tasks
	 * but no new tasks will be accepted (see {@link ExecutorService#shutdown()}).
	 * If {@code quietTime} has elapsed and {@code executorService} isn't {@linkplain ExecutorService#isTerminated() terminated}
	 * then {@link ExecutorService#shutdownNow()} is invoked followed by the
	 * {@link ExecutorService#awaitTermination(long, TimeUnit) awaitTermination(timeout - quietTime, timeUnit)} method.
	 * <p>
	 * This argument MUST NOT be greater than {@code timeout}.
	 * @param timeout
	 * The total amount of time this method is allowed to spend till return.
	 * @param timeUnit
	 * The time unit of the time arguments.
	 * @return
	 * {@link List} of tasks that never commenced execution (see {@link ExecutorService#shutdownNow()}).
	 *
	 */
	public static final List<Runnable> shutdownGracefully(
			final ExecutorService executorService,
			final long quietTime,
			final long timeout,
			final TimeUnit timeUnit) {
		checkNotNull(executorService, Message.ARGUMENT_NULL, "first", "executorService");
		checkArgument(quietTime >= 0, ARGUMENT_ILLEGAL, quietTime, "second", "quietTime", "Expected value must be nonnegative");
		checkArgument(timeout >= 0, ARGUMENT_ILLEGAL, timeout, "third", "timeout", "Expected value must be nonnegative");
		checkArgument(quietTime <= timeout, ARGUMENT_ILLEGAL, quietTime, "second", "quietTime",
				"Expected value must not be greater than the third argument");
		checkNotNull(timeUnit, Message.ARGUMENT_NULL, "fourth", "timeUnit");
		final List<Runnable> result;
		boolean interrupted = false;
		executorService.shutdown();
		try {
			executorService.awaitTermination(quietTime, timeUnit);
		} catch (final InterruptedException e) {
			interrupted = true;
			Thread.currentThread().interrupt();
			executorService.shutdownNow();
		}
		result = executorService.shutdownNow();
		if (!interrupted) {
			try {
				executorService.awaitTermination(timeout - quietTime, timeUnit);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return result;
	}

	/**
	 * Shortcut method for {@link #shutdownGracefully(ExecutorService, long, long, TimeUnit)} with sensible default values.
	 *
	 * @param executorService
	 * An {@link ExecutorService} to shutdown.
	 * @return
	 * {@link List} of tasks that never commenced execution (see {@link ExecutorService#shutdownNow()}).
	 */
	public static final List<Runnable> shutdownGracefully(final ExecutorService executorService) {
		return shutdownGracefully(executorService, 1, 2, TimeUnit.SECONDS);
	}

	/**
	 * Returns an unshutdownable view of the specified {@code executorService}.
	 * Unshutdownable means that methods {@link ExecutorService#shutdown()} and {@link ExecutorService#shutdownNow()} always
	 * throw an unchecked {@link Exception}.
	 *
	 * @param executorService
	 * An {@link ExecutorService} for which an unshutdownable view is to be returned.
	 * @return
	 * An unshutdownable view of the specified {@code executorService}.
	 */
	public static final ExecutorService unshutdownable(final ExecutorService executorService) {
		checkNotNull(executorService, Message.ARGUMENT_NULL_SINGLE, "executorService");
		return new UnshutdownableExecutorService(executorService);
	}
}
