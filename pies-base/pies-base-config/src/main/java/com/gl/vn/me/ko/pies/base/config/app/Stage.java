package com.gl.vn.me.ko.pies.base.config.app;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.feijoa.Stringable;
import com.gl.vn.me.ko.pies.base.feijoa.StringableConvertationException;
import com.gl.vn.me.ko.pies.base.feijoa.StringableConverter;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a stage the Application is running in. Some parts of the Application MAY decide to change internal
 * behavior depending on the {@link Stage}, but functionality MUST NOT be affected.
 * Note that logging SHOULD NOT be affected by {@link Stage}.
 *
 * @see ApplicationConfig
 */
@Immutable
public enum Stage implements Stringable {
	/**
	 * At this stage startup time is more important than runtime performance.
	 * <p>
	 * Name of this {@link Stage} is {@code "development"}.
	 */
	DEVELOPMENT("development"),
	/**
	 * At this stage performance and delays is more important than startup time.
	 * <p>
	 * Name of this {@link Stage} is {@code "production"}.
	 */
	PRODUCTION("production");

	private static final class StageConverter implements StringableConverter<Stage> {
		private static final StageConverter INSTANCE = new StageConverter();

		private StageConverter() {
		}

		@Override
		public final Stage valueOf(final String name) throws StringableConvertationException {
			checkNotNull(name, Message.ARGUMENT_NULL_SINGLE, "name");
			final Stage result;
			switch (name) {
				case "development": {
					result = DEVELOPMENT;
					break;
				}
				case "production": {
					result = PRODUCTION;
					break;
				}
				default:
					throw new StringableConvertationException(Message.format(Message.ARGUMENT_ILLEGAL_SINGLE, name,
							"name", "Value must be one of \"development\", \"production\""));
			}
			return result;
		}
	}

	/**
	 * Returns {@link StringableConverter} that can restore {@link Stage} from a {@link String} returned from
	 * {@link Stage#toString()}.
	 *
	 * @return
	 * {@link StringableConverter} for {@link Stage}.
	 */
	public static final StringableConverter<Stage> converter() {
		return StageConverter.INSTANCE;
	}
	private final String name;

	private Stage(final String name) {
		this.name = name;
	}

	/**
	 * Returns name of the {@link Stage}.
	 * The returned value can be used as argument for the method {@link StringableConverter#valueOf(String)} of the
	 * {@link StringableConverter} returned by {@link #converter()}.
	 *
	 * @return
	 * Name of the {@link Stage}.
	 * @see #converter()
	 */
	@Override
	public final String toString() {
		return name;
	}
}
