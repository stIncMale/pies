package com.gl.vn.me.ko.pies.base.thread;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public final class TestApplicationUncaughtExceptionHandler {
	private static final String SYS_PROPERTY_CONSOLE_CHARSET = "pies.consoleCharset";
	private static final String CHARSET_NAME = "UTF-8";

	@BeforeClass
	public static final void setUp() {
		/*
		 * Std class will not be successfully initialized without this property.
		 */
		System.setProperty(SYS_PROPERTY_CONSOLE_CHARSET, CHARSET_NAME);
	}

	@AfterClass
	public static final void tearDown() {
		System.setProperty(SYS_PROPERTY_CONSOLE_CHARSET, "");
	}

	public TestApplicationUncaughtExceptionHandler() {
	}

	@Test
	public final void uncaughtException() throws UnsupportedEncodingException {
		final ApplicationUncaughtExceptionHandler handler = ApplicationUncaughtExceptionHandler.getInstance();
		final ByteArrayOutputStream data = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(data, false, CHARSET_NAME);
		final String exceptionMessage = "Test uncaught exception";
		final Thread t = new ApplicationThreadFactory().newThread(null);
		final String threadDescription = t.toString();
		final String escapedThreadDescription = threadDescription.replace("(", "\\(").replace(")", "\\)");
		try {
			throw new RuntimeException(exceptionMessage);
		} catch (final Throwable e) {
			handler.processUncaughtException(t, e, out);
		}
		final String handlerMessage = data.toString(CHARSET_NAME);
		final Pattern regex = Pattern.compile("^"
				+ ".*" + escapedThreadDescription + ".*"
				+ exceptionMessage
				+ ".*TestApplicationUncaughtExceptionHandler\\.uncaughtException.*$", Pattern.DOTALL);
		final Matcher regexMatcher = regex.matcher(handlerMessage);
		assertTrue("Assert that handler message is correct", regexMatcher.matches());
	}
}
