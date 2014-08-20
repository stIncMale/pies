package com.gl.vn.me.ko.pies.base.thread;

import static com.gl.vn.me.ko.pies.base.constant.Message.GUICE_POTENTIALLY_SWALLOWED;
import com.gl.vn.me.ko.pies.base.di.GuiceModule;
import java.util.concurrent.ThreadFactory;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link GuiceModule} is aware of the following:
 * <ul>
 * <li>{@link Singleton @Singleton} {@link ThreadFactory}</li>
 * </ul>
 */
public final class ThreadGuiceModule extends GuiceModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadGuiceModule.class);

	/**
	 * Constructor required according to the specification of {@link GuiceModule}.
	 * This constructor MUST NOT be called directly by any Application code.
	 */
	public ThreadGuiceModule() {
	}

	@Override
	protected final void configure() {
		try {
			bind(ThreadFactory.class).to(ApplicationThreadFactory.class);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
	}
}
