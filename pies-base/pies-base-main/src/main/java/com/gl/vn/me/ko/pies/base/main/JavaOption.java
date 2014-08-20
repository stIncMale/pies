package com.gl.vn.me.ko.pies.base.main;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.config.PropertyName;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import java.nio.charset.Charset;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Provides a convenient way to access {@code -D} Java options that are needed by {@link Main} class.
 * <p>
 * Option MUST be considered mandatory unless otherwise is specified.
 */
final class JavaOption {
	/**
	 * Represents names of {@code -D} Java options that are presented as system properties at runtime.
	 *
	 * @see System#getProperty(String)
	 */
	@Immutable
	static enum JavaOptionName implements PropertyName {
		/**
		 * Specifies Configs Location.
		 * <p>
		 * Denotes option {@code -Dpies.configsLocation}, which name is {@code "pies.configsLocation"}.
		 */
		CONFIGS_LOCATION("pies.configsLocation"),
		/**
		 * Specifies Runtime Location.
		 * <p>
		 * Denotes option {@code -Dpies.runtimeLocation}, which name is {@code "pies.runtimeLocation"}.
		 */
		RUNTIME_LOCATION("pies.runtimeLocation"),
		/**
		 * Name of a {@link Charset} to use for the {@link System#out} and {@link System#err} streams.
		 * <p>
		 * Denotes option {@code -Dpies.consoleCharset}, which name is {@code "pies.consoleCharset"}.
		 */
		CONSOLE_CHARSET("pies.consoleCharset");
		private final String name;

		private JavaOptionName(final String name) {
			this.name = name;
		}

		@Override
		public final String toString() {
			return name;
		}
	}

	/**
	 * Indicates that the {@code -D} Java option isn't specified.
	 */
	static final class JavaOptionUnspecifiedException extends ApplicationException {
		private static final long serialVersionUID = 0;

		/**
		 * Creates an instance of {@link JavaOptionUnspecifiedException}.
		 */
		private JavaOptionUnspecifiedException() {
		}

		/**
		 * Creates an instance of {@link JavaOptionUnspecifiedException}.
		 *
		 * @param message
		 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
		 */
		private JavaOptionUnspecifiedException(@Nullable final String message) {
			super(message);
		}

		/**
		 * Creates an instance of {@link JavaOptionUnspecifiedException}.
		 *
		 * @param message
		 * The detail message which is saved for later retrieval by the {@link #getMessage()} method.
		 * @param cause
		 * The cause which is saved for later retrieval by the {@link #getCause()} method.
		 */
		private JavaOptionUnspecifiedException(@Nullable final String message, @Nullable final Throwable cause) {
			super(message, cause);
		}

		/**
		 * Creates an instance of {@link JavaOptionUnspecifiedException}.
		 *
		 * @param cause
		 * The cause which is saved for later retrieval by the {@link #getCause()} method.
		 */
		private JavaOptionUnspecifiedException(@Nullable final Throwable cause) {
			super(cause);
		}
	}
	private static boolean cleared = false;
	private static final Object MUTEX = new Object();

	/**
	 * Clears Java system properties that represent {@code -D} Java options specified by {@link JavaOptionName}.
	 * This method MUST be called by {@link Main} class once these properties become useless.
	 */
	static final void clear() {
		synchronized (MUTEX) {
			cleared = true;
			for (final JavaOptionName javaOptionName : JavaOptionName.values()) {
				System.setProperty(javaOptionName.toString(), "");
			}
		}
	}

	/**
	 * Reads value of the Java option specified by {@code optionName} and checks that the value is not {@code null},
	 * that is the option is set.
	 *
	 * @param optionName
	 * Name of {@code -D} Java option to read.
	 * @return
	 * Value of the Java option specified by {@code optionName} if the option is set.
	 * @throws JavaOptionUnspecifiedException
	 * If the Java option specified by {@code optionName} isn't set.
	 */
	static final String getValue(final JavaOptionName optionName)
			throws JavaOptionUnspecifiedException {
		checkNotNull(optionName, Message.ARGUMENT_NULL_SINGLE, "optionName");
		final String result;
		synchronized (MUTEX) {
			checkState(!cleared, "Can't get value because options were cleared");
			@Nullable
			final String optionValue = System.getProperty(optionName.toString());
			if (optionValue == null) {
				throw new JavaOptionUnspecifiedException(Message.format(
						"-D Java option %s is not specified", optionName));
			} else {
				result = optionValue;
			}
		}
		return result;
	}

	private JavaOption() {
		throw new UnsupportedOperationException(Message.OPERATION_NOT_SUPPORTED);
	}
}
