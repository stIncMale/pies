package com.gl.vn.me.ko.pies.base.config;

import static com.gl.vn.me.ko.pies.base.config.PropertyType.BIG_DECIMAL;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.BIG_INTEGER;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.BOOLEAN;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.DOUBLE;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.INTEGER;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.LIST_OF_STRINGS;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.LONG;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.SET_OF_STRINGS;
import static com.gl.vn.me.ko.pies.base.config.PropertyType.STRING;
import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Constant;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.feijoa.Stringable;
import com.gl.vn.me.ko.pies.base.feijoa.StringableConverter;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationError;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Represents configuration that is specified via XML document.
 * This implementation uses {@link XMLConfiguration} in order to read cachedProps from XML.
 * See
 * <a href="http://commons.apache.org/proper/commons-configuration/">Apache Commons Configuration</a>
 * for details; e.g. <a href=
 * "http://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html#Variable_Interpolation"
 * >Variable Interpolation (aka Property Substitution)</a> is a very useful functionality.
 * However note also that Apache Commons Configuration seem to violate its specification sometimes,
 * e.g. you can't preserve spaces by using {@code xml:space="preserve"} as specified in {@link XMLConfiguration}.
 * <p>
 * Semicolon {@code ';'} (Unicode code point {@code U+003B}) is used as delimiter in multiple properties (see
 * {@link XMLConfiguration#setListDelimiter(char)}). One can escape the semicolon character in the multiple value by
 * using backslash {@code '\'} (Unicode code point {@code U+005C}).
 */
@ThreadSafe
public final class XmlPropsConfig implements PropsConfig {
	@SuppressFBWarnings(value = {"DM_BOOLEAN_CTOR", "DM_FP_NUMBER_CTOR", "DM_NUMBER_CTOR", "DM_STRING_VOID_CTOR"},
			justification = "See comment on Props.EMPTY_VALUES field")
	private static final class Props {
		/*
		 * Values in EMPTY_VALUES MUST be new objects, i.e. instead of Boolean.FALSE one MUST use new Boolean(false).
		 */
		private static final Map<PropertyType<?>, Object> EMPTY_VALUES = ImmutableMap.<PropertyType<?>, Object>builder()
				.put(BIG_DECIMAL, new BigDecimal(0))
				.put(BIG_INTEGER, new BigInteger("0"))
				.put(BOOLEAN, new Boolean(false))
				.put(DOUBLE, new Double(0))
				.put(INTEGER, new Integer(0))
				.put(LONG, new Long(0))
				.put(STRING, new String())
				.put(LIST_OF_STRINGS, new ArrayList<>(0))
				.put(SET_OF_STRINGS, new HashSet<>(0))
				.build();

		private static final <T> T empty(final PropertyType<T> type) {
			@SuppressWarnings("unchecked")
			final T result = (T)EMPTY_VALUES.get(type);
			return result;
		}
		private final Map<PropertyType<?>, ConcurrentMap<PropertyName, Object>> byType;

		private Props() {
			byType = new HashMap<>(9);
			for (final PropertyType<?> type : PropertyType.types()) {
				byType.put(type, new ConcurrentHashMap<>());
			}
		}

		private final <T> ConcurrentMap<PropertyName, T> byType(final PropertyType<T> type) {
			@SuppressWarnings("unchecked")
			final ConcurrentMap<PropertyName, T> result = (ConcurrentMap<PropertyName, T>)byType.get(type);
			return result;
		}

		/**
		 * Returns a description of the {@link Props}.
		 *
		 * @return
		 * A description of the {@link Props}.
		 */
		@Override
		public final String toString() {
			final StringBuilder sb = new StringBuilder(this.getClass().getName())
					.append("(byType=").append(byType).append(')');
			final String result = sb.toString();
			return result;
		}
	}
	/**
	 * This {@code char} is used as delimiter in multiple properties
	 * (see {@link XMLConfiguration#setListDelimiter(char)}).
	 */
	private static final char MULTIPLE_PROPERTY_DELIMETER = ';';

	/**
	 * Returns object that is used by {@link XmlPropsConfig} to read configuration.
	 *
	 * @param path
	 * Location of the XML file that contains configuration. UTF-8 encoding MUST be used in the file.
	 * @param validate
	 * Specifies whether XSD schema validation should be performed when loading XML documents.
	 * The XSD schema MUST be specified in the XML document if this argument is {@code true}.
	 * @return Object that is used by {@link XmlPropsConfig} to read configuration.
	 * @throws ConfigurationException
	 * If an error occurs during the config load operation.
	 * @throws SAXException
	 * If {@code validate} is {@code true} and XML is not valid or doesn't specify the schema it expects to
	 * be validated against.
	 */
	private static final XMLConfiguration createConfigReader(final Path path, final boolean validate)
			throws ConfigurationException, SAXException {
		/*
		 * Seems like Apache Configuration can only validate XML via DTD, but not XSD.
		 * So we need to perform validation by ourselves.
		 */
		if (validate) {
			validate(path);
		}
		final XMLConfiguration result = new XMLConfiguration();
		result.setValidating(false);
		result.setEncoding(Constant.CHARSET.name());
		result.setThrowExceptionOnMissing(false);
		result.setListDelimiter(MULTIPLE_PROPERTY_DELIMETER);
		try {
			result.load(path.toUri().toURL());
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * Checks if the specified file contains valid XML or not.
	 * The XSD schema MUST be specified in the XML document.
	 *
	 * @param path
	 * A Path to file that contains XML that need to be validated.
	 * @throws SAXException
	 * If XML is not valid or doesn't specify the schema it expects to be validated against.
	 */
	private static final void validate(final Path path) throws SAXException {
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final Validator validator;
		try {
			final Schema schema = schemaFactory.newSchema();
			validator = schema.newValidator();
		} catch (final SAXException e) {
			throw new RuntimeException(e);
		}
		final Source source = new StreamSource(path.toFile());
		try {
			validator.validate(source);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	private final XMLConfiguration configReader;
	private final Props cachedProps;

	/**
	 * Constructs a new instance of {@link XmlPropsConfig}.
	 *
	 * @param config
	 * Location of the configuration file. UTF-8 encoding MUST be used in the file.
	 * @param validate
	 * Specifies whether XSD schema validation need to be performed when loading XML document.
	 * @throws ConfigCreationException
	 * If creation of configuration is failed.
	 */
	public XmlPropsConfig(final Path config, final boolean validate) throws ConfigCreationException {
		checkNotNull(config, Message.ARGUMENT_NULL_SINGLE, "config");
		try {
			configReader = createConfigReader(config, validate);
			cachedProps = new Props();
		} catch (final Exception e) {
			throw new ConfigCreationException(e);
		}
	}

	/**
	 * Constructs a new instance of {@link XmlPropsConfig}.
	 * Acts like {@link #XmlPropsConfig(Path, boolean)} by creating a {@link Path} as following:
	 * <pre>{@code
	 * 	FileSystems.getDefault().getPath(config)
	 * }</pre>
	 *
	 * @param config
	 * Location of the configuration file. UTF-8 encoding MUST be used in the file.
	 * @param validate
	 * Specifies whether XSD schema validation need to be performed when loading XML document.
	 * @throws ConfigCreationException
	 * If creation of configuration is failed.
	 */
	public XmlPropsConfig(final String config, final boolean validate) {
		checkNotNull(config, Message.ARGUMENT_NULL_SINGLE, "config");
		try {
			final Path path = FileSystems.getDefault().getPath(config);
			configReader = createConfigReader(path, validate);
			cachedProps = new Props();
		} catch (final RuntimeException | ConfigurationException | SAXException e) {
			throw new ConfigCreationException(e);
		}
	}

	private final <T> void cachePropertyIfNeeded(final PropertyName name, final PropertyType<T> type) {
		final ConcurrentMap<PropertyName, T> cachedProperties = cachedProps.byType(type);
		@Nullable
		final T readValue = readValue(name, type, null);
		if (readValue == null) {
			cachedProperties.putIfAbsent(name, Props.empty(type));
		} else {
			cachedProperties.putIfAbsent(name, readValue);
		}
	}

	@Override
	public final <T> T get(final PropertyName name, final PropertyType<T> type) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL, "first", "name");
		checkNotNull(type, Message.ARGUMENT_NULL, "second", "type");
		return getValue(name, type);
	}

	@Override
	public final <T> Optional<T> get(final PropertyName name, final T defaultValue, final PropertyType<T> type) {
		checkNotNull(name, Message.ARGUMENT_NULL, "first", "name");
		checkNotNull(type, Message.ARGUMENT_NULL, "third", "type");
		return Optional.ofNullable(getValue(name, type, defaultValue));
	}

	@Override
	public final BigDecimal getBigDecimal(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.BIG_DECIMAL);
	}

	@Override
	public final Optional<BigDecimal> getBigDecimal(final PropertyName name, @Nullable final BigDecimal defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.BIG_DECIMAL, defaultValue));
	}

	@Override
	public final BigInteger getBigInteger(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.BIG_INTEGER);
	}

	@Override
	public final Optional<BigInteger> getBigInteger(final PropertyName name, @Nullable final BigInteger defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.BIG_INTEGER, defaultValue));
	}

	@Override
	public final Boolean getBoolean(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.BOOLEAN);
	}

	@Override
	public final Optional<Boolean> getBoolean(final PropertyName name, @Nullable final Boolean defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.BOOLEAN, defaultValue));
	}

	@Override
	public final Double getDouble(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.DOUBLE);
	}

	@Override
	public final Optional<Double> getDouble(final PropertyName name, @Nullable final Double defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.DOUBLE, defaultValue));
	}

	@Override
	public final Integer getInteger(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.INTEGER);
	}

	@Override
	public final Optional<Integer> getInteger(final PropertyName name, @Nullable final Integer defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.INTEGER, defaultValue));
	}

	@Override
	public final List<String> getListOfStrings(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		@SuppressWarnings("unchecked")
		final List<String> result = getValue(name, PropertyType.LIST_OF_STRINGS);
		return result;
	}

	@Override
	public final Optional<List<String>> getListOfStrings(final PropertyName name, @Nullable final List<String> defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		@SuppressWarnings("unchecked")
		final List<String> result = getValue(name, PropertyType.LIST_OF_STRINGS, defaultValue);
		return Optional.ofNullable(result);
	}

	@Override
	public final Long getLong(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.LONG);
	}

	@Override
	public final Optional<Long> getLong(final PropertyName name, @Nullable final Long defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.LONG, defaultValue));
	}

	@Override
	public final Set<String> getSetOfStrings(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		@SuppressWarnings("unchecked")
		final Set<String> result = getValue(name, PropertyType.SET_OF_STRINGS);
		return result;
	}

	@Override
	public final Optional<Set<String>> getSetOfStrings(final PropertyName name, @Nullable final Set<String> defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		@SuppressWarnings("unchecked")
		final Set<String> result = getValue(name, PropertyType.SET_OF_STRINGS, defaultValue);
		return Optional.ofNullable(result);
	}

	@Override
	public final String getString(final PropertyName name) throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return getValue(name, PropertyType.STRING);
	}

	@Override
	public final Optional<String> getString(final PropertyName name, @Nullable final String defaultValue) {
		checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
		return Optional.ofNullable(getValue(name, PropertyType.STRING, defaultValue));
	}

	@Override
	public <T extends Stringable> T getStringable(final PropertyName name, final StringableConverter<T> converter)
			throws NoSuchPropertyException {
		checkNotNull(name, Message.ARGUMENT_NULL, "first", "name");
		checkNotNull(converter, Message.ARGUMENT_NULL, "second", "converter");
		return converter.valueOf(getValue(name, PropertyType.STRING));
	}

	@Override
	public <T extends Stringable> Optional<T> getStringable(final PropertyName name, @Nullable final T defaultValue,
			final StringableConverter<T> converter) {
		checkNotNull(name, Message.ARGUMENT_NULL, "first", "name");
		checkNotNull(converter, Message.ARGUMENT_NULL, "third", "converter");
		@Nullable
		final String stringValue = getValue(name, PropertyType.STRING, null);
		return Optional.ofNullable(stringValue == null ? defaultValue : converter.valueOf(stringValue));
	}

	private final <T> T getValue(final PropertyName name, final PropertyType<T> type) throws NoSuchPropertyException {
		@Nullable
		final T result = getValue(name, type, null);// returns null if and only only if the property is undefined
		if (result == null) {
			throw new NoSuchPropertyException(Message.format("Configuration doesn't contain property %s", name));
		}
		return result;
	}

	@Nullable
	private final <T> T getValue(final PropertyName name, final PropertyType<T> type, @Nullable final T defaultValue) {
		final ConcurrentMap<PropertyName, T> cachedProperties = cachedProps.byType(type);
		@Nullable
		T cachedValue = cachedProperties.get(name);
		if (cachedValue == null) {// value isn't cached
			cachePropertyIfNeeded(name, type);
			cachedValue = cachedProperties.get(name);// read cached value
		}
		return (cachedValue == Props.empty(type)) ? defaultValue : cachedValue;
	}

	@Nullable
	private final List<String> readMultiple(final PropertyName name) {
		@Nullable
		final List<String> result;
		final List<Object> readMultipleValues = configReader.getList(name.toString(), null);
		if (readMultipleValues != null) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			final List<String> multipleValues = (List)readMultipleValues;
			final ImmutableList.Builder<String> listBuilder = ImmutableList.<String>builder();
			for (@Nullable final String value : multipleValues) {
				if ((value != null) && StringUtils.isNotBlank(value)) {
					final String trimmedValue = value.trim();
					listBuilder.add(trimmedValue);
				}
			}
			result = listBuilder.build();
		} else {
			result = null;
		}
		return result;
	}

	@Nullable
	private final List<String> readListOfStrings(final PropertyName name, @Nullable final List<String> defaultValue) {
		@Nullable
		final List<String> readMultiple = readMultiple(name);
		@Nullable
		final List<String> result = readMultiple == null ? defaultValue : readMultiple;
		return result;
	}

	@Nullable
	private final Set<String> readSetOfStrings(final PropertyName name, @Nullable final Set<String> defaultValue) {
		@Nullable
		final List<String> readMultiple = readMultiple(name);
		@Nullable
		final Set<String> result = readMultiple == null ? defaultValue : ImmutableSet.copyOf(readMultiple);
		return result;
	}

	@Nullable
	private final <T> T readValue(final PropertyName name, final PropertyType<T> type, @Nullable final Object defaultValue) {
		@Nullable
		final T result;
		if (BIG_DECIMAL.equals(type)) {
			result = type.cast(configReader.getBigDecimal(name.toString(), BIG_DECIMAL.cast(defaultValue)));
		} else if (BIG_INTEGER.equals(type)) {
			result = type.cast(configReader.getBigInteger(name.toString(), BIG_INTEGER.cast(defaultValue)));
		} else if (BOOLEAN.equals(type)) {
			result = type.cast(configReader.getBoolean(name.toString(), BOOLEAN.cast(defaultValue)));
		} else if (DOUBLE.equals(type)) {
			result = type.cast(configReader.getDouble(name.toString(), DOUBLE.cast(defaultValue)));
		} else if (INTEGER.equals(type)) {
			result = type.cast(configReader.getInteger(name.toString(), INTEGER.cast(defaultValue)));
		} else if (LONG.equals(type)) {
			result = type.cast(configReader.getLong(name.toString(), LONG.cast(defaultValue)));
		} else if (STRING.equals(type)) {
			result = type.cast(configReader.getString(name.toString(), STRING.cast(defaultValue)));
		} else if (LIST_OF_STRINGS.equals(type)) {
			result = type.cast(readListOfStrings(name, LIST_OF_STRINGS.cast(defaultValue)));
		} else if (SET_OF_STRINGS.equals(type)) {
			result = type.cast(readSetOfStrings(name, SET_OF_STRINGS.cast(defaultValue)));
		} else {
			throw new ApplicationError(Message.CAN_NEVER_HAPPEN);
		}
		return result;
	}

	/**
	 * Returns a description of the {@link XmlPropsConfig}.
	 *
	 * @return
	 * A description of the {@link XmlPropsConfig}.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName())
				.append("(cachedProps=").append(cachedProps).append(')');
		final String result = sb.toString();
		return result;
	}
}
