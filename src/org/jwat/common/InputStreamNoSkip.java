/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;

public class InputStreamNoSkip extends FilterInputStream {
	public static final int SKIP_READ_BUFFER_SIZE = 8192;
	protected byte[] skip_read_buffer;

	public InputStreamNoSkip(final InputStream in) {
		super(in);
		this.skip_read_buffer = new byte[8192];
	}

	@Override
	public long skip(final long n) throws IOException {
		long remaining = n;
		long skipped = 0L;
		for (long readLast = 0L; remaining > 0L
				&& readLast != -1L; remaining -= readLast, skipped += readLast, readLast = this
						.read(this.skip_read_buffer, 0, (int) Math.min(remaining, 8192L))) {
		}
		return skipped;
	}
}