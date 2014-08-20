package com.gl.vn.me.ko.pies.app.proxy;

import com.gl.vn.me.ko.pies.base.main.App;
import com.gl.vn.me.ko.pies.base.main.GuiceLocator;
import com.gl.vn.me.ko.pies.platform.app.CommonApp;
import com.gl.vn.me.ko.pies.platform.server.Server;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestServer;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServer;
import com.google.inject.Injector;
import io.netty.util.concurrent.Future;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of PIES Proxy Application.
 */
@Singleton
public final class ProxyApplication extends CommonApp {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyApplication.class);

	/**
	 * Constructor required according to the specification of {@link App}.
	 * This constructor MUST NOT be called directly by any Application code.
	 */
	public ProxyApplication() {
		super(GuiceLocator.createInjector(ProxyModule.getInstance()));
	}

	/**
	 * Creates and starts Proxy and Control Servers.
	 */
	@Override
	public final void run() {
		final Injector injector = getInjector();
		final Server proxyServer = injector.getInstance(TcpReverseProxyServer.class);
		try {
			final Future<?> proxyServerCompletion = proxyServer.start();
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
			proxyServerCompletion.await();
		} catch (final InterruptedException e) {
			LOGGER.info("Interrupt was detected. {} will shut down", proxyServer);
			Thread.currentThread().interrupt();
		} finally {
			proxyServer.shutdown();
		}
	}
}
