package com.gl.vn.me.ko.pies.base.feijoa;

/**
 * Objects of this type can be losslessly represented by a {@link String} via the {@link #toString()} method. And can be
 * restored from a {@link String} via the {@link StringableConverter#valueOf(String)} method.
 * <p>
 * It MAY be useful to implement this interface by descendants of {@link Enum}.
 *
 * @see StringableConverter
 */
public interface Stringable {
	/**
	 * Returns {@link String} value of the {@link Stringable}.
	 * The returned value can be used as argument for the method {@link StringableConverter#valueOf(String)}.
	 *
	 * @return
	 * Value of the {@link Stringable}.
	 */
	@Override
	String toString();
}
