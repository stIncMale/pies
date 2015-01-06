package com.gl.vn.me.ko.pies.base.feijoa;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An unshutdownable view of an {@link ExecutorService}.
 * Unshutdownable means that methods {@link ExecutorService#shutdown()} and {@link ExecutorService#shutdownNow()}
 * always throw an unchecked {@link Exception}.
 */
@ThreadSafe
final class UnshutdownableExecutorService implements ExecutorService {
	private final ExecutorService executorService;

	UnshutdownableExecutorService(final ExecutorService executorService) {
		checkNotNull(executorService, Message.ARGUMENT_NULL_SINGLE, "executorService");
		this.executorService = executorService;
	}

	@Override
	public final boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
		return executorService.awaitTermination(timeout, unit);
	}

	@Override
	public final void execute(final Runnable command) {
		executorService.execute(command);
	}

	@Override
	public final <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executorService.invokeAll(tasks);
	}

	@Override
	public final <T> List<Future<T>> invokeAll(
			final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
		return executorService.invokeAll(tasks, timeout, unit);
	}

	@Override
	public final <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return executorService.invokeAny(tasks);
	}

	@Override
	public final <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return executorService.invokeAny(tasks, timeout, unit);
	}

	@Override
	public final boolean isShutdown() {
		return executorService.isShutdown();
	}

	@Override
	public final boolean isTerminated() {
		return executorService.isTerminated();
	}

	/**
	 * Always throws an unchecked {@link Exception}.
	 *
	 * @throws UnsupportedOperationException
	 * Always.
	 */
	@Override
	public final void shutdown() {
		throw new UnsupportedOperationException("The method isn't supported");
	}

	/**
	 * Always throws an unchecked {@link Exception}.
	 *
	 * @return
	 * Never return a value.
	 * @throws UnsupportedOperationException
	 * Always.
	 */
	@Override
	public final List<Runnable> shutdownNow() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("The method isn't supported");
	}

	@Override
	public final <T> Future<T> submit(final Callable<T> task) {
		return executorService.submit(task);
	}

	@Override
	public final <T> Future<T> submit(final Runnable task, final T result) {
		return executorService.submit(task, result);
	}

	@Override
	public final Future<?> submit(final Runnable task) {
		return executorService.submit(task);
	}

	/**
	 * Returns a description of the {@link UnshutdownableExecutorService}.
	 *
	 * @return
	 * A description of the {@link UnshutdownableExecutorService}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(getClass().getName())
				.append("(executorService=").append(executorService).append(')');
		return sb.toString();
	}
}
