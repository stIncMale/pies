package com.gl.vn.me.ko.pies.base.main;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.gl.vn.me.ko.pies.base.config.app.ApplicationConfig;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.di.GuiceModule;
import com.gl.vn.me.ko.pies.base.di.GuiceModuleNotFoundException;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to conveniently locate published instances of {@link GuiceModule}.
 * <p>
 * {@link GuiceModule} respects {@link ApplicationConfig#getStage()} when creates instances of {@link Injector}.
 */
public final class GuiceLocator {
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiceLocator.class);
	@Nullable
	private static volatile Stage stage = null;

	private static final class GuiceModuleLoaderInitializer {
		private static final Object GUICE_MODULE_LOCK = new Object();
		private static final ServiceLoader<GuiceModule> GUICE_MODULE_LOADER = ServiceLoader.load(GuiceModule.class);
	}

	/**
	 * Creates a new {@link Injector} by using the provided {@code modules}.
	 *
	 * @param modules
	 * Required modules.
	 * @return
	 * {@link Injector} created by using the provided {@code modules}.
	 */
	public static final Injector createInjector(final AbstractModule... modules) {
		checkNotNull(modules);
		checkState(stage != null);
		final Injector result = Guice.createInjector(stage, modules);
		return result;
	}

	/**
	 * Creates a new {@link Injector} by using modules specified via {@code names}.
	 *
	 * @param names
	 * Names of the required modules.
	 * @return
	 * {@link Injector} created by using the requested instances of {@link GuiceModule}.
	 * @throws GuiceModuleNotFoundException
	 * If lookup of at least one requested {@link GuiceModule} is failed.
	 */
	public static final Injector createInjector(final String... names) throws GuiceModuleNotFoundException {
		checkNotNull(names);
		checkState(stage != null);
		final Injector result = Guice.createInjector(stage, getModules(names));
		return result;
	}

	/**
	 * Returns published instances of {@link GuiceModule} according to the requested {@code names}.
	 * Multiple invocations of the method with the same modules requested will result to the same objects returned, i.e.
	 * each {@link GuiceModule} is instantiated only once.
	 * <p>
	 * It's important that instances of {@link GuiceModule} are requested by name and not by class, because otherwise if
	 * there are API and implementation JAR's, {@link GuiceModule} can only be published by code in implementation JAR
	 * and requesting it by class would create a compile-time dependency on the implementation JAR.
	 *
	 * @param names
	 * Names of the required modules.
	 * @return
	 * Requested modules.
	 * @throws GuiceModuleNotFoundException
	 * If lookup of at least one requested {@link GuiceModule} is failed.
	 */
	public static final Set<GuiceModule> getModules(final String... names) throws GuiceModuleNotFoundException {
		final Set<GuiceModule> result = new HashSet<>();
		final Set<String> moduleNames = new HashSet<>(Arrays.asList(names));
		final Set<GuiceModule> modules = new HashSet<>();
		try {
			synchronized (GuiceModuleLoaderInitializer.GUICE_MODULE_LOCK) {
				for (final GuiceModule module : GuiceModuleLoaderInitializer.GUICE_MODULE_LOADER) {
					if (!moduleNames.isEmpty()) {
						modules.add(module);
						final String moduleName = module.getName();
						if (moduleNames.contains(moduleName)) {
							result.add(module);
							moduleNames.remove(moduleName);
						}
					} else {
						break;
					}
				}
			}
		} catch (final ServiceConfigurationError e) {
			throw new GuiceModuleNotFoundException(
					Message.format("Can't load at least one module of: %s", moduleNames), e);
		} finally {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The following Guice modules were requested by name: {}. "
						+ "The following Guice modules were loaded: {}", Arrays.toString(names), modules);
			}
		}
		if (moduleNames.size() > 0) {
			throw new GuiceModuleNotFoundException(
					Message.format("The following modules wasn't found: %s", moduleNames));
		}
		return result;
	}

	/**
	 * Initializes {@link GuiceLocator}.
	 * This method MUST be called by {@link Main} class
	 * before the first invocation of {@link #createInjector(String...)}.
	 * This method MUST be called only once.
	 *
	 * @param appStage
	 * {@link Stage} that will be used by method {@link #createInjector(String...)}.
	 */
	static final void initialize(final com.gl.vn.me.ko.pies.base.config.app.Stage appStage) {
		checkNotNull(appStage);
		checkState(stage == null, "GuiceLocator was already initialized");
		final Stage guiceStage;
		switch (appStage) {
			case DEVELOPMENT: {
				guiceStage = Stage.DEVELOPMENT;
				break;
			}
			case PRODUCTION: {
				guiceStage = Stage.PRODUCTION;
				break;
			}
			default: {
				throw new ApplicationError(Message.CAN_NEVER_HAPPEN);
			}
		}
		stage = guiceStage;
		if (stage == Stage.PRODUCTION) {//eager load guice modules
			synchronized (GuiceModuleLoaderInitializer.GUICE_MODULE_LOADER) {
				GuiceModuleLoaderInitializer.GUICE_MODULE_LOADER.forEach((guiceModule) -> {
				});
			}
		}
		LOGGER.info("GuiceLocator initialized with stage {}", appStage);
	}

	private GuiceLocator() {
		throw new UnsupportedOperationException(Message.INSTANTIATION_NOT_SUPPORTED);
	}
}
