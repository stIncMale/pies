package com.gl.vn.me.ko.pies.base.throwable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import org.junit.Test;

@SuppressFBWarnings(value = "NM_CLASS_NOT_EXCEPTION", justification = "It's name of a test class")
public final class TestExternallyVisibleException {
	private static final String SERIAL_DATA_FILE = "serializedFormExternallyVisibleException.ser";
	private static final String EXTERNAL_MESSAGE = "extMsg";

	public TestExternallyVisibleException() {
	}

	@Test
	public final void serializeDeserialize() throws IOException, ClassNotFoundException {
		final ExternallyVisibleException originalObject = new ExternallyVisibleException(EXTERNAL_MESSAGE);
		final byte[] serialData;
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream(0)) {
			try (final ObjectOutput objOut = new ObjectOutputStream(out)) {
				objOut.writeObject(originalObject);
			}
			serialData = out.toByteArray();
		}
		final ExternallyVisibleException deserializedObject;
		try (final InputStream in = new ByteArrayInputStream(serialData)) {
			try (final ObjectInput objIn = new ObjectInputStream(in)) {
				deserializedObject = (ExternallyVisibleException)objIn.readObject();
			}
		}
		assertEquals("Assert that object was deserialized correctly", EXTERNAL_MESSAGE, deserializedObject.getMessage());
	}

	@Test
	public final void serializedForm() throws URISyntaxException, IOException, ClassNotFoundException {
		@Nullable
		final URL serialDataUrl = TestExternallyVisibleException.class.getResource(SERIAL_DATA_FILE);
		if (serialDataUrl != null) {
			final Path serialDataPath = Paths.get(serialDataUrl.toURI());
			final byte[] serialData = Files.readAllBytes(serialDataPath);
			final ExternallyVisibleException deserializedObject;
			try (final InputStream in = new ByteArrayInputStream(serialData)) {
				try (final ObjectInput objIn = new ObjectInputStream(in)) {
					deserializedObject = (ExternallyVisibleException)objIn.readObject();
				}
			}
			assertEquals("Assert that object was deserialized correctly", EXTERNAL_MESSAGE, deserializedObject.getMessage());
		} else {
			fail(String.format("Can't get resource %s", SERIAL_DATA_FILE));
		}
	}
}
