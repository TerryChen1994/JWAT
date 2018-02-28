/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;

public class ByteCountingInputStream extends FilterInputStream {
	public static final int READLINE_INITIAL_SIZE = 128;
	protected long consumed;
	protected long counter;

	public ByteCountingInputStream(final InputStream in) {
		super(in);
		this.consumed = 0L;
		this.counter = 0L;
	}

	public long getConsumed() {
		return this.consumed;
	}

	public void setCounter(final long bytes) {
		this.counter = bytes;
	}

	public long getCounter() {
		return this.counter;
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
		final int b = this.in.read();
		if (b != -1) {
			++this.consumed;
			++this.counter;
		}
		return b;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final int bytesRead = this.in.read(b, off, len);
		if (bytesRead > 0) {
			this.consumed += bytesRead;
			this.counter += bytesRead;
		}
		return bytesRead;
	}

	@Override
	public long skip(final long n) throws IOException {
		final long bytesSkipped = this.in.skip(n);
		this.consumed += bytesSkipped;
		this.counter += bytesSkipped;
		return bytesSkipped;
	}
}