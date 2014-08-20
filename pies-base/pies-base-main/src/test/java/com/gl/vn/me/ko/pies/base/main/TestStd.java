package com.gl.vn.me.ko.pies.base.main;

import static org.junit.Assert.assertTrue;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.main.JavaOption.JavaOptionName;
import com.gl.vn.me.ko.pies.base.main.Std.StreamType;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public final class TestStd {
	private static final String CHARSET_NAME = "UTF-8";

	@BeforeClass
	public static final void setUp() {
		/*
		 * Std class will not be successfully initialized without this property.
		 */
		System.setProperty(JavaOptionName.CONSOLE_CHARSET.toString(), CHARSET_NAME);
	}

	@AfterClass
	public static final void tearDown() {
		System.setProperty(JavaOptionName.CONSOLE_CHARSET.toString(), "");
	}

	public TestStd() {
	}

	@Test
	public final void println() throws UnsupportedEncodingException {
		final ByteArrayOutputStream data = new ByteArrayOutputStream();
		final PrintStream printStream = new PrintStream(data, false, CHARSET_NAME);
		final String message = "Test message";
		final StreamType streamType = StreamType.STDERR;
		final String threadName = Thread.currentThread().getName();
		Std.println(message, streamType, printStream);
		final String printedMessage = data.toString(CHARSET_NAME);
		final Pattern regex = Pattern.compile("^"
				+ "\\[" + streamType.toString() + "\\] "
				+ "\\[[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}[\\+-]{1}[0-9]{2}:?[0-9]{2}\\] "
				+ "\\[" + threadName + "\\] - "
				+ message + "\\s*$");
		final Matcher regexMatcher = regex.matcher(printedMessage);
		assertTrue(Message.format("Assert that printed message is correct. Printed message is '%s'", printedMessage),
				regexMatcher.matches());
	}
}
