package com.gl.vn.me.ko.pies.base.feijoa;

/**
 * Implementation of this interface provides ability to restore object of type {@code T} from {@link String} that was
 * obtained via the method {@link Stringable#toString()}.
 *
 * @param <T>
 * Type which instances can be restored from {@link String} by this {@link StringableConverter}.
 * @see Stringable
 */
public interface StringableConverter<T extends Stringable> {
	/**
	 * Restores object of type {@code T} from the provided {@code stringValue}.
	 *
	 * @param stringValue
	 * A {@link String} returned from {@code T.}{@link Stringable#toString() toString()} method.
	 * @return
	 * Object of type {@code T} such that the following assertion succeeds:
	 * <pre>{@code
	 * MyClass originalObject = ...;// implements Stringable
	 * MyConverter c = ...;// implements StringableConverter<MyClass>
	 * MyClass restoredObject = c.valueOf(originalObject.toString());
	 * assert originalObject.equals(restoredObject);
	 * }</pre>
	 *
	 * @throws StringableConvertationException
	 * If conversion failed.
	 */
	T valueOf(String stringValue) throws StringableConvertationException;
}
