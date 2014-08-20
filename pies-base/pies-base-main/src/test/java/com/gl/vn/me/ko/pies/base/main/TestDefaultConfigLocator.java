package com.gl.vn.me.ko.pies.base.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.gl.vn.me.ko.pies.base.config.PropertyName;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.base.config.app.ConfigLocator;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.Test;

public final class TestDefaultConfigLocator {
	private enum TestPropertyName implements PropertyName {
		PROPERTY_TEST("pies.base.main.test");
		private final String name;

		private TestPropertyName(final String name) {
			this.name = name;
		}

		@Override
		public final String toString() {
			return name;
		}
	}

	private static final String PROPERTY_TEST_VALUE = "test";
	private static final String CONFIG_FILE_NAME = "testDefaultConfigLocator.xml";

	private final ConfigLocator configLocator;

	public TestDefaultConfigLocator() throws URISyntaxException {
		final URL configsLocationUrl = TestDefaultConfigLocator.class.getResource(CONFIG_FILE_NAME);
		final String configsLocation
				= Paths.get(configsLocationUrl.toURI()).getParent().toString();
		configLocator = new DefaultConfigLocator(configsLocation);
	}

	@Test
	public final void getXmlPropsConfig() {
		final PropsConfig config = configLocator.getXmlPropsConfig(CONFIG_FILE_NAME);
		assertNotNull("Assert that located config is not null", config);
		assertEquals(
				"Assert that located config is correct", PROPERTY_TEST_VALUE,
				config.getString(TestPropertyName.PROPERTY_TEST));
	}
}
