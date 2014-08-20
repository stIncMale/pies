package com.gl.vn.me.ko.pies.base.feijoa;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * This class provides various utility methods related to {@link Throwable}s.
 */
public final class ThrowableUtil {
	/**
	 * Extracts a {@link Throwable} of type {@code extract} from {@code t}
	 * and its {@linkplain Throwable#getCause() cause} chain.
	 * This method returns empty {@link Optional} if {@code t} isn't of type
	 * {@code extract} and there is no {@code extract} among its causes.
	 * If there are multiple causes of type {@link extract} in the cause chain
	 * then the closest one to {@code t} in the cause chain is returned.
	 *
	 * @param <T>
	 * Type of a {@link Throwable} to extract.
	 * @param t
	 * {@link Throwable} from which to extract a {@link Throwable} of type {@code extract}.
	 * @param extract
	 * An instance of {@link Class} that represents type of a {@link Throwable} to extract.
	 *
	 * @return
	 * {@link Optional} with extracted {@link Throwable} of type {@code extract} or {@code null}.
	 */
	public static final <T> Optional<T> extract(Throwable t, final Class<T> extract) {
		checkNotNull(t, Message.ARGUMENT_NULL, "first", "t");
		checkNotNull(extract, Message.ARGUMENT_NULL, "second", "extract");
		@Nullable
		T result = null;
		do {
			if (extract.isInstance(t)) {
				@SuppressWarnings("unchecked")
				final T extracted = (T)t;
				result = extracted;
				break;
			} else {
				final Throwable cause = t.getCause();
				t = cause;
			}
		} while (t != null);
		return Optional.ofNullable(result);
	}
}
