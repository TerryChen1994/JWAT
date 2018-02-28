/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.security.MessageDigest;
import java.io.InputStream;
import java.security.DigestInputStream;

public class DigestInputStreamNoSkip extends DigestInputStream {
	public static final int SKIP_READ_BUFFER_SIZE = 8192;
	protected byte[] skip_read_buffer;

	public DigestInputStreamNoSkip(final InputStream stream, final MessageDigest digest) {
		super(stream, digest);
		this.skip_read_buffer = new byte[8192];
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(final int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
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