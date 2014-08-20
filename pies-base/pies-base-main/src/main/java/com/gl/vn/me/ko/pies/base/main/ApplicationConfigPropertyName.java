package com.gl.vn.me.ko.pies.base.main;

import com.gl.vn.me.ko.pies.base.config.PropertyName;
import com.gl.vn.me.ko.pies.base.config.app.Stage;
import javax.annotation.concurrent.Immutable;

/**
 * Represents names of properties that are specified by the Application Config.
 * <p>
 * Property MUST be considered mandatory unless otherwise is specified.
 */
@Immutable
public enum ApplicationConfigPropertyName implements PropertyName {
	/**
	 * This property specifies {@link Stage}.
	 * <p>
	 * Name of this property is {@code "stage"}.
	 *
	 * @see Stage
	 */
	STAGE("stage");
	private final String name;

	private ApplicationConfigPropertyName(final String name) {
		this.name = name;
	}

	@Override
	public final String toString() {
		return name;
	}
}
