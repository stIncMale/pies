package com.gl.vn.me.ko.pies.platform.server;

import io.netty.util.concurrent.Future;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents an I/O server.
 */
@ThreadSafe
public interface Server {
	/**
	 * Performs a shutdown procedure.
	 * After completion of this method {@link Server} doesn't perform I/O and any other operations anymore.
	 * This method MUST be idempotent.
	 *
	 * @see #start()
	 */
	void shutdown();

	/**
	 * Performs a start procedure.
	 * After completion of this method {@link Server} is able to perform I/O operations until {@link #shutdown()} is
	 * invoked.
	 * <p>
	 * Once the {@link #start()} method is called the {@link #shutdown()} method MUST be called later. A suggested
	 * approach to start, wait and shutdown a {@link Server} is following:
	 * <pre><code>
	 * final Server server = ...
	 * try {
	 * 	final Future&lt;?&gt; completion = server.start();
	 * 	... // do something else if you need to
	 * 	completion.await();
	 * } catch (final InterruptedException e) {
	 * 	// shutdown() called in finally block is our reaction on interruption
	 * 	Thread.currentThread().interrupt();
	 * } finally {
	 * 	server.shutdown();
	 * }
	 * </code></pre>
	 * Two invocations of the {@link #start()} method on the same {@link Server} MUST be separated with
	 * {@link #shutdown()} method invocation. {@link Server} implementation MUST explicitly specify if it supports
	 * multiple {@link #start()} {@literal &} {@link #shutdown()} procedures, otherwise one MUST NOT invoke {@link #start()}
	 * method more than one time for a given {@link Server} instance.
	 *
	 * @return
	 * {@link Future} that MAY be used to {@link Future#await() wait} till the {@link Server} operates.
	 * After completion of this {@link Future} the method {@link #shutdown()} MUST be called in order
	 * to correctly shutdown the {@link Server}.
	 * @throws InterruptedException
	 * If the current {@link Thread} is interrupted.
	 * @see #shutdown()
	 */
	Future<?> start() throws InterruptedException;
}
