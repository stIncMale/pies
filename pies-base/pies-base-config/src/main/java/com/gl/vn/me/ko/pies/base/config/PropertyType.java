package com.gl.vn.me.ko.pies.base.config;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Represents type of a property.
 *
 * @param T
 * A Java type of value of a property.
 */
@Immutable
public final class PropertyType<T> {
	/**
	 * Corresponds to {@link BigDecimal}.
	 */
	public static final PropertyType<BigDecimal> BIG_DECIMAL = new PropertyType<>("BigDecimal", 1);
	/**
	 * Corresponds to {@link BigInteger}.
	 */
	public static final PropertyType<BigInteger> BIG_INTEGER = new PropertyType<>("BigInteger", 2);
	/**
	 * Corresponds to {@link Boolean}.
	 */
	public static final PropertyType<Boolean> BOOLEAN = new PropertyType<>("Boolean", 3);
	/**
	 * Corresponds to {@link Double}.
	 */
	public static final PropertyType<Double> DOUBLE = new PropertyType<>("Double", 4);
	/**
	 * Corresponds to {@link Integer}.
	 */
	public static final PropertyType<Integer> INTEGER = new PropertyType<>("Integer", 5);
	/**
	 * Corresponds to {@link Long}.
	 */
	public static final PropertyType<Long> LONG = new PropertyType<>("Long", 6);
	/**
	 * Corresponds to {@link String}.
	 */
	public static final PropertyType<String> STRING = new PropertyType<>("String", 7);
	/**
	 * Corresponds to {@link List}{@code <}{@link String}{@code >}.
	 * A type of a multiple property.
	 */
	public static final PropertyType<List<String>> LIST_OF_STRINGS = new PropertyType<>("List<String>", 8);
	/**
	 * Corresponds to {@link Set}{@code <}{@link String}{@code >}.
	 * A type of a multiple property.
	 */
	public static final PropertyType<Set<String>> SET_OF_STRINGS = new PropertyType<>("Set<String>", 9);
	private static final Set<PropertyType<?>> TYPES = ImmutableSet.of(
			BIG_DECIMAL,
			BIG_INTEGER,
			BOOLEAN,
			DOUBLE,
			INTEGER,
			LONG,
			STRING,
			LIST_OF_STRINGS,
			SET_OF_STRINGS);

	/**
	 * Returns a {@link Set} of all possible {@link PropertyType}s.
	 *
	 * @return
	 * An unmodifiable {@link Set} of all possible {@link PropertyType}s.
	 */
	static final Set<PropertyType<?>> types() {
		return TYPES;
	}

	private final String name;
	private final int id;

	private PropertyType(final String name, final int id) {
		this.name = name;
		this.id = id;
	}

	/**
	 * Casts the specified {@code object} the Java type that corresponds to the {@link PropertyType}.
	 *
	 * @param object
	 * An {@link Object} to cast.
	 * @return
	 * {@code object} that was cast.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	final T cast(final @Nullable Object object) {
		return (T)object;
	}

	/**
	 * Returns a description of the {@link PropertyType}.
	 *
	 * @return
	 * A description of the {@link PropertyType}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(name=").append(name)
				.append(", id=").append(id).append(')');
		final String result = sb.toString();
		return result;
	}

	/**
	 * Tests if the specified {@code object} represents the same {@link PropertyType} as this {@link PropertyType}.
	 *
	 * @param object
	 * {@code Object} to test.
	 * @return
	 * {@code true} if the specified {@code object} is equal to this {@link PropertyType} and {@code false} otherwise.
	 */
	@SuppressFBWarnings(
			value = "NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION", justification = "Object.equals(...) allows null arguments")
	@Override
	public final boolean equals(@Nullable final Object object) {
		final boolean result;
		if (this == object) {
			result = true;
		} else {
			if (object instanceof PropertyType) {
				final PropertyType<?> propertyType = (PropertyType<?>)object;
				result = this.id == propertyType.id;
			} else {
				result = false;
			}
		}
		return result;
	}

	@Override
	public final int hashCode() {
		return id;
	}
}
