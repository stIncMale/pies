package com.gl.vn.me.ko.pies.base.main;

import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.base.config.app.ApplicationConfig;
import com.gl.vn.me.ko.pies.base.config.app.Stage;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of {@link ApplicationConfig}.
 */
@Immutable
@Singleton
final class DefaultApplicationConfig implements ApplicationConfig {
	private final Stage stage;

	/**
	 * Constructs an instance of {@link DefaultApplicationConfig}.
	 *
	 * @param applicationConfig
	 * {@link PropsConfig} that defines properties with names specified in
	 * {@link ApplicationConfigPropertyName}.
	 */
	@Inject
	DefaultApplicationConfig(final PropsConfig applicationConfig) {
		stage = applicationConfig.getStringable(ApplicationConfigPropertyName.STAGE, Stage.converter());
	}

	@Override
	public final Stage getStage() {
		return stage;
	}
}
