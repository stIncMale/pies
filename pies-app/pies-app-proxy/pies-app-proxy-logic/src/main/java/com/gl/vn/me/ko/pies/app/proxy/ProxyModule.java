package com.gl.vn.me.ko.pies.app.proxy;

import static com.gl.vn.me.ko.pies.base.constant.Message.GUICE_POTENTIALLY_SWALLOWED;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.base.config.app.ConfigLocator;
import com.gl.vn.me.ko.pies.base.main.GuiceLocator;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestRequestHandlerResult;
import com.gl.vn.me.ko.pies.platform.server.rest.JsonRestServer;
import com.gl.vn.me.ko.pies.platform.server.rest.RestRequestHandler;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerAddress;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerBoss;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerName;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerRequestHandling;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerThreadFactory;
import com.gl.vn.me.ko.pies.platform.server.rest.RestServerWorker;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServer;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerBackEndAddress;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerBoss;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerConnectTimeout;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerFrontEndAddress;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerName;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerThreadFactory;
import com.gl.vn.me.ko.pies.platform.server.tcp.TcpReverseProxyServerWorker;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractModule} that SHOULD be used throughout PIES Proxy Application.
 */
final class ProxyModule extends AbstractModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyModule.class);
	private static final ProxyModule INSTANCE = new ProxyModule();
	private static final String[] DEPENDENCIES = new String[] {
		"com.gl.vn.me.ko.pies.base.main",
		"com.gl.vn.me.ko.pies.base.thread"};
	/**
	 * Name of the file that specifies Proxy Config.
	 */
	private static final String CONFIG_FILE_NAME = "proxyConfig.xml";

	final static ProxyModule getInstance() {
		return INSTANCE;
	}

	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
			justification = "The method is called from configure() method, FindBugs just don't understand lambdas")
	private static final ChannelInitializer<ServerSocketChannel> createServerChannelInitializer() {
		final ChannelInitializer<ServerSocketChannel> result;
		try {
			result = new ChannelInitializer<ServerSocketChannel>() {
				@Override
				protected final void initChannel(final ServerSocketChannel channel) throws Exception {
					final ChannelPipeline pipeline = channel.pipeline();
					final ChannelHandler channelHandler;
					if (LoggerFactory.getLogger(ProxyApplication.class).isDebugEnabled()) {
						channelHandler = new LoggingHandler(LogLevel.DEBUG);
					} else {
						channelHandler = new LoggingHandler(LogLevel.INFO);
					}
					pipeline.addLast(channelHandler);
				}
			};
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	private ProxyModule() {
	}

	@SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON",
			justification = "It's more readable to use anonimous class for binding instead of a static one")
	@Override
	protected final void configure() {
		try {
			for (final AbstractModule module : GuiceLocator.getModules(DEPENDENCIES)) {
				install(module);
			}
			bind(JsonBuilderFactory.class).toInstance(Json.createBuilderFactory(null));
			bind(String.class).annotatedWith(TcpReverseProxyServerName.class).toInstance("Proxy Server");
			bind(String.class).annotatedWith(RestServerName.class).toInstance("Control Server");
			bind(TcpReverseProxyServer.class).in(Singleton.class);
			bind(JsonRestServer.class).in(Singleton.class);
			bind(new TypeLiteral<ChannelInitializer<ServerSocketChannel>>() {
			})
					.annotatedWith(RestServerBoss.class)
					.toProvider((Provider<ChannelInitializer<ServerSocketChannel>>)ProxyModule::createServerChannelInitializer)
					.in(Singleton.class);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
	}

	@Provides
	@Singleton
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final PropsConfig provideConfig(final ConfigLocator configLocator) {
		final PropsConfig result;
		try {
			result = configLocator.getXmlPropsConfig(CONFIG_FILE_NAME);
			ProxyConfigPropertyName.validate(result, CONFIG_FILE_NAME);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@RestServerAddress
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final InetSocketAddress provideControlSrvAddress(final PropsConfig cfg) {
		final InetSocketAddress result;
		try {
			final int port = cfg.getInteger(ProxyConfigPropertyName.CONTROL_PORT).intValue();
			final Optional<String> optHostPropertyValue = cfg.getString(ProxyConfigPropertyName.CONTROL_HOST, null);
			final InetAddress host;
			try {
				host = optHostPropertyValue.isPresent()
						? InetAddress.getByName(optHostPropertyValue.get()) : InetAddress.getLocalHost();
			} catch (final UnknownHostException e) {
				throw new ApplicationException(e);
			}
			result = new InetSocketAddress(host, port);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@RestServerBoss
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Integer provideControlSrvMaxBossThreads(final PropsConfig cfg) {
		final Integer result;
		try {
			result = cfg.getInteger(ProxyConfigPropertyName.CONTROL_ACCEPTORS);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@RestServerRequestHandling
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Integer provideControlSrvMaxDispatcherThreads(final PropsConfig cfg) {
		final Integer result;
		try {
			result = cfg.getInteger(ProxyConfigPropertyName.CONTROL_POST_RESPONSE_WORKERS);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@RestServerWorker
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Integer provideControlSrvMaxWorkerThreads(final PropsConfig cfg) {
		final Integer result;
		try {
			result = cfg.getInteger(ProxyConfigPropertyName.CONTROL_WORKERS);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@RestServerRequestHandling
	@Nullable
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Collection<? extends RestRequestHandler<? extends JsonRestRequestHandlerResult>>
			provideControlSrvRestRequestHandlers(final JsonBuilderFactory jsonBuilderFactory,
					final TcpReverseProxyServer proxyServer) {
		final Collection<? extends RestRequestHandler<? extends JsonRestRequestHandlerResult>> result;
		try {
			final ImmutableList.Builder<RestRequestHandler<JsonRestRequestHandlerResult>> resultBuilder
					= ImmutableList.builder();
			resultBuilder.add(new ProxyShutdownRestRequestHandler(proxyServer, jsonBuilderFactory));
			result = resultBuilder.build();
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@RestServerThreadFactory
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final ThreadFactory provideControlSrvThreadFactory(final ThreadFactory threadFactory) {
		final ThreadFactory result;
		try {
			result = threadFactory;
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@TcpReverseProxyServerFrontEndAddress
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final InetSocketAddress provideProxySrvFeAddress(final PropsConfig cfg) {
		final InetSocketAddress result;
		try {
			final int port = cfg.getInteger(ProxyConfigPropertyName.PROXY_FE_PORT).intValue();
			final Optional<String> optHostPropertyValue = cfg.getString(ProxyConfigPropertyName.PROXY_FE_HOST, null);
			final InetAddress host;
			try {
				host = optHostPropertyValue.isPresent()
						? InetAddress.getByName(optHostPropertyValue.get()) : InetAddress.getLocalHost();
			} catch (final UnknownHostException e) {
				throw new ApplicationException(e);
			}
			result = new InetSocketAddress(host, port);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@TcpReverseProxyServerBackEndAddress
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final InetSocketAddress provideProxySrvBeAddress(final PropsConfig cfg) {
		final InetSocketAddress result;
		try {
			final int port = cfg.getInteger(ProxyConfigPropertyName.PROXY_BE_PORT).intValue();
			final Optional<String> optHostPropertyValue = cfg.getString(ProxyConfigPropertyName.PROXY_BE_HOST, null);
			final InetAddress host;
			try {
				host = optHostPropertyValue.isPresent()
						? InetAddress.getByName(optHostPropertyValue.get()) : InetAddress.getLocalHost();
			} catch (final UnknownHostException e) {
				throw new ApplicationException(e);
			}
			result = new InetSocketAddress(host, port);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@TcpReverseProxyServerBoss
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Integer provideProxySrvMaxBossThreads(final PropsConfig cfg) {
		final Integer result;
		try {
			result = cfg.getInteger(ProxyConfigPropertyName.PROXY_ACCEPTORS);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@Singleton
	@TcpReverseProxyServerWorker
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Integer provideProxySrvMaxWorkerThreads(final PropsConfig cfg) {
		final Integer result;
		try {
			result = cfg.getInteger(ProxyConfigPropertyName.PROXY_WORKERS);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@TcpReverseProxyServerThreadFactory
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final ThreadFactory provideProxySrvThreadFactory(final ThreadFactory threadFactory) {
		final ThreadFactory result;
		try {
			result = threadFactory;
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}

	@Provides
	@TcpReverseProxyServerConnectTimeout
	@Singleton
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This method is called by Guice framework")
	private final Integer providProxySrvConnectTimeoutMillis(final PropsConfig cfg) {
		final Integer result;
		try {
			result = cfg.getInteger(ProxyConfigPropertyName.PROXY_IO_TIMEOUT_MILLIS);
		} catch (final RuntimeException e) {
			LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
			throw e;
		}
		return result;
	}
}
