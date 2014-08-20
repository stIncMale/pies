package com.gl.vn.me.ko.pies.base.thread;

import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.main.Std;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.annotation.concurrent.Immutable;

/**
 * This {@link UncaughtExceptionHandler} SHOULD be used for all threads where it possible besides the "main" thread.
 */
@Immutable
public final class ApplicationUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private static final ApplicationUncaughtExceptionHandler INSTANCE = new ApplicationUncaughtExceptionHandler();

	static final ApplicationUncaughtExceptionHandler getInstance() {
		return INSTANCE;
	}

	private ApplicationUncaughtExceptionHandler() {
	}

	/**
	 * This method is called from {@link #uncaughtException(Thread, Throwable)} and is required only for testing.
	 * The method MAY be used in tests and MUST NOT be used elsewhere.
	 *
	 * @param t
	 * The thread that is going to be terminated because of the uncaught {@link Throwable} {@code e}.
	 * @param e
	 * The uncaught {@link Throwable}.
	 * @param printStream
	 * {@link PrintStream} to use for printing information about {@code t} and {@code e}.
	 * Information is printed by using {@link Std#println(String, Std.StreamType, PrintStream)} method.
	 */
	@VisibleForTesting
	final void processUncaughtException(final Thread t, final Throwable e, final PrintStream printStream) {
		final String msgFormat = "%s is going to be terminated because of the %s";
		Std.println(Message.format(msgFormat, t, Throwables.getStackTraceAsString(e)), Std.StreamType.STDERR, printStream);
	}

	/**
	 * Prints information about the thread that is going to be terminated and the cause to {@link System#err}.
	 *
	 * @param t
	 * The thread that is going to be terminated because of the uncaught {@link Throwable} {@code e}.
	 * @param e
	 * The uncaught {@link Throwable}.
	 */
	/*
	 * This method MUST delegate all work to the processUncaughtException(...) method for the sake of testability.
	 */
	@Override
	public final void uncaughtException(final Thread t, final Throwable e) {
		/*
		 * There is no sense in checking preconditions
		 * because any exception thrown by this method will be ignored by JVM.
		 */
		final PrintStream errStream = System.err;
		processUncaughtException(t, e, errStream);
	}
}
