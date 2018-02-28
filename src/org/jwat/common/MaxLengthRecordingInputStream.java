/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MaxLengthRecordingInputStream extends FilterInputStream {
	public static final int SKIP_READ_BUFFER_SIZE = 1024;
	protected byte[] skip_read_buffer = new byte[1024];
	protected ByteArrayOutputStream record;
	protected long available;

	public MaxLengthRecordingInputStream(InputStream in, long available) {
		super(in);
		this.record = new ByteArrayOutputStream();
		this.available = available;
	}

	public byte[] getRecording() {
		return this.record.toByteArray();
	}

	public void close() throws IOException {
		this.record.close();
	}

	public int available() throws IOException {
		return ((this.available > 2147483647L) ? 2147483647 : (int) this.available);
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
		int b = -1;
		if (this.available > 0L) {
			b = this.in.read();
			if (b != -1) {
				this.available -= 1L;
				this.record.write(b);
			}
		}
		return b;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = -1;
		if (this.available > 0L) {
			bytesRead = this.in.read(b, off, (int) Math.min(len, this.available));
			if (bytesRead > 0) {
				this.available -= bytesRead;
				this.record.write(b, off, bytesRead);
			}
		}
		return bytesRead;
	}

	public long skip(long n) throws IOException {
		long bytesSkipped = 0L;
		if (this.available > 0L) {
			bytesSkipped = read(this.skip_read_buffer, 0, (int) Math.min(Math.min(n, this.available), 1024L));
			if (bytesSkipped == -1L) {
				bytesSkipped = 0L;
			}
		}
		return bytesSkipped;
	}
}