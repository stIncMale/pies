package com.gl.vn.me.ko.pies.platform.app;

import com.gl.vn.me.ko.pies.base.config.app.ApplicationConfig;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.main.App;
import com.gl.vn.me.ko.pies.base.main.LockFileWriter;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerAddress;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import java.net.InetSocketAddress;

/**
 * Provides a common {@link App} logic for all Applications.
 *
 * @see App
 */
public abstract class CommonApp implements App {
	private static final String PROPERTY_NAME_NETTY_LEAK_DETECTION_LEVEL = "io.netty.leakDetectionLevel";

	static {
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	}

	private final Injector injector;

	/**
	 * Constructor of {@link CommonApp}.
	 *
	 * @param injector
	 * An {@link Injector} that MUST be aware of the following:
	 * <ul>
	 * <li>{@literal @}{@link InetSocketAddress} {@link RestServerAddress} of a Control Server.
	 * This Control Server MUST accept {@code "PUT /shutdown/"} HTTP requests and MUST react on such request
	 * by shutting down the Application which it controls.
	 * </ul>
	 */
	protected CommonApp(final Injector injector) {
		this.injector = injector;
		{//write Control Server address to the Lock File in the form of host:port
			final InetSocketAddress controlServerAddress
					= injector.getInstance(Key.get(InetSocketAddress.class, RestServerAddress.class));
			final LockFileWriter lockFileWriter = injector.getInstance(LockFileWriter.class);
			lockFileWriter.write(controlServerAddress.getHostName() + ":" + controlServerAddress.getPort());
		}
		{//process Application stage
			final ResourceLeakDetector.Level nettyLeakDetectionLevel;
			switch (getConfig().getStage()) {
				case DEVELOPMENT: {
					nettyLeakDetectionLevel = ResourceLeakDetector.Level.PARANOID;
					break;
				}
				case PRODUCTION: {
					nettyLeakDetectionLevel = ResourceLeakDetector.Level.SIMPLE;
					break;
				}
				default: {
					throw new ApplicationError(Message.CAN_NEVER_HAPPEN);
				}
			}
			/*
			 * ResourceLeakDetector.setLevel(nettyLeakDetectionLevel) isn't used
			 * because in this case Netty logs incorrect leak detection level.
			 */
			System.setProperty(PROPERTY_NAME_NETTY_LEAK_DETECTION_LEVEL, nettyLeakDetectionLevel.toString());
		}
	}

	/**
	 * Returns the {@link Injector} specified to construct this {@link CommonApp}.
	 *
	 * @return
	 * The {@link Injector} specified to construct this {@link CommonApp}.
	 */
	protected final Injector getInjector() {
		return injector;
	}

	/**
	 * Returns the {@link ApplicationConfig}.
	 *
	 * @return
	 * The {@link ApplicationConfig}.
	 */
	protected final ApplicationConfig getConfig() {
		final ApplicationConfig cfg = injector.getInstance(ApplicationConfig.class);
		return cfg;
	}
}
