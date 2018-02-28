/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UTF8 {
	public int utf8_c;
	public byte[] chars_read;
	public boolean bValidChar;

	public UTF8() {
		this.bValidChar = false;
	}

	public int readUtf8(int c, final InputStream in) throws IOException {
		final ByteArrayOutputStream charsOut = new ByteArrayOutputStream(4);
		this.utf8_c = 0;
		this.bValidChar = false;
		if ((c & 0x80) == 0x0) {
			this.bValidChar = true;
			this.utf8_c = c;
		} else {
			byte utf8_read = 1;
			this.bValidChar = true;
			byte utf8_octets;
			if ((c & 0xE0) == 0xC0) {
				this.utf8_c = (c & 0x1F);
				utf8_octets = 2;
			} else if ((c & 0xF0) == 0xE0) {
				this.utf8_c = (c & 0xF);
				utf8_octets = 3;
			} else if ((c & 0xF8) == 0xF0) {
				this.utf8_c = (c & 0x7);
				utf8_octets = 4;
			} else {
				this.utf8_c = 0;
				utf8_read = 0;
				utf8_octets = 0;
				this.bValidChar = false;
			}
			while (this.bValidChar && utf8_read < utf8_octets) {
				c = in.read();
				if (c == -1) {
					this.bValidChar = false;
					this.chars_read = charsOut.toByteArray();
					return -1;
				}
				charsOut.write(c);
				if ((c & 0xC0) == 0x80) {
					this.utf8_c = (this.utf8_c << 6 | (c & 0x3F));
					++utf8_read;
				} else {
					this.bValidChar = false;
				}
			}
			if (utf8_read == utf8_octets) {
				switch (utf8_octets) {
				case 2: {
					if (this.utf8_c < 128) {
						this.bValidChar = false;
						break;
					}
					break;
				}
				case 3: {
					if (this.utf8_c < 2048) {
						this.bValidChar = false;
						break;
					}
					break;
				}
				case 4: {
					if (this.utf8_c < 65536) {
						this.bValidChar = false;
						break;
					}
					break;
				}
				}
			}
			c = this.utf8_c;
		}
		this.chars_read = charsOut.toByteArray();
		return c;
	}

	public int writeUtf8(final int c, final OutputStream out) throws IOException {
		byte utf8_write = 1;
		byte utf8_octets;
		int shift;
		if (c < 128) {
			out.write(c);
			utf8_octets = 1;
			shift = 0;
		} else if (c < 2048) {
			final int b = c >> 6 | 0xC0;
			out.write(b);
			utf8_octets = 2;
			shift = 0;
		} else if (c < 65536) {
			final int b = c >> 12 | 0xE0;
			out.write(b);
			utf8_octets = 3;
			shift = 6;
		} else {
			if (c >= 1114112) {
				throw new IOException("Character (0x" + Integer.toHexString(c) + ") not UTF-8 encodable!");
			}
			final int b = c >> 18 | 0xF0;
			out.write(b);
			utf8_octets = 4;
			shift = 12;
		}
		while (utf8_write < utf8_octets) {
			final int b = (c >> shift & 0x3F) | 0x80;
			out.write(b);
			shift -= 6;
			++utf8_write;
		}
		return utf8_write;
	}
}