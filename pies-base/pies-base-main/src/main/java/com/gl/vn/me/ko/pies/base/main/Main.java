package com.gl.vn.me.ko.pies.base.main;

import static com.gl.vn.me.ko.pies.base.constant.Message.GUICE_POTENTIALLY_SWALLOWED;
import com.gl.vn.me.ko.pies.base.config.PropsConfig;
import com.gl.vn.me.ko.pies.base.config.app.ApplicationConfig;
import com.gl.vn.me.ko.pies.base.config.app.ConfigLocator;
import com.gl.vn.me.ko.pies.base.config.app.Stage;
import com.gl.vn.me.ko.pies.base.constant.Constant;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.di.GuiceModule;
import com.gl.vn.me.ko.pies.base.main.JavaOption.JavaOptionName;
import com.gl.vn.me.ko.pies.base.thread.ApplicationThread;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Java {@link #main(String[])} method.
 */
public final class Main {
	/**
	 * Implementation of {@link UncaughtExceptionHandler} that is used for Java main thread.
	 */
	@Immutable
	private static final class JavaMainThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {
		private static final MainApplicationThreadUncaughtExceptionHandler INSTANCE
				= new MainApplicationThreadUncaughtExceptionHandler();

		private JavaMainThreadUncaughtExceptionHandler() {
		}

		@Override
		@SuppressFBWarnings(value = "DM_EXIT", justification = "Just as planned")
		public final void uncaughtException(final Thread t, final Throwable e) {
			/*
			 * Throwable SHOULD be handled the simplest and most bulletproof way.
			 */
			e.printStackTrace();
			System.exit(Constant.EXIT_STATUS_ERROR);
		}
	}

	/**
	 * Provides logic of the Main App Thread.
	 *
	 * @see {@link Main#main(String[])}
	 */
	private static final class MainApplicationThreadRunnable implements Runnable {
		@Nullable
		private Thread originalMainThread;

		private MainApplicationThreadRunnable(final Thread originalMainThread) {
			this.originalMainThread = originalMainThread;
		}

		private final void joinOriginalMainThread() {
			final Thread localOriginalMainThread = originalMainThread;
			if (localOriginalMainThread != null) {
				try {
					localOriginalMainThread.join();
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new ApplicationError("Unexpected interrupt is detected");
				} finally {
					originalMainThread = null;
				}
			}
		}

		@Override
		@SuppressFBWarnings(value = "DM_EXIT", justification = "Just as planned")
		public final void run() {
			int exitStatus = Constant.EXIT_STATUS_ERROR;
			Class<? extends App> appClass = null;
			try {
				joinOriginalMainThread();
				final App app = getApplicationImpl();
				appClass = app.getClass();
				LOGGER.info("Start execution of Application logic provided by the {}", appClass);
				app.run();
				exitStatus = Constant.EXIT_STATUS_SUCCESS;
			} catch (final Exception e) {
				LOGGER.error("Exception caught", e);
			}
			if (appClass != null) {
				LOGGER.info("End execution of Application logic provided by the {}", appClass);
			}
			LOGGER.info("Application is going to be terminated with exit status {}", Integer.valueOf(exitStatus));
			System.exit(exitStatus);
		}
	}

	/**
	 * Implementation of {@link UncaughtExceptionHandler} that is used for Main Application Thread.
	 *
	 * @see {@link Main#main(String[])}
	 */
	@Immutable
	private static final class MainApplicationThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {
		private static final MainApplicationThreadUncaughtExceptionHandler INSTANCE
				= new MainApplicationThreadUncaughtExceptionHandler();

		private MainApplicationThreadUncaughtExceptionHandler() {
		}

		@Override
		@SuppressFBWarnings(value = "DM_EXIT", justification = "Just as planned")
		public final void uncaughtException(final Thread t, final Throwable e) {
			/*
			 * There is no sense in checking preconditions
			 * because any exception thrown by this method will be ignored by the JVM.
			 */
			final int exitStatus = Constant.EXIT_STATUS_ERROR;
			final String msgFormat = "Application and %s is going to be terminated with exit status %s because of the %s";
			try {
				Std.errPrintln(Message.format(msgFormat, t, Integer.valueOf(exitStatus),
						Throwables.getStackTraceAsString(e)));
			} finally {
				System.exit(exitStatus);
			}
		}
	}

	/**
	 * This {@link GuiceModule} is aware of the following:
	 * <ul>
	 * <li>{@link Singleton @Singleton} {@link ApplicationConfig}</li>
	 * <li>{@link Singleton @Singleton} {@link ConfigLocator}</li>
	 * <li>{@link Singleton @Singleton} {@link LockFileWriter}</li>
	 * </ul>
	 */
	public static final class MainGuiceModule extends GuiceModule {
		private static final Logger LOGGER = LoggerFactory.getLogger(MainGuiceModule.class);

		/**
		 * Constructor required according to the specification of {@link GuiceModule}.
		 * This constructor MUST NOT be called directly by any Application code.
		 */
		public MainGuiceModule() {
		}

		@Override
		protected final void configure() {
			try {
				bind(ApplicationConfig.class).toInstance(new DefaultApplicationConfig(APPLICATION_CONFIG));
				bind(ConfigLocator.class).toInstance(CONFIG_LOCATOR);
				bind(LockFileWriter.class).toInstance(LOCK_FILE_WRITER);
			} catch (final RuntimeException e) {
				LOGGER.error(GUICE_POTENTIALLY_SWALLOWED, e);
				throw e;
			}
		}
	}
	private static final String JAVA_MAIN_THREAD_NAME = "java-main-thread";
	private static final String SYS_PROPERTY_LOG4J2_CONFIG = "log4j.configurationFile";
	private static final String LOG4J_CONFIG_FILE_NAME = "log4j2.xml";
	private static final String APPLICATION_CONFIG_FILE_NAME = "applicationConfig.xml";
	private static final String APPLICATION_PID_FILE_NAME = "application.pid";
	private static final String APPLICATION_LOCK_FILE_NAME = "application.lock";
	private static final String POSIX_FILE_ATTRIBUTE_VIEW_NAME = "posix";
	private static final ConfigLocator CONFIG_LOCATOR;
	private static final String[] DEPENDENCIES = new String[] {"com.gl.vn.me.ko.pies.base.thread"};
	private static final Injector INJECTOR;
	private static final ThreadFactory THREAD_FACTORY;
	private static final LockFileWriter LOCK_FILE_WRITER;
	/**
	 * Classes in the current package are allowed to fully access data specified in the Application Config file
	 * {@link #APPLICATION_CONFIG_FILE_NAME}. Other classes MAY only access Application Config via
	 * {@link ApplicationConfig}.
	 */
	private static final PropsConfig APPLICATION_CONFIG;
	private static final Logger LOGGER;

	static {
		/*
		 * Order of operations is very important
		 * because access to logging system when it's not configured MAY broke logging system.
		 */
		configureJavaMainThread();
		Std.initialize();
		CONFIG_LOCATOR = new DefaultConfigLocator(JavaOption.getValue(JavaOptionName.CONFIGS_LOCATION));
		// generally Application Config SHOULD NOT be located via the Config Locator, but it's legal from the Main class
		APPLICATION_CONFIG = CONFIG_LOCATOR.getXmlPropsConfig(APPLICATION_CONFIG_FILE_NAME);
		configureLoggingSystem();
		final Logger logger = LoggerFactory.getLogger(Main.class);
		LOGGER = logger;
		GuiceLocator
				.initialize(APPLICATION_CONFIG.getStringable(ApplicationConfigPropertyName.STAGE, Stage.converter()));
		INJECTOR = GuiceLocator.createInjector(DEPENDENCIES);
		THREAD_FACTORY = INJECTOR.getInstance(ThreadFactory.class);//INJECTOR is used to enshure singleton
		addDeleteShutdownHookForApplicationPidFile();
		LOCK_FILE_WRITER = createApplicationLockFile();
		JavaOption.clear();
	}

	/**
	 * Performs a MUST have configuration on the Java main thread.
	 * Invocation of this method MUST be the first action in the static initialization block of the class {@link Main}.
	 */
	private static final void configureJavaMainThread() {
		Thread.currentThread().setUncaughtExceptionHandler(JavaMainThreadUncaughtExceptionHandler.INSTANCE);
		Thread.currentThread().setName(JAVA_MAIN_THREAD_NAME);
	}

	/**
	 * Initializes logging system. Currently Log4J2 is used as logging system implementation.
	 * Logging system MUST NOT be accessed before completion of this method. Otherwise it MAY not work at all or work
	 * incorrectly.
	 */
	private static final void configureLoggingSystem() {
		final Path logConfigPath = FileSystems.getDefault()
				.getPath(JavaOption.getValue(JavaOptionName.CONFIGS_LOCATION), LOG4J_CONFIG_FILE_NAME);
		System.setProperty(SYS_PROPERTY_LOG4J2_CONFIG, logConfigPath.toString());
		Std.outPrintln(Message.format("Logging system was initialized to use config %s", logConfigPath));
	}

	/**
	 * Adds {@link Runtime#addShutdownHook(Thread) shutdown hook} that deletes PID File if it exists.
	 */
	private static final void addDeleteShutdownHookForApplicationPidFile() {
		final String runtimeLocation = JavaOption.getValue(JavaOptionName.RUNTIME_LOCATION);
		Runtime.getRuntime().addShutdownHook(THREAD_FACTORY.newThread(() -> {
			try {
				Files.deleteIfExists(FileSystems.getDefault()
						.getPath(runtimeLocation, APPLICATION_PID_FILE_NAME));
			} catch (final IOException e) {
				throw new ApplicationException(e);
			}
		}));
	}

	/**
	 * Creates a new Lock File and adds {@link Runtime#addShutdownHook(Thread) shutdown hook} that deletes Lock File.
	 *
	 * @return
	 * A {@link LockFileWriter} that can be used to write to Lock File.
	 * This {@link LockFileWriter} will be {@linkplain LockFileWriter#close() closed}
	 * by {@link Runtime#addShutdownHook(Thread) shutdown hook} created by this method.
	 *
	 * @throws ApplicationException
	 * If something goes wrong, particularly if Lock File already exists.
	 */
	private static final LockFileWriter createApplicationLockFile() throws ApplicationException {
		final String runtimeLocation = JavaOption.getValue(JavaOptionName.RUNTIME_LOCATION);
		final boolean posix = FileSystems.getDefault().supportedFileAttributeViews().contains(POSIX_FILE_ATTRIBUTE_VIEW_NAME);
		final FileAttribute<?> fileAttributes;
		if (posix) {
			fileAttributes = PosixFilePermissions.asFileAttribute(
					ImmutableSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
		} else {
			fileAttributes = null;
		}
		final Path lockFilePath = FileSystems.getDefault().getPath(runtimeLocation, APPLICATION_LOCK_FILE_NAME);
		try {
			if (fileAttributes != null) {
				Files.createFile(lockFilePath, fileAttributes);
			} else {
				Files.createFile(lockFilePath);
			}
		} catch (final FileAlreadyExistsException e) {
			throw new ApplicationException(
					Message.format("Application can't be started because Lock File %s exists", lockFilePath), e);
		} catch (final IOException e) {
			throw new ApplicationException(e);
		}
		final Writer writer;
		try {
			writer = Files.newBufferedWriter(lockFilePath, StandardOpenOption.WRITE);
		} catch (final IOException e) {
			throw new ApplicationException(e);
		}
		final LockFileWriter result = new DefaultLockFileWriter(writer);
		addDeleteShutdownHookForApplicationLockFile(result);
		return result;
	}

	/**
	 * Adds {@link Runtime#addShutdownHook(Thread) shutdown hook} that deletes Lock File.
	 */
	private static final void addDeleteShutdownHookForApplicationLockFile(final LockFileWriter lockFileWriter) {
		final String runtimeLocation = JavaOption.getValue(JavaOptionName.RUNTIME_LOCATION);
		Runtime.getRuntime().addShutdownHook(THREAD_FACTORY.newThread(() -> {
			final boolean existed;
			try {
				lockFileWriter.close();
				existed = Files.deleteIfExists(FileSystems.getDefault()
						.getPath(runtimeLocation, APPLICATION_LOCK_FILE_NAME));
			} catch (final IOException e) {
				throw new ApplicationException(e);
			}
			if (!existed) {
				LOGGER.warn("Application Lock File {} doesn't exist", APPLICATION_LOCK_FILE_NAME);
			}
		}));
	}

	/**
	 * Locates implementation of {@link App} via {@link ServiceLoader}.
	 *
	 * @return
	 * Implementation of {@link App}.
	 */
	private static final App getApplicationImpl() {
		final App result;
		final Set<App> applicationImpls = ImmutableSet.copyOf(ServiceLoader.load(App.class));
		if (applicationImpls.isEmpty()) {
			throw new ApplicationError(Message.format("Can't find implementation of %s", App.class));
		} else if (applicationImpls.size() > 1) {
			final Set<Class<?>> applicationImplClasses = new HashSet<>(applicationImpls.size());
			for (final App applicationImpl : applicationImpls) {
				applicationImplClasses.add(applicationImpl.getClass());
			}
			throw new ApplicationError(Message.format("More than one implementation of %s found: %s",
					App.class, applicationImplClasses));
		} else {// exactly one implementation was found
			final App app = applicationImpls.iterator().next();
			result = app;
		}
		return result;
	}

	/**
	 * Entry point of the Application. This method starts a new {@link ApplicationThread} that joins (see
	 * {@link Thread#join()}) original Java main thread, locates an implementation of the {@link App},
	 * constructs an object of
	 * type {@link App} according to found implementation and runs it by executing {@link App#run()} method. Such a
	 * thread is called Main Application Thread and its {@linkplain ApplicationThread#getName() name} is
	 * {@code "pies-main-thread"}.
	 *
	 * @param args
	 * Command-line arguments of the App. This argument is not used.
	 * @see App
	 */
	public static final void main(final String[] args) {
		LOGGER.info("Java main(String[] args) method of the {} was invoked", Main.class);
		final Thread originalMainThread = Thread.currentThread();
		originalMainThread.setUncaughtExceptionHandler(MainApplicationThreadUncaughtExceptionHandler.INSTANCE);
		final Thread applicationMainThread = THREAD_FACTORY.newThread(new MainApplicationThreadRunnable(originalMainThread));
		applicationMainThread.setName("pies-main-thread");
		applicationMainThread.setUncaughtExceptionHandler(MainApplicationThreadUncaughtExceptionHandler.INSTANCE);
		applicationMainThread.start();
		LOGGER.info("Main Application Thread {} was started", applicationMainThread);
	}
}
