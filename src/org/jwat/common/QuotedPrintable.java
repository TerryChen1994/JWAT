/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.ByteArrayOutputStream;

public class QuotedPrintable {
	public static byte[] decode(final String encoded_text) {
		final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		int idx = 0;
		while (idx < encoded_text.length()) {
			int c = encoded_text.charAt(idx++);
			if (c == 95) {
				bytesOut.write(32);
			} else if (c == 61) {
				if (idx + 2 > encoded_text.length()) {
					return null;
				}
				c = encoded_text.charAt(idx++);
				c = Base16.decodeTab[c];
				if (c == -1) {
					return null;
				}
				int cout = c << 4;
				c = encoded_text.charAt(idx++);
				c = Base16.decodeTab[c];
				if (c == -1) {
					return null;
				}
				cout |= c;
				bytesOut.write(cout);
			} else {
				if (c <= 32 || c >= 127) {
					return null;
				}
				bytesOut.write(c);
			}
		}
		return bytesOut.toByteArray();
	}
}