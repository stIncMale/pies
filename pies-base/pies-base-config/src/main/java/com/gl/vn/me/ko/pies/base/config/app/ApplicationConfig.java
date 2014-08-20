package com.gl.vn.me.ko.pies.base.config.app;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents Application Config.
 */
@ThreadSafe
public interface ApplicationConfig {
	/**
	 * Returns a {@link Stage} the Application is running in.
	 *
	 * @return
	 * A {@link Stage} the Application is running in.
	 */
	Stage getStage();
}
