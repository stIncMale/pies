package com.gl.vn.me.ko.pies.base.config;

import com.gl.vn.me.ko.pies.base.feijoa.Stringable;
import com.gl.vn.me.ko.pies.base.feijoa.StringableConverter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents a configuration that consists of properties.
 * <p>
 * A property is an entity that is identified by {@link PropertyName} and MAY have a value. Value of a property MUST be
 * consistent with the {@link PropertyType}. A value of a property MAY be treated as some other {@link PropertyType}
 * if it's possible to convert the value to that type.
 * A property can either be single or multiple. Single properties MAY only have a single value
 * and multiple properties MAY have multiple values.
 * <p>
 * A property is undefined if it is not specified in the configuration or its value is {@code null}.
 */
@ThreadSafe
public interface PropsConfig {
	/**
	 * Returns value of a property of the type {@code type}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param type
	 * {@link PropertyType} of a property.
	 * @param <T>
	 * A Java type of value of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	<T> T get(PropertyName name, PropertyType<T> type) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#BIG_DECIMAL}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @param type
	 * {@link PropertyType} of a property.
	 * @param <T>
	 * A Java type of value of a property.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	<T> Optional<T> get(PropertyName name, @Nullable T defaultValue, PropertyType<T> type);

	/**
	 * Returns value of a property of the type {@link PropertyType#BIG_DECIMAL}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	BigDecimal getBigDecimal(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#BIG_DECIMAL}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<BigDecimal> getBigDecimal(PropertyName name, @Nullable BigDecimal defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#BIG_INTEGER}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	BigInteger getBigInteger(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#BIG_INTEGER}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<BigInteger> getBigInteger(PropertyName name, @Nullable BigInteger defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#BOOLEAN}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	Boolean getBoolean(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#BOOLEAN}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<Boolean> getBoolean(PropertyName name, @Nullable Boolean defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#DOUBLE}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	Double getDouble(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#DOUBLE}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<Double> getDouble(PropertyName name, @Nullable Double defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#INTEGER}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	Integer getInteger(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#INTEGER}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<Integer> getInteger(PropertyName name, @Nullable Integer defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#LIST_OF_STRINGS}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property. The returned {@link List} is unmodifiable and MAY contain duplicate elements.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	List<String> getListOfStrings(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#LIST_OF_STRINGS}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link List} is unmodifiable unless this {@link List} is the {@code defaultValue},
	 * and MAY contain duplicate elements. The {@code defaultValue} is returned as is.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<List<String>> getListOfStrings(PropertyName name, @Nullable List<String> defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#LONG}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	Long getLong(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#LONG}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<Long> getLong(PropertyName name, @Nullable Long defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#SET_OF_STRINGS}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property. The returned {@link Set} is unmodifiable and is ordered.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	Set<String> getSetOfStrings(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#SET_OF_STRINGS}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Set} is unmodifiable unless this {@link Set} is the {@code defaultValue}, and is ordered.
	 * The {@code defaultValue} is returned as is.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<Set<String>> getSetOfStrings(PropertyName name, @Nullable Set<String> defaultValue);

	/**
	 * Returns value of a property of the type {@link PropertyType#STRING}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	String getString(PropertyName name) throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@link PropertyType#STRING}.
	 *
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	Optional<String> getString(PropertyName name, @Nullable String defaultValue);

	/**
	 * Returns value of a property of the type {@code T}.
	 *
	 * @param <T>
	 * Type of a property.
	 * @param name
	 * Identifier of a property.
	 * @param converter
	 * {@link StringableConverter} that knows how to restore object of type {@code T} from {@link String}
	 * value of the property.
	 * @return
	 * Value of a property.
	 * @throws NoSuchPropertyException
	 * If the property identified by the {@code name} is undefined.
	 */
	<T extends Stringable> T getStringable(PropertyName name, StringableConverter<T> converter)
			throws NoSuchPropertyException;

	/**
	 * Returns value of a property of the type {@code T}.
	 *
	 * @param <T>
	 * Type of a property.
	 * @param name
	 * Identifier of a property.
	 * @param defaultValue
	 * Value to use if the property identified by the {@code name} is undefined.
	 * @param converter
	 * {@link StringableConverter} that knows how to restore object of type {@code T} from {@link String}
	 * value of the property.
	 * @return
	 * Value of a property or {@code defaultValue} if the property is undefined.
	 * The returned {@link Optional} isn't {@link Optional#isPresent() present} if and only if the property is undefined and
	 * {@code defaultValue} is {@code null}.
	 */
	<T extends Stringable> Optional<T> getStringable(PropertyName name, @Nullable T defaultValue,
			StringableConverter<T> converter);
}
