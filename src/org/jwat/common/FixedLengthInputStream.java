/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;

public final class FixedLengthInputStream extends FilterInputStream {
	protected long remaining;

	public FixedLengthInputStream(final InputStream in, final long length) {
		super(in);
		this.remaining = length;
	}

	@Override
	public void close() throws IOException {
		long skippedLast = 0L;
		if (this.remaining > 0L) {
			for (skippedLast = this.skip(this.remaining); this.remaining > 0L
					&& skippedLast > 0L; skippedLast = this.skip(this.remaining)) {
			}
		}
	}

	@Override
	public int available() throws IOException {
		return (this.remaining > 2147483647L) ? Integer.MAX_VALUE : ((int) this.remaining);
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
	public int read() throws IOException {
		int b = -1;
		if (this.remaining > 0L) {
			b = this.in.read();
			if (b != -1) {
				--this.remaining;
			}
		}
		return b;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		int bytesRead = -1;
		if (this.remaining > 0L) {
			bytesRead = this.in.read(b, off, (int) Math.min(len, this.remaining));
			if (bytesRead > 0) {
				this.remaining -= bytesRead;
			}
		}
		return bytesRead;
	}

	@Override
	public long skip(final long n) throws IOException {
		long bytesSkipped = 0L;
		if (this.remaining > 0L) {
			bytesSkipped = this.in.skip(Math.min(n, this.remaining));
			this.remaining -= bytesSkipped;
		}
		return bytesSkipped;
	}
}