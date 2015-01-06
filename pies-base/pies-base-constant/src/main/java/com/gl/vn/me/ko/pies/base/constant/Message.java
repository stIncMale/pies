package com.gl.vn.me.ko.pies.base.constant;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.lang.reflect.Constructor;
import java.util.Locale;

/**
 * Provides Application's internal string resources that allow to specify common messages in a generic way.
 * Some string values contain {@code %s} macroses that SHOULD be substituted
 * by using {@link Message#format(String, Object...)} method or {@link com.google.common.base.Preconditions} methods.
 * <p>
 * Examples of usage:
 * <pre>{@code
 * //the message is "The second argument arg2 is illegal"
 * Preconditions.checkArgument(arg2 >= 0, Message.ARGUMENT_ILLEGAL, "second", "arg2");
 *
 * //the message is "The argument arg is null"
 * Preconditions.checkNotNull(arg, Message.ARGUMENT_NULL_SINGLE, "arg");
 * }</pre>
 */
public final class Message {
	/**
	 * Specifies impossibility of constructor call.
	 * Example of usage:
	 * <pre>{@code
	 * throw new UnsupportedOperationException(Message.INSTANTIATION_NOT_SUPPORTED);
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String INSTANTIATION_NOT_SUPPORTED = "The class is not designed to be instantiated";
	/**
	 * Specifies impossibility of method invocation.
	 * Example of usage:
	 * <pre>{@code
	 * throw new UnsupportedOperationException(Message.OPERATION_NOT_SUPPORTED);
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String OPERATION_NOT_SUPPORTED = "The method is not supported";
	/**
	 * Specifies that an argument of a method is {@code null}.
	 * Example of usage:
	 * <pre>{@code
	 * Preconditions.checkNotNull(arg, Message.ARGUMENT_NULL, "first", "arg");
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String ARGUMENT_NULL = "The %s argument %s is null";
	/**
	 * Specifies that the only argument of a method is {@code null}.
	 * Example of usage:
	 * <pre>{@code
	 * Preconditions.checkNotNull(arg, Message.ARGUMENT_NULL_SINGLE, "arg");
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String ARGUMENT_NULL_SINGLE = "The argument %s is null";
	/**
	 * Specifies that an argument of a method is illegal.
	 * Example of usage:
	 * <pre>{@code
	 * Preconditions.checkArgument(arg > 0 && arg < 3, Message.ARGUMENT_ILLEGAL, arg, "first", "arg",
	 * 		"Expected value must be greater than 0 and lower than 3");
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String ARGUMENT_ILLEGAL = "Value %s of the %s argument %s is illegal. %s";
	/**
	 * Specifies that the only argument of a method is illegal.
	 * Example of usage:
	 * <pre>{@code
	 * Preconditions.checkArgument(size > 2, Message.ARGUMENT_ILLEGAL, size, "size",
	 * 		"Expected set must contain at least two elements");
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String ARGUMENT_ILLEGAL_SINGLE = "Value %s of the argument %s is illegal. %s";
	/**
	 * Specifies that instantiation of a class via the {@link Class#newInstance()} method failed.
	 * Example of usage:
	 * <pre>{@code
	 * throw new ApplicationException(Message.format(Message.INSTANTIATION_FAILED, klass.toString()));
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String INSTANTIATION_FAILED = "Failed to instantiate class %s";
	/**
	 * Specifies that instantiation of a class via the {@link Constructor#newInstance(Object...)} method failed.
	 * Example of usage:
	 * <pre>{@code
	 * throw new ApplicationException(Message.format(Message.INSTANTIATION_FAILED_CTOR, ctor.toString()));
	 * }</pre>
	 * Value of this constant is {@value} .
	 */
	public static final String INSTANTIATION_FAILED_CTOR = "Failed to instantiate class via constructor %s";
	/**
	 * Specifies message for exceptional situation that "can never happen".
	 * E.g. execution of a {@code default} block in a {@code switch} statement that tests for each value of some
	 * {@code enum}. It's possible that a new value was added to the {@code enum} without updating the {@code switch}
	 * statement, but this is very unlikely and, even if it will happen, there will be no need in any
	 * runtime information for investigation of the problem.
	 * <p>
	 * Example of usage:
	 * <pre>{@code
	 * throw new ApplicationError(Message.CAN_NEVER_HAPPEN);
	 * }</pre>
	 * Value of this constant is {@value}.
	 */
	public static final String CAN_NEVER_HAPPEN
			= "Surprise Motherfucker! You see a error that could never happen. There are two possibilities:"
			+ " the End of the World is coming, or something is very wrong with the Application";
	/**
	 * Specifies message for situation when one had to swallow a {@link Throwable}.
	 * <p>
	 * Example of usage:
	 * <pre>{@code
	 * LOGGER.error(Message.SWALLOWED, swallowedException);
	 * }</pre>
	 * Value of this constant is {@value}.
	 */
	public static final String SWALLOWED = "This exception was swallowed and logged only";
	/**
	 * Specifies message that MAY be used to describe {@link Throwable} logged from implementation of {@link AbstractModule}.
	 * The thing is that sometimes (I can't figure out strict test case) Guice swallows exceptions that were thrown
	 * from methods marked with {@link Provides} and exits JVM.
	 * So it's RECOMMENDED to catch any possible {@link RuntimeException} and log it before rethrowing.
	 */
	public static final String GUICE_POTENTIALLY_SWALLOWED = "This exception is logged because Guice may swallow it";

	/**
	 * This method SHOULD be used to format constants provided by the {@link Message} class as well as any other
	 * strings that isn't user-specific.
	 * The method calls {@link String#format(Locale, String, Object...)} by specifying {@link Constant#LOCALE} as
	 * {@link Locale} and therefore guarantees that the formatted string will not depends on the system locale.
	 * If your {@code format} doesn't contain any locale-specific format specifiers then you MAY use
	 * {@link String#format(String, Object...)} directly (e.g. this is useful when using {@link Preconditions}).
	 *
	 * @param format
	 * A format string as specified in {@link String#format(Locale, String, Object...)}.
	 * @param args
	 * Arguments referenced by the format specifiers in the format string.
	 * See {@link String#format(Locale, String, Object...)} for details.
	 * @return
	 * A formatted string.
	 */
	public static final String format(final String format, final Object... args) {
		checkNotNull(format, Message.ARGUMENT_NULL, "first", "format");
		final String result = String.format(Constant.LOCALE, format, args);
		return result;
	}

	private Message() {
		throw new UnsupportedOperationException(Message.INSTANTIATION_NOT_SUPPORTED);
	}
}
