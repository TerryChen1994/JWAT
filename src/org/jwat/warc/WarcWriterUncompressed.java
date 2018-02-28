/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class WarcWriterUncompressed extends WarcWriter {
	WarcWriterUncompressed(final OutputStream out) {
		if (out == null) {
			throw new IllegalArgumentException("The 'out' parameter is null!");
		}
		this.out = out;
		this.init();
	}

	WarcWriterUncompressed(final OutputStream out, final int buffer_size) {
		if (out == null) {
			throw new IllegalArgumentException("The 'out' parameter is null!");
		}
		if (buffer_size <= 0) {
			throw new IllegalArgumentException("The 'buffer_size' parameter is less than or equal to zero!");
		}
		this.out = new BufferedOutputStream(out, buffer_size);
		this.init();
	}

	public boolean isCompressed() {
		return false;
	}

	public void close() throws IOException {
		if (this.state == 1 || this.state == 2) {
			this.closeRecord();
		}
		if (this.out != null) {
			this.out.flush();
			this.out.close();
			this.out = null;
		}
	}

	public void closeRecord() throws IOException {
		if (this.state == 1 || this.state == 2) {
			this.closeRecord_impl();
			this.state = 3;
		} else if (this.state == 0) {
			throw new IllegalStateException("Please write a record before closing it!");
		}
	}

	public byte[] writeHeader(final WarcRecord record) throws IOException {
		if (record == null) {
			throw new IllegalArgumentException("The 'record' parameter is null!");
		}
		if (this.state == 1) {
			throw new IllegalStateException("Headers written back to back!");
		}
		if (this.state == 2) {
			this.closeRecord_impl();
		}
		return this.writeHeader_impl(record);
	}
}