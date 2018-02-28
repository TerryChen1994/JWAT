/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.Arrays;
import java.io.ByteArrayOutputStream;

public class Base2 {
	public static String[] encodeTab;
	public static byte[] decodeTab;

	public static String decodeToString(final String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return "";
		}
		final StringBuffer out = new StringBuffer(256);
		boolean b = true;
		int idx = 0;
		int mod = 0;
		int cout = 0;
		while (b) {
			if (idx < in.length()) {
				final char cin = in.charAt(idx++);
				final int cIdx = Base2.decodeTab[cin];
				if (cIdx == -1) {
					return null;
				}
				cout = (cout << 1 | cIdx);
				mod = (mod + 1) % 8;
				if (mod != 0) {
					continue;
				}
				out.append((char) cout);
				cout = 0;
			} else {
				b = false;
			}
		}
		if (mod == 0) {
			return out.toString();
		}
		return null;
	}

	public static byte[] decodeToArray(final String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return new byte[0];
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean b = true;
		int idx = 0;
		int mod = 0;
		int cout = 0;
		while (b) {
			if (idx < in.length()) {
				final char cin = in.charAt(idx++);
				final int cIdx = Base2.decodeTab[cin];
				if (cIdx == -1) {
					return null;
				}
				cout = (cout << 1 | cIdx);
				mod = (mod + 1) % 8;
				if (mod != 0) {
					continue;
				}
				out.write(cout);
			} else {
				b = false;
			}
		}
		if (mod == 0) {
			return out.toByteArray();
		}
		return null;
	}

	public static String encodeString(final String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return "";
		}
		final StringBuffer out = new StringBuffer(in.length() << 3);
		for (int i = 0; i < in.length(); ++i) {
			final int cin = in.charAt(i);
			if (cin >= 256) {
				return null;
			}
			out.append(Base2.encodeTab[cin >> 4 & 0xF]);
			out.append(Base2.encodeTab[cin & 0xF]);
		}
		return out.toString();
	}

	public static String encodeArray(final byte[] in) {
		if (in == null) {
			return null;
		}
		if (in.length == 0) {
			return "";
		}
		final StringBuffer out = new StringBuffer(in.length << 3);
		for (int i = 0; i < in.length; ++i) {
			out.append(Base2.encodeTab[in[i] >> 4 & 0xF]);
			out.append(Base2.encodeTab[in[i] & 0xF]);
		}
		return out.toString();
	}

	public static String delimit(final String inStr, final int width, final char delimiter) {
		if (inStr == null) {
			return null;
		}
		if (inStr.length() == 0) {
			return "";
		}
		if (width <= 0) {
			return inStr;
		}
		final StringBuffer outSb = new StringBuffer(inStr.length() + (inStr.length() + width - 1) / width);
		int idx = 0;
		int lIdx = width;
		while (idx < inStr.length()) {
			if (idx == lIdx) {
				outSb.append(delimiter);
				lIdx += width;
			}
			outSb.append(inStr.charAt(idx++));
		}
		return outSb.toString();
	}

	static {
		Base2.encodeTab = new String[] { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001",
				"1010", "1011", "1100", "1101", "1110", "1111" };
		Arrays.fill(Base2.decodeTab = new byte[256], (byte) (-1));
		Base2.decodeTab[48] = 0;
		Base2.decodeTab[49] = 1;
	}
}