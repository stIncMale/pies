package com.gl.vn.me.ko.pies.app.initiator;

import com.gl.vn.me.ko.pies.base.main.App;
import com.gl.vn.me.ko.pies.base.main.GuiceLocator;
import com.gl.vn.me.ko.pies.platform.app.CommonApp;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestServer;
import io.netty.util.concurrent.Future;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of PIES Initiator Application.
 */
@Singleton
public final class InitiatorApplication extends CommonApp {
	private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorApplication.class);

	/**
	 * Constructor required according to the specification of {@link App}.
	 * This constructor MUST NOT be called directly by any Application code.
	 */
	public InitiatorApplication() {
		super(GuiceLocator.createInjector(InitiatorModule.getInstance()));
	}

	/**
	 * Creates and starts Control Server which uses Initiator Client.
	 */
	@Override
	public final void run() {
		final Server controlServer = getInjector().getInstance(JsonRestServer.class);
		try {
			final Future<?> controlServerCompletion = controlServer.start();
			controlServerCompletion.await();
		} catch (final InterruptedException e) {
			LOGGER.info("Interrupt was detected. {} will shut down", controlServer);
			Thread.currentThread().interrupt();
		} finally {
			controlServer.shutdown();
			Thread.currentThread().interrupt();
		}
	}
}
