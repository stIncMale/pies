package com.gl.vn.me.ko.pies.base.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.feijoa.Stringable;
import com.gl.vn.me.ko.pies.base.feijoa.StringableConvertationException;
import com.gl.vn.me.ko.pies.base.feijoa.StringableConverter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.junit.Test;

public final class TestXmlPropsConfig {
	private enum StringableClass implements Stringable {
		A("a"),
		B("b");

		private static final class StringableClassConverter implements StringableConverter<StringableClass> {
			private static final StringableClassConverter INSTANCE = new StringableClassConverter();

			private StringableClassConverter() {
			}

			@Override
			public final StringableClass valueOf(final String value) throws StringableConvertationException {
				final StringableClass result;
				switch (value) {
					case "a": {
						result = StringableClass.A;
						break;
					}
					case "b": {
						result = StringableClass.B;
						break;
					}
					default:
						throw new StringableConvertationException(Message.format(
								Message.ARGUMENT_ILLEGAL_SINGLE, value, "value", "Value must be one of \"a\", \"b\""));
				}
				return result;
			}
		}

		private static final StringableClassConverter converter() {
			return StringableClassConverter.INSTANCE;
		}
		private final String value;

		private StringableClass(final String value) {
			this.value = value;
		}

		@Override
		public final String toString() {
			return value;
		}
	}

	private enum TestPropertyName implements PropertyName {
		PROPERTY_NONEXISTENT("foo.bar.testNonexistent"),
		PROPERTY_STRING("foo.bar.testString"),
		PROPERTY_STRINGABLE("foo.bar.testStringable"),
		PROPERTY_INTEGER("foo.bar.testInteger"),
		PROPERTY_DOUBLE("foo.bar.testDouble"),
		PROPERTY_BOOLEAN_TRUE("foo.bar.testBooleanTrue"),
		PROPERTY_BOOLEAN_FALSE("foo.bar.testBooleanFalse"),
		PROPERTY_BIG_INTEGER("foo.bar.testBigInteger"),
		PROPERTY_BIG_DECIMAL("foo.bar.testBigDecimal"),
		PROPERTY_LONG("foo.bar.testLong"),
		PROPERTY_MULTIPLE_OF_STRINGS("foo.bar.testMultipleOfStrings"),
		PROPERTY_VARIABLE_INTERPOLATION("foo.bar.testVariableInterpolation");
		private final String name;

		private TestPropertyName(final String name) {
			this.name = name;
		}

		@Override
		public final String toString() {
			return name;
		}
	}
	private static final String MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG
			= "Assert that correct value from config was returned";
	private static final String MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE = "Assert that correct default value was returned";
	private static final String MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS
			= "Assert that multiple invocations of get...(...) return the same object";
	private static final String CONFIG_FILENAME_VALID = "testXmlConfigValid.xml";
	private static final String CONFIG_FILENAME_INVALID1 = "testXmlConfigInvalid1.xml";
	private static final String CONFIG_FILENAME_INVALID2 = "testXmlConfigInvalid2.xml";
	private static final String PROPERTY_STRING_VALUE = "value 𤧰 \u0064" + (new String(new int[] {0x494F8}, 0, 1));
	private static final StringableClass PROPERTY_STRINGABLE_VALUE = StringableClass.A;
	private static final Integer PROPERTY_INTEGER_VALUE = Integer.valueOf(-1);
	private static final Double PROPERTY_DOUBLE_VALUE = Double.valueOf("1.6E10");
	private static final Boolean PROPERTY_BOOLEAN_TRUE_VALUE = Boolean.TRUE;
	private static final Boolean PROPERTY_BOOLEAN_FALSE_VALUE = Boolean.FALSE;
	private static final BigInteger PROPERTY_BIG_INTEGER_VALUE = new BigInteger("999999999999999999");
	private static final BigDecimal PROPERTY_BIG_DECIMAL_VALUE = new BigDecimal("-0.5E-9999");
	private static final Long PROPERTY_LONG_VALUE = Long.valueOf(9000000000L);
	private static final BigDecimal PROPERTY_VARIABLE_INTERPOLATION_VALUE = PROPERTY_BIG_DECIMAL_VALUE;
	private static final List<String> PROPERTY_LIST_OF_STRINGS_VALUE;
	private static final Set<String> PROPERTY_SET_OF_STRINGS_VALUE;

