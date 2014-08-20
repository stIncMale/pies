package com.gl.vn.me.ko.pies.base.main;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Constant;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.main.JavaOption.JavaOptionName;
import com.gl.vn.me.ko.pies.base.main.JavaOption.JavaOptionUnspecifiedException;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nullable;

/**
 * Provides methods for printing to {@link System#out} and {@link System#err} that append some useful information to
 * printed strings.
 * <p>
 * In case of using {@link Std}{@code .outPrintln(...)} and {@link Std}{@code .errPrintln(...)} methods a {@code PREFIX}
 * is prepended that contains information on the stream and date. Format of the {@code PREFIX} is
 * {@code "[STREAM] [DATE] [THREAD_NAME] - "}, where
 * <ul>
 * <li>{@code STREAM} is {@link StreamType#STDOUT}{@code .}{@link StreamType#toString() toString()} for
 * {@link System#out} and {@link StreamType#STDERR} {@code .}{@link StreamType#toString() toString()} for
 * {@link System#err}</li>
 * <li>{@code DATE} represents date formatted with {@link DateTimeFormatter} constructed with pattern
 * {@code "yyyy-MM-dd'T'HH:mm:ss.SSSZ"} and {@link Constant#LOCALE}</li>
 * <li>{@code THREAD_NAME} represents {@linkplain Thread#getName() name} of the {@link Thread} that prints to standard
 * stream</li>
 * </ul>
 * <p>
 * This class also substitutes {@link System#out} and {@link System#err} streams during initialization, which is
 * initiated from the {@link Main} class initialization. Streams are substituted with streams that use a special
 * {@link Charset} specific to the Application (specified by the {@code -Dpies.consoleCharset} Java option, don't
 * confuse it with {@link Constant#CHARSET}).
 * <p>
 * One SHOULD use {@link Std} to print to standard streams
 * and SHOULD NOT use {@link System#out} and {@link System#err} directly.
 */
public final class Std {
	/**
	 * Represents type of a standard stream.
	 */
	public static enum StreamType {
		/**
		 * Designates {@link System#out} stream.
		 */
		STDOUT,
		/**
		 * Designates {@link System#err} stream.
		 */
		STDERR;

		private final PrintStream getStream() {
			final PrintStream result;
			switch (this) {
				case STDOUT: {
					final PrintStream stream = System.out;
					result = stream;
					break;
				}
				case STDERR: {
					final PrintStream stream = System.err;
					result = stream;
					break;
				}
				default: {
					throw new ApplicationError(Message.CAN_NEVER_HAPPEN);
				}
			}
			return result;
		}
	}
	private static final DateTimeFormatter DATE_FORMATTER
			= DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Constant.LOCALE);

	static {
		configureOutStreams();
	}

	/**
	 * Substitutes {@link System#out} and {@link System#err} streams with streams that use {@link Charset} specified by
	 * the {@link JavaOptionName#CONSOLE_CHARSET}.
	 */
	private static final void configureOutStreams() {
		final String consoleCharset;
		try {
			consoleCharset = JavaOption.getValue(JavaOptionName.CONSOLE_CHARSET);
			final PrintStream out = new PrintStream(System.out, true, consoleCharset);
			final PrintStream err = new PrintStream(System.err, true, consoleCharset);
			System.setOut(out);
			System.setErr(err);
		} catch (final JavaOptionUnspecifiedException | UnsupportedEncodingException e) {
			throw new ApplicationException("Failed to configure standard output and error streams", e);
		}
		Std.outPrintln(Message.format("Standard output streams was configured to use %s charset", consoleCharset));
	}

	/**
	 * Acts as {@code java.lang.System.err.println()}.
	 */
	public static final void errPrintln() {
		System.out.println();
	}

	/**
	 * Acts as {@link System#err}{@code .}{@link PrintStream#println println}{@code (PREFIX + x)}.
	 * See documentation of the class {@link Std} for the description of {@code PREFIX}.
	 *
	 * @param x
	 * The {@link String} to be printed.
	 */
	public static final void errPrintln(@Nullable final String x) {
		println(x, StreamType.STDERR);
	}

	private static final StringBuilder getPrefix(final StreamType type) {
		final StringBuilder result = new StringBuilder();
		result.append("[").append(type.toString()).append("] [")
				.append(DATE_FORMATTER.format(ZonedDateTime.now())).append("] [")
				.append(Thread.currentThread().getName()).append("] - ");
		return result;
	}

	/**
	 * Initiates {@link Std} class initialization.
	 */
	static final void initialize() {
		// empty body is enough
	}

	/**
	 * Acts as {@code java.lang.System.out.println()}.
	 */
	public static final void outPrintln() {
		System.out.println();
	}

	/**
	 * Acts as {@link System#out}{@code .}{@link PrintStream#println println}{@code (PREFIX + x)}.
	 * See documentation of the class {@link Std} for the description of {@code PREFIX}.
	 *
	 * @param x
	 * The {@link String} to be printed.
	 */
	public static final void outPrintln(@Nullable final String x) {
		println(x, StreamType.STDOUT);
	}

	private static final void println(@Nullable final String x, final StreamType type) {
		println(x, type, type.getStream());
	}

	/**
	 * Acts as {@link #outPrintln(String)} or {@link #errPrintln(String)} but constructs {@code PREFIX} according to the
	 * specified {@code type} and prints to the provided {@code stream}. This method is useless for most cases.
	 *
	 * @param x
	 * The {@link String} to be printed.
	 * @param type
	 * Type of the {@code stream}.
	 * @param stream
	 * {@link PrintStream} to use for printing {@code x}.
	 */
	public static final void println(@Nullable final String x, final StreamType type, final PrintStream stream) {
		checkNotNull(type, Message.ARGUMENT_NULL, "second", "type");
		checkNotNull(type, Message.ARGUMENT_NULL, "third", "stream");
		stream.println(getPrefix(type)
				.append(x)
				.toString());
	}

	private Std() {
		throw new UnsupportedOperationException(Message.INSTANTIATION_NOT_SUPPORTED);
	}
}
