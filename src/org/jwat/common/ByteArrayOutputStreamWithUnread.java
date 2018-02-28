/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamWithUnread extends ByteArrayOutputStream {
	public ByteArrayOutputStreamWithUnread() {
	}

	public ByteArrayOutputStreamWithUnread(final int size) {
		super(size);
	}

	public void unread(final int b) {
		if (this.count == 0) {
			throw new IllegalArgumentException("Can not unread more that buffered!");
		}
		--this.count;
	}

	public void unread(final byte[] b) {
		if (this.count < b.length) {
			throw new IllegalArgumentException("Can not unread more that buffered!");
		}
		this.count -= b.length;
	}

	public void unread(final byte[] b, final int off, final int len) {
		if (this.count < len) {
			throw new IllegalArgumentException("Can not unread more that buffered!");
		}
		this.count -= len;
	}
}