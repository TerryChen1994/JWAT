/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.OutputStream;

public class RandomAccessFileOutputStream extends OutputStream {
	protected RandomAccessFile raf;

	public RandomAccessFileOutputStream(final RandomAccessFile raf) {
		this.raf = raf;
	}

	@Override
	public void close() throws IOException {
		this.raf = null;
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(final int b) throws IOException {
		this.raf.write(b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		this.raf.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		this.raf.write(b, off, len);
	}
}