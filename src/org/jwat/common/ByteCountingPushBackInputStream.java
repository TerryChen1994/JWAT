/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class ByteCountingPushBackInputStream extends PushbackInputStream {
	public static final int READLINE_INITIAL_SIZE = 128;
	protected int pushback_size;
	protected long consumed = 0L;

	protected long counter = 0L;

	public ByteCountingPushBackInputStream(InputStream in, int size) {
		super(in, size);
		this.pushback_size = size;
	}

	public int getPushbackSize() {
		return this.pushback_size;
	}

	public long getConsumed() {
		return this.consumed;
	}

	public void setCounter(long bytes) {
		this.counter = bytes;
	}

	public long getCounter() {
		return this.counter;
	}

	public boolean markSupported() {
		return false;
	}

	public synchronized void mark(int readlimit) {
	}

	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	public int read() throws IOException {
		int b = super.read();
		if (b != -1) {
			this.consumed += 1L;
			this.counter += 1L;
		}
		return b;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = super.read(b, off, len);
		if (bytesRead > 0) {
			this.consumed += bytesRead;
			this.counter += bytesRead;
		}
		return bytesRead;
	}

	public long skip(long n) throws IOException {
		long bytesSkipped = super.skip(n);
		this.consumed += bytesSkipped;
		this.counter += bytesSkipped;
		return bytesSkipped;
	}

	public void unread(int b) throws IOException {
		super.unread(b);
		this.consumed -= 1L;
		this.counter -= 1L;
	}

	public void unread(byte[] b) throws IOException {
		unread(b, 0, b.length);
	}

	public void unread(byte[] b, int off, int len) throws IOException {
		super.unread(b, off, len);
		this.consumed -= len;
		this.counter -= len;
	}

	public String readLine() throws IOException {
		StringBuffer sb = new StringBuffer(128);
		while (true) {
			int b = read();
			if (b == -1) {
				return null;
			}
			if (b == 10) {
				break;
			}
			if (b != 13)
				;
			sb.append((char) b);
		}

		return sb.toString();
	}

	public int readFully(byte[] buffer) throws IOException {
		int readOffset = 0;
		int readRemaining = buffer.length;
		int readLast = 0;
		while ((readRemaining > 0) && (readLast != -1)) {
			readRemaining -= readLast;
			readOffset += readLast;
			readLast = read(buffer, readOffset, readRemaining);
		}
		if (readRemaining > 0) {
			unread(buffer, 0, readOffset);
			readOffset = 0;
		}
		return readOffset;
	}

	public int peek(byte[] buffer) throws IOException {
		int readOffset = 0;
		int readRemaining = buffer.length;
		int readLast = 0;
		while ((readRemaining > 0) && (readLast != -1)) {
			readRemaining -= readLast;
			readOffset += readLast;
			readLast = read(buffer, readOffset, readRemaining);
		}
		if (readOffset > 0) {
			unread(buffer, 0, readOffset);
		}
		return readOffset;
	}
}