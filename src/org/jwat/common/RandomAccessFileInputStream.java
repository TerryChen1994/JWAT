/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.InputStream;

public class RandomAccessFileInputStream extends InputStream {
	protected RandomAccessFile raf;
	protected long mark_position;

	public RandomAccessFileInputStream(final RandomAccessFile raf) {
		this.mark_position = -1L;
		this.raf = raf;
	}

	@Override
	public void close() throws IOException {
		this.raf = null;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void mark(final int readlimit) {
		try {
			this.mark_position = this.raf.getFilePointer();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		if (this.mark_position == -1L) {
			throw new IOException("Mark not set or is invalid");
		}
		this.raf.seek(this.mark_position);
	}

	@Override
	public int available() throws IOException {
		final long avail = this.raf.length() - this.raf.getFilePointer();
		return (int) Math.min(avail, 2147483647L);
	}

	@Override
	public long skip(final long n) throws IOException {
		final long skip = Math.min(n, this.raf.length() - this.raf.getFilePointer());
		this.raf.seek(this.raf.getFilePointer() + skip);
		return skip;
	}

	@Override
	public int read() throws IOException {
		return this.raf.read();
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return this.raf.read(b);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return this.raf.read(b, off, len);
	}
}