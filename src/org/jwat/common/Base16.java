/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.Arrays;
import java.io.ByteArrayOutputStream;

public class Base16 {
	public static String encodeTab;
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
				final int cIdx = Base16.decodeTab[cin];
				if (cIdx == -1) {
					return null;
				}
				switch (mod) {
				case 0: {
					cout = cIdx << 4;
					break;
				}
				case 1: {
					cout |= cIdx;
					out.append((char) cout);
					break;
				}
				}
				mod = (mod + 1) % 2;
			} else {
				b = false;
			}
		}
		if (mod != 1) {
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
				final int cIdx = Base16.decodeTab[cin];
				if (cIdx == -1) {
					return null;
				}
				switch (mod) {
				case 0: {
					cout = cIdx << 4;
					break;
				}
				case 1: {
					cout |= cIdx;
					out.write(cout);
					break;
				}
				}
				mod = (mod + 1) % 2;
			} else {
				b = false;
			}
		}
		if (mod != 1) {
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
		final StringBuffer out = new StringBuffer(256);
		boolean b = true;
		int idx = 0;
		while (b) {
			if (idx < in.length()) {
				final int cin = in.charAt(idx++);
				if (cin >= 256) {
					return null;
				}
				out.append(Base16.encodeTab.charAt(cin >> 4 & 0xF));
				out.append(Base16.encodeTab.charAt(cin & 0xF));
			} else {
				b = false;
			}
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
		final StringBuffer out = new StringBuffer(256);
		boolean b = true;
		int idx = 0;
		while (b) {
			if (idx < in.length) {
				final int cin = in[idx++] & 0xFF;
				out.append(Base16.encodeTab.charAt(cin >> 4 & 0xF));
				out.append(Base16.encodeTab.charAt(cin & 0xF));
			} else {
				b = false;
			}
		}
		return out.toString();
	}

	static {
		Base16.encodeTab = "0123456789ABCDEF";
		Arrays.fill(Base16.decodeTab = new byte[256], (byte) (-1));
		for (int i = 0; i < Base16.encodeTab.length(); ++i) {
			Base16.decodeTab[Character.toUpperCase(Base16.encodeTab.charAt(i))] = (byte) i;
			Base16.decodeTab[Character.toLowerCase(Base16.encodeTab.charAt(i))] = (byte) i;
		}
	}
}