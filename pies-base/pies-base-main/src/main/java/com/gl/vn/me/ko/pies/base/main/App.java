package com.gl.vn.me.ko.pies.base.main;

import java.util.ServiceLoader;
import javax.inject.Singleton;

/**
 * Represents refined Application logic, that is logic without processing of command-line arguments and some other routines that
 * need to be done before the pure Application logic can be executed.
 * <p>
 * Implementation of this interface is located by the {@link Main} class via {@link ServiceLoader}, and therefore implementation
 * of the {@link App} MUST be public, MUST have a public constructor without parameters, and a provider-configuration file MUST be
 * available as specified in the {@link ServiceLoader} documentation. There MUST be exactly one published implementation of
 * {@link App}. The described constructor MUST NOT be called directly by any Application code.
 * <p>
 * The {@link Main} class will invoke method {@link App#run()} on the object constructed via the mentioned constructor.
 * Note that there is a guarantee that only one instance of the class that implements {@link App} will be created
 * and the {@link App#run()} method will be called only once.
 * <p>
 * Implementation example:
 * <pre><code>
 * public final class HelloWorld implements App {
 *  private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorld.class);
 *
 *  private final ApplicationConfig applicationConfig;
 *
 *  public HelloWorld() {
 *  	final Injector injector = GuiceLocator.createInjector("com.gl.vn.me.ko.pies.base.main");
 *  	applicationConfig = injector.getInstance(ApplicationConfig.class);
 *  }
 *
 *  {@literal @}Override
 *  public final void run() {
 *  	LOGGER.info("Hello World! I'm running in the {} stage", applicationConfig.getStage());
 *  }
 * }
 * </code></pre>
 *
 * @see Main#main(String[])
 */
@Singleton
public interface App extends Runnable {
	/**
	 * Method that contains Application logic. This method is invoked by the {@link Main} class. After return from this method JVM
	 * will be shut down by using {@link System#exit(int)} method, so if there are any threads started by the method (including
	 * those started indirectly) that require graceful shutdown, the method SHOULD take care of them.
	 */
	@Override
	void run();
}
