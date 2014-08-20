package com.gl.vn.me.ko.pies.app.echo;

import com.gl.vn.me.ko.pies.base.main.App;
import com.gl.vn.me.ko.pies.base.main.GuiceLocator;
import com.gl.vn.me.ko.pies.platform.app.CommonApp;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestServer;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpServer;
import com.google.inject.Injector;
import io.netty.util.concurrent.Future;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of PIES Echo Application.
 */
@Singleton
public final class EchoApplication extends CommonApp {
	private static final Logger LOGGER = LoggerFactory.getLogger(EchoApplication.class);

	/**
	 * Constructor required according to the specification of {@link App}.
	 * This constructor MUST NOT be called directly by any Application code.
	 */
	public EchoApplication() {
		super(GuiceLocator.createInjector(EchoModule.getInstance()));
	}

	/**
	 * Creates and starts Echo and Control Servers.
	 */
	@Override
	public final void run() {
		final Injector injector = getInjector();
		final Server echoServer = injector.getInstance(TcpServer.class);
		try {
			final Future<?> echoServerCompletion = echoServer.start();
			final Server controlServer = injector.getInstance(JsonRestServer.class);
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
			echoServerCompletion.await();
		} catch (final InterruptedException e) {
			LOGGER.info("Interrupt was detected. {} will shut down", echoServer);
			Thread.currentThread().interrupt();
		} finally {
			echoServer.shutdown();
		}
	}
}
