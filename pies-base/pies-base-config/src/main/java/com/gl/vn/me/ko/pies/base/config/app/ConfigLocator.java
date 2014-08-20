package com.gl.vn.me.ko.pies.base.config.app;

import com.gl.vn.me.ko.pies.base.config.ConfigCreationException;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides a way to access Application's configs located in Configs Location.
 * <p>
 * Note that the Application Config SHOULD NOT be located via this API. In order to access Application Config request
 * injection of {@link ApplicationConfig}.
 */
@ThreadSafe
public interface ConfigLocator {
	/**
	 * Finds configuration specified by the {@code configFileShortName}, reads, validates if possible and creates a new
	 * {@link PropsConfig} that represents configuration.
	 *
	 * @param configFileShortName
	 * Name of the XML configuration file without other path parts.
	 * E.g. {@code "applicationConfig.xml"}.
	 * @return
	 * A newly constructed {@link PropsConfig} that represents the requested configuration.
	 * @throws ConfigCreationException
	 * If creation of configuration is failed.
	 */
	PropsConfig getXmlPropsConfig(final String configFileShortName) throws ConfigCreationException;
}
