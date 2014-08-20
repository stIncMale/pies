package com.gl.vn.me.ko.pies.base.di;

import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.main.GuiceLocator;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import java.util.ServiceLoader;

/**
 * Represents {@link AbstractModule} that is provided as part of API.
 * <p>
 * Published instances of {@link GuiceModule} are accessed via {@link ServiceLoader}, and therefore implementation of
 * the {@link GuiceModule} MUST be public, MUST have a public constructor without parameters, and a
 * provider-configuration file MUST be available as specified in the {@link ServiceLoader} documentation. The described
 * constructor MUST NOT be called directly by any Application code.
 * <p>
 * It's expected that each project will locate only the required published instances of {@link GuiceModule} via
 * {@link GuiceLocator} and create an {@link Injector} that MUST only be available for that project. Instances of
 * {@link GuiceModule} MUST be accessed only via {@link GuiceLocator} even from project that publish the
 * {@link GuiceModule}.
 * <p>
 * {@link GuiceModule} SHOULD be documented to specify everything that can be requested from the {@link Injector} that
 * is based on the {@link GuiceModule}, and to specify {@linkplain #getName() names} of {@link GuiceModule}s it depends on.
 *
 * @see GuiceLocator
 */
public abstract class GuiceModule extends AbstractModule {
	/**
	 * Constructor of {@link GuiceModule}.
	 */
	protected GuiceModule() {
	}

	/**
	 * Returns name of the {@link GuiceModule}. Name is equal to name of the Java package that provides
	 * {@link GuiceModule}, so any Java package MAY only publish a single {@link GuiceModule}, because name MUST be unique.
	 *
	 * @return
	 * Name of the {@link GuiceModule}.
	 */
	public final String getName() {
		final Class<? extends GuiceModule> klass = this.getClass();
		final Package pckg = klass.getPackage();
		final String result;
		if (pckg == null) {
			throw new ApplicationError(Message.format("Unable to return name of the guice module %s", klass));
		} else {
			final String pckgName = pckg.getName();
			result = pckgName;
		}
		return result;
	}

	/**
	 * Returns a description of the {@link GuiceModule}.
	 *
	 * @return
	 * A description of the {@link GuiceModule}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(name=").append(getName()).append(')');
		final String result = sb.toString();
		return result;
	}
}
