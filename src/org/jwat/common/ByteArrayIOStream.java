/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class ByteArrayIOStream {
	public static final int DEFAULT_BUFFER_SIZE = 10485760;
	protected Semaphore lock;
	protected byte[] bytes;
	protected ByteBuffer byteBuffer;
	protected int limit;

	public ByteArrayIOStream() {
		this(10485760);
	}

	public ByteArrayIOStream(final int bufferSize) {
		this.lock = new Semaphore(1);
		this.limit = 0;
		this.bytes = new byte[bufferSize];
		this.byteBuffer = ByteBuffer.wrap(this.bytes);
	}

	public OutputStream getOutputStream() {
		if (!this.lock.tryAcquire()) {
			throw new IllegalStateException();
		}
		this.byteBuffer.clear();
		this.limit = 0;
		return new OutputStreamImpl(this);
	}

	public byte[] getBytes() {
		return this.bytes;
	}

	public int getLength() {
		return this.bytes.length;
	}

	public int getLimit() {
		return this.limit;
	}

	public ByteBuffer getBuffer() {
		final ByteBuffer buffer = ByteBuffer.wrap(this.bytes);
		buffer.position(0);
		buffer.limit(this.limit);
		return buffer;
	}

	public InputStream getInputStream() {
		if (!this.lock.tryAcquire()) {
			throw new IllegalStateException();
		}
		this.byteBuffer.clear();
		this.byteBuffer.limit(this.limit);
		return new InputStreamImpl(this);
	}

	protected void release() {
		this.lock = null;
		this.byteBuffer = null;
		this.bytes = null;
		this.limit = 0;
	}

	public static class OutputStreamImpl extends OutputStream {
		protected ByteArrayIOStream baios;
		protected ByteBuffer byteBuffer;

		protected OutputStreamImpl(final ByteArrayIOStream baios) {
			this.baios = baios;
			this.byteBuffer = baios.byteBuffer;
		}

		@Override
		public void close() {
			if (this.baios != null) {
				this.baios.limit = this.baios.byteBuffer.position();
				this.baios.lock.release();
				this.baios = null;
				this.byteBuffer = null;
			}
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			this.byteBuffer.put(b, off, len);
		}

		@Override
		public void write(final byte[] b) throws IOException {
			this.byteBuffer.put(b);
		}

		@Override
		public void write(final int b) throws IOException {
			this.byteBuffer.put((byte) b);
		}
	}

	public static class InputStreamImpl extends InputStream {
		protected ByteArrayIOStream baios;
		protected ByteBuffer byteBuffer;

		protected InputStreamImpl(final ByteArrayIOStream baios) {
			this.baios = baios;
			this.byteBuffer = baios.byteBuffer;
		}

		@Override
		public void close() {
			if (this.baios != null) {
				this.baios.lock.release();
				this.baios = null;
				this.byteBuffer = null;
			}
		}

		@Override
		public int available() throws IOException {
			return this.byteBuffer.limit() - this.byteBuffer.position();
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public synchronized void mark(final int readlimit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void reset() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public int read() throws IOException {
			if (this.byteBuffer.remaining() > 0) {
				return this.byteBuffer.get();
			}
			return -1;
		}

		@Override
		public int read(final byte[] b, final int off, int len) throws IOException {
			if (len == 0) {
				return 0;
			}
			final int remaining = this.byteBuffer.remaining();
			if (len > remaining) {
				len = remaining;
			}
			if (len > 0) {
				this.byteBuffer.get(b, off, len);
				return len;
			}
			return -1;
		}

		@Override
		public int read(final byte[] b) throws IOException {
			int len = b.length;
			if (len == 0) {
				return 0;
			}
			final int remaining = this.byteBuffer.remaining();
			if (len > remaining) {
				len = remaining;
			}
			if (len > 0) {
				this.byteBuffer.get(b, 0, len);
				return len;
			}
			return -1;
		}

		@Override
		public long skip(long n) throws IOException {
			final int remaining = this.byteBuffer.remaining();
			if (n > remaining) {
				n = remaining;
			}
			if (n > 0L) {
				this.byteBuffer.position(this.byteBuffer.position() + (int) n);
			}
			return n;
		}
	}
}