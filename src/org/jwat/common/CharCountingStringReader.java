/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.StringReader;

public class CharCountingStringReader extends StringReader {
	protected long consumed;
	protected long counter;

	public CharCountingStringReader(final String str) {
		super(str);
		this.consumed = 0L;
		this.counter = 0L;
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
	public int read() throws IOException {
		final int c = super.read();
		if (c != -1) {
			++this.consumed;
			++this.counter;
		}
		return c;
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		final int bytesRead = super.read(cbuf, off, len);
		if (bytesRead > 0) {
			this.consumed += bytesRead;
			this.counter += bytesRead;
		}
		return bytesRead;
	}

	@Override
	public long skip(final long n) throws IOException {
		final long bytesSkipped = super.skip(n);
		this.consumed += bytesSkipped;
		this.counter += bytesSkipped;
		return bytesSkipped;
	}

	public String readLine() throws IOException {
		final StringBuilder buf = new StringBuilder();
		while (true) {
			final int c = this.read();
			if (c == -1) {
				return null;
			}
			if (c == 10) {
				return buf.toString();
			}
			if (c == 13) {
				continue;
			}
			buf.append((char) c);
		}
	}
}