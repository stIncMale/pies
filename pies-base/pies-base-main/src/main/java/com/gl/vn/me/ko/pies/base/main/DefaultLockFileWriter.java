package com.gl.vn.me.ko.pies.base.main;

import static com.google.common.base.Preconditions.checkNotNull;
import com.gl.vn.me.ko.pies.base.constant.Message;
import com.gl.vn.me.ko.pies.base.throwable.ApplicationException;
import java.io.IOException;
import java.io.Writer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A wrapper for {@link Writer} opened for the Lock File.
 */
@ThreadSafe
final class DefaultLockFileWriter extends LockFileWriter {
	private final Writer writer;
	private final Object mutex;

	/**
	 * Constructs a new instance of {@link DefaultLockFileWriter}.
	 *
	 * @param lockFileWriter
	 * A {@link Writer} to wrap up. This {@link Writer} MUST only be used via the constructed {@link LockFileWriter}.
	 */
	DefaultLockFileWriter(final Writer lockFileWriter) {
		checkNotNull(lockFileWriter, Message.ARGUMENT_NULL_SINGLE, "lockFileWriter");
		writer = lockFileWriter;
		mutex = new Object();
	}

	@Override
	public final void write(final String data) {
		synchronized (mutex) {
			try {
				writer.write(data, 0, data.length());
				writer.flush();
			} catch (final IOException e) {
				throw new ApplicationException(e);
			}
		}
	}

	@Override
	final void close() {
		synchronized (mutex) {
			try {
				writer.close();
			} catch (final IOException e) {
				throw new ApplicationException(e);
			}
		}
	}
}