	static {
		PROPERTY_LIST_OF_STRINGS_VALUE = new ArrayList<>();
		PROPERTY_LIST_OF_STRINGS_VALUE.add("value1");
		PROPERTY_LIST_OF_STRINGS_VALUE.add("value2");
		PROPERTY_LIST_OF_STRINGS_VALUE.add("value1");
		PROPERTY_LIST_OF_STRINGS_VALUE.add("value3");
		PROPERTY_LIST_OF_STRINGS_VALUE.add("valueWithEscaped;Semicolon");
		PROPERTY_SET_OF_STRINGS_VALUE = new LinkedHashSet<>(PROPERTY_LIST_OF_STRINGS_VALUE);
	}

	private static final PropsConfig createConfig(final String fileName, final boolean validate)
			throws URISyntaxException, ConfigCreationException {
		final URL configUrl = TestXmlPropsConfig.class.getResource(fileName);
		final Path path = Paths.get(configUrl.toURI());
		return new XmlPropsConfig(path, validate);
	}
	private final PropsConfig config;

	public TestXmlPropsConfig() throws URISyntaxException {
		config = createConfig(CONFIG_FILENAME_VALID, true);
	}

	@Test
	public final void get() {
		final Set<String> value = config.get(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS, PropertyType.SET_OF_STRINGS);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_SET_OF_STRINGS_VALUE, value);
	}

	@Test
	public final void getDefault() {
		@Nullable
		final Set<String> value
				= config.get(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS, null, PropertyType.SET_OF_STRINGS).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_SET_OF_STRINGS_VALUE, value);
	}

	@Test
	public final void getDefaultNonexistent() {
		final Set<String> defaultValue = new HashSet<>(1);
		defaultValue.add("default");
		@Nullable
		final Set<String> value
				= config.get(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue, PropertyType.SET_OF_STRINGS).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getMultipleInvocations() {
		final Set<String> value1 = config.get(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS, PropertyType.SET_OF_STRINGS);
		final Set<String> value2 = config.get(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS, PropertyType.SET_OF_STRINGS);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getNonexistent() {
		config.get(TestPropertyName.PROPERTY_NONEXISTENT, PropertyType.SET_OF_STRINGS);
	}

	@Test
	public final void getBigDecimal() {
		final BigDecimal value = config.getBigDecimal(TestPropertyName.PROPERTY_BIG_DECIMAL);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BIG_DECIMAL_VALUE, value);
	}

	@Test
	public final void getBigDecimalDefault() {
		@Nullable
		final BigDecimal value = config.getBigDecimal(TestPropertyName.PROPERTY_BIG_DECIMAL, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BIG_DECIMAL_VALUE, value);
	}

	@Test
	public final void getBigDecimalDefaultNonexistent() {
		final BigDecimal defaultValue = BigDecimal.TEN;
		@Nullable
		final BigDecimal value = config.getBigDecimal(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getBigDecimalMultipleInvocations() {
		final BigDecimal value1 = config.getBigDecimal(TestPropertyName.PROPERTY_BIG_DECIMAL);
		final BigDecimal value2 = config.getBigDecimal(TestPropertyName.PROPERTY_BIG_DECIMAL);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getBigDecimalNonexistent() {
		config.getBigDecimal(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getBigInteger() {
		final BigInteger value = config.getBigInteger(TestPropertyName.PROPERTY_BIG_INTEGER);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BIG_INTEGER_VALUE, value);
	}

	@Test
	public final void getBigIntegerDefault() {
		@Nullable
		final BigInteger value = config.getBigInteger(TestPropertyName.PROPERTY_BIG_INTEGER, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BIG_INTEGER_VALUE, value);
	}

	@Test
	public final void getBigIntegerDefaultNonexistent() {
		final BigInteger defaultValue = BigInteger.ONE;
		@Nullable
		final BigInteger value = config.getBigInteger(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getBigIntegerMultipleInvocations() {
		final BigInteger value1 = config.getBigInteger(TestPropertyName.PROPERTY_BIG_INTEGER);
		final BigInteger value2 = config.getBigInteger(TestPropertyName.PROPERTY_BIG_INTEGER);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getBigIntegerNonexistent() {
		config.getBigInteger(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getBoolean() {
		final Boolean valueTrue = config.getBoolean(TestPropertyName.PROPERTY_BOOLEAN_TRUE);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BOOLEAN_TRUE_VALUE, valueTrue);
		final Boolean valueFalse = config.getBoolean(TestPropertyName.PROPERTY_BOOLEAN_FALSE);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BOOLEAN_FALSE_VALUE, valueFalse);
	}

	@Test
	public final void getBooleanDefault() {
		@Nullable
		final Boolean value = config.getBoolean(TestPropertyName.PROPERTY_BOOLEAN_TRUE, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_BOOLEAN_TRUE_VALUE, value);
	}

	@Test
	public final void getBooleanDefaultNonexistent() {
		final Boolean defaultValue = Boolean.FALSE;
		@Nullable
		final Boolean value = config.getBoolean(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getBooleanMultipleInvocations() {
		final Boolean value1 = config.getBoolean(TestPropertyName.PROPERTY_BOOLEAN_TRUE);
		final Boolean value2 = config.getBoolean(TestPropertyName.PROPERTY_BOOLEAN_TRUE);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getBooleanNonexistent() {
		config.getString(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getDouble() {
		final Double value = config.getDouble(TestPropertyName.PROPERTY_DOUBLE);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_DOUBLE_VALUE, value);
	}

	@Test
	public final void getDoubleDefault() {
		@Nullable
		final Double value = config.getDouble(TestPropertyName.PROPERTY_DOUBLE, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_DOUBLE_VALUE, value);
	}

	@Test
	public final void getDoubleDefaultNonexistent() {
		final Double defaultValue = Double.valueOf(Double.MAX_VALUE);
		@Nullable
		final Double value = config.getDouble(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getDoubleMultipleInvocations() {
		final Double value1 = config.getDouble(TestPropertyName.PROPERTY_INTEGER);
		final Double value2 = config.getDouble(TestPropertyName.PROPERTY_INTEGER);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getDoubleNonexistent() {
		config.getDouble(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getInteger() {
		final Integer value = config.getInteger(TestPropertyName.PROPERTY_INTEGER);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_INTEGER_VALUE, value);
	}

	@Test
	public final void getIntegerDefault() {
		@Nullable
		final Integer value = config.getInteger(TestPropertyName.PROPERTY_INTEGER, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_INTEGER_VALUE, value);
	}

	@Test
	public final void getIntegerDefaultNonexistent() {
		final Integer defaultValue = Integer.valueOf(Integer.MAX_VALUE);
		@Nullable
		final Integer value = config.getInteger(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getIntegerMultipleInvocations() {
		final Integer value1 = config.getInteger(TestPropertyName.PROPERTY_INTEGER);
		final Integer value2 = config.getInteger(TestPropertyName.PROPERTY_INTEGER);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getIntegerNonexistent() {
		config.getInteger(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getInterpolatedValue() {
		final BigDecimal value = config.getBigDecimal(TestPropertyName.PROPERTY_VARIABLE_INTERPOLATION);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_VARIABLE_INTERPOLATION_VALUE, value);
	}

	@Test
	public final void getListOfStrings() {
		final List<String> value = config.getListOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_LIST_OF_STRINGS_VALUE, value);
	}

	@Test
	public final void getListOfStringsDefault() {
		@Nullable
		final List<String> value = config.getListOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_LIST_OF_STRINGS_VALUE, value);
	}

	@Test
	public final void getListOfStringsDefaultNonexistent() {
		final List<String> defaultValue = new ArrayList<>(1);
		defaultValue.add("default");
		@Nullable
		final List<String> value = config.getListOfStrings(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getListOfStringsMultipleInvocations() {
		final List<String> value1 = config.getListOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS);
		final List<String> value2 = config.getListOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getListOfStringsNonexistent() {
		config.getListOfStrings(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getLong() {
		final Long value = config.getLong(TestPropertyName.PROPERTY_LONG);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_LONG_VALUE, value);
	}

	@Test
	public final void getLongDefault() {
		@Nullable
		final Long value = config.getLong(TestPropertyName.PROPERTY_LONG, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_LONG_VALUE, value);
	}

	@Test
	public final void getLongDefaultNonexistent() {
		final Long defaultValue = Long.valueOf(Long.MIN_VALUE);
		@Nullable
		final Long value = config.getLong(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getLongMultipleInvocations() {
		final Long value1 = config.getLong(TestPropertyName.PROPERTY_LONG);
		final Long value2 = config.getLong(TestPropertyName.PROPERTY_LONG);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getLongNonexistent() {
		config.getLong(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getSetOfStrings() {
		final Set<String> value = config.getSetOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_SET_OF_STRINGS_VALUE, value);
	}

	@Test
	public final void getSetOfStringsDefault() {
		@Nullable
		final Set<String> value = config.getSetOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_SET_OF_STRINGS_VALUE, value);
	}

	@Test
	public final void getSetOfStringsDefaultNonexistent() {
		final Set<String> defaultValue = new HashSet<>(1);
		defaultValue.add("default");
		@Nullable
		final Set<String> value = config.getSetOfStrings(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getSetOfStringsMultipleInvocations() {
		final Set<String> value1 = config.getSetOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS);
		final Set<String> value2 = config.getSetOfStrings(TestPropertyName.PROPERTY_MULTIPLE_OF_STRINGS);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getSetOfStringsNonexistent() {
		config.getSetOfStrings(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void getString() {
		final String value = config.getString(TestPropertyName.PROPERTY_STRING);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_STRING_VALUE, value);
	}

	@Test
	public final void getStringable() {
		final StringableClass value
				= config.getStringable(TestPropertyName.PROPERTY_STRINGABLE, StringableClass.converter());
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_STRINGABLE_VALUE, value);
	}

	@Test
	public final void getStringableDefault() {
		@Nullable
		final StringableClass value
				= config.getStringable(TestPropertyName.PROPERTY_STRINGABLE, null, StringableClass.converter()).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_STRINGABLE_VALUE, value);
	}

	@Test
	public final void getStringableDefaultNonexistent() {
		final StringableClass defaultValue = StringableClass.B;
		@Nullable
		final Stringable value
				= config.getStringable(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue, StringableClass.converter()).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getStringableMultipleInvocations() {
		final Stringable value1
				= config.getStringable(TestPropertyName.PROPERTY_STRINGABLE, StringableClass.converter());
		final Stringable value2
				= config.getStringable(TestPropertyName.PROPERTY_STRINGABLE, StringableClass.converter());
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getStringableNonexistent() {
		config.getStringable(TestPropertyName.PROPERTY_NONEXISTENT, StringableClass.converter());
	}

	@Test
	public final void getStringDefault() {
		@Nullable
		final String value = config.getString(TestPropertyName.PROPERTY_STRING, null).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_VALUE_FROM_CONFIG, PROPERTY_STRING_VALUE, value);
	}

	@Test
	public final void getStringDefaultNonexistent() {
		final String defaultValue = "default";
		@Nullable
		final String value = config.getString(TestPropertyName.PROPERTY_NONEXISTENT, defaultValue).orElse(null);
		assertEquals(MESSAGE_ASSERT_CORRECT_DEFAULT_VALUE, defaultValue, value);
	}

	@Test
	public final void getStringMultipleInvocations() {
		final String value1 = config.getString(TestPropertyName.PROPERTY_STRING);
		final String value2 = config.getString(TestPropertyName.PROPERTY_STRING);
		assertSame(MESSAGE_ASSERT_SAME_VALUE_FROM_MULTIPLE_INVOCATIONS, value1, value2);
	}

	@Test(expected = NoSuchPropertyException.class)
	public final void getStringNonexistent() {
		config.getString(TestPropertyName.PROPERTY_NONEXISTENT);
	}

	@Test
	public final void loadInvalidWithoutValidation() throws URISyntaxException {
		createConfig(CONFIG_FILENAME_INVALID1, false);
	}

	@Test(expected = ConfigCreationException.class)
	public final void validateInvalid1() throws URISyntaxException {
		createConfig(CONFIG_FILENAME_INVALID1, true);
	}

	@Test(expected = ConfigCreationException.class)
	public final void validateInvalid2() throws URISyntaxException {
		createConfig(CONFIG_FILENAME_INVALID2, true);
	}
}
