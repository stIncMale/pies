package com.gl.vn.me.ko.pies.base.config;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents name of a property.
 * Name identifies a property within a single {@link PropsConfig}.
 */
@ThreadSafe
public interface PropertyName {
	/**
	 * Returns {@link String} representation of the name of a property.
	 * Such representation MAY also be referred to as name of a property.
	 *
	 * @return
	 * Name of a property.
	 */
	@Override
	String toString();
}
