/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.ByteArrayOutputStream;

public class ISO8859_1 {
	public static final byte[] validBytes;
	public byte[] encoded;
	public String decoded;

	public boolean encode(final String inStr, final String exceptions) {
		boolean valid = true;
		final StringBuffer sb = new StringBuffer();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < inStr.length(); ++i) {
			final char c = inStr.charAt(i);
			if (c < 'Ā') {
				if (ISO8859_1.validBytes[c] != 0 || exceptions.indexOf(c) != -1) {
					sb.append(c);
					out.write(c);
				} else {
					valid = false;
				}
			} else {
				valid = false;
			}
		}
		this.decoded = sb.toString();
		this.encoded = out.toByteArray();
		return valid;
	}

	public boolean decode(final byte[] inBytes, final String exceptions) {
		boolean valid = true;
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < inBytes.length; ++i) {
			final int c = inBytes[i] & 0xFF;
			if (ISO8859_1.validBytes[c] != 0 || exceptions.indexOf(c) != -1) {
				sb.append((char) c);
			} else {
				valid = false;
			}
		}
		this.decoded = sb.toString();
		return valid;
	}

	static {
		validBytes = new byte[256];
		for (int i = 0; i < 32; ++i) {
			ISO8859_1.validBytes[i] = 0;
		}
		for (int i = 32; i < 127; ++i) {
			ISO8859_1.validBytes[i] = (byte) i;
		}
		ISO8859_1.validBytes[127] = 0;
		for (int i = 128; i < 256; ++i) {
			ISO8859_1.validBytes[i] = (byte) i;
		}
	}
}