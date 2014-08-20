package com.gl.vn.me.ko.pies.base.main;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides ability to write to the Lock File.
 */
@ThreadSafe
public abstract class LockFileWriter {
	/**
	 * Constructor of {@link LockFileWriter}.
	 */
	protected LockFileWriter() {
	}

	/**
	 * Writes the supplied {@code data} to the Lock File.
	 *
	 * @param data
	 * Data to write.
	 */
	public abstract void write(String data);

	/**
	 * Closes the {@link LockFileWriter} so that {@link #write(String)} can be performed any more.
	 */
	abstract void close();
}
