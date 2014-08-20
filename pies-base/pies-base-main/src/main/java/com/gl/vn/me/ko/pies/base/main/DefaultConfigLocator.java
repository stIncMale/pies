package com.gl.vn.me.ko.pies.base.main;

import com.gl.vn.me.ko.pies.base.config.ConfigCreationException;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.base.config.XmlPropsConfig;
import com.gl.vn.me.ko.pies.base.config.app.ConfigLocator;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of {@link ConfigLocator}.
 */
@Immutable
final class DefaultConfigLocator implements ConfigLocator {
	private final Path configsLocation;

	DefaultConfigLocator(final String configsLocation) {
		final Path path = Paths.get(configsLocation);
		this.configsLocation = path;
	}

	@Override
	public final PropsConfig getXmlPropsConfig(final String configFileShortName) throws ConfigCreationException {
		final Path configPath = configsLocation.resolve(configFileShortName);
		return new XmlPropsConfig(configPath, true);
	}
}
