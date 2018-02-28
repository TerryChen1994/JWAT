/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.Arrays;
import java.io.ByteArrayOutputStream;

public class Base64 {
	private static String encodeTab;
	public static byte[] decodeTab;

	public static String decodeToString(final String in, final boolean bStrict) {
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
				final char cin = in.charAt(idx);
				if (cin == '=') {
					b = false;
				} else {
					++idx;
					final int cIdx = Base64.decodeTab[cin];
					if (cIdx == -1) {
						return null;
					}
					switch (mod) {
					case 0: {
						cout = cIdx << 2;
						break;
					}
					case 1: {
						cout |= cIdx >> 4;
						out.append((char) cout);
						cout = (cIdx << 4 & 0xFF);
						break;
					}
					case 2: {
						cout |= cIdx >> 2;
						out.append((char) cout);
						cout = (cIdx << 6 & 0xFF);
						break;
					}
					case 3: {
						cout |= cIdx;
						out.append((char) cout);
						break;
					}
					}
					mod = (mod + 1) % 4;
				}
			} else {
				b = false;
			}
		}
		if (mod == 1) {
			return null;
		}
		if (bStrict) {
			while (mod != 0 && idx < in.length() && in.charAt(idx) == '=') {
				++idx;
				mod = (mod + 1) % 4;
			}
			if (mod != 0 || idx < in.length()) {
				return null;
			}
		}
		return out.toString();
	}

	public static byte[] decodeToArray(final String in, final boolean bStrict) {
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
				final char cin = in.charAt(idx);
				if (cin == '=') {
					b = false;
				} else {
					++idx;
					final int cIdx = Base64.decodeTab[cin];
					if (cIdx == -1) {
						return null;
					}
					switch (mod) {
					case 0: {
						cout = cIdx << 2;
						break;
					}
					case 1: {
						cout |= cIdx >> 4;
						out.write(cout);
						cout = (cIdx << 4 & 0xFF);
						break;
					}
					case 2: {
						cout |= cIdx >> 2;
						out.write(cout);
						cout = (cIdx << 6 & 0xFF);
						break;
					}
					case 3: {
						cout |= cIdx;
						out.write(cout);
						break;
					}
					}
					mod = (mod + 1) % 4;
				}
			} else {
				b = false;
			}
		}
		if (mod == 1) {
			return null;
		}
		if (bStrict) {
			while (mod != 0 && idx < in.length() && in.charAt(idx) == '=') {
				++idx;
				mod = (mod + 1) % 4;
			}
			if (mod != 0 || idx < in.length()) {
				return null;
			}
		}
		return out.toByteArray();
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
		int cout = 0;
		int mod = 0;
		while (b) {
			if (idx < in.length()) {
				final char cin = in.charAt(idx++);
				if (cin >= 'Ā') {
					return null;
				}
				switch (mod) {
				case 0: {
					cout = (cin >> 2 & '?');
					out.append(Base64.encodeTab.charAt(cout));
					cout = (cin << 4 & '?');
					break;
				}
				case 1: {
					cout |= (cin >> 4 & '?');
					out.append(Base64.encodeTab.charAt(cout));
					cout = (cin << 2 & '?');
					break;
				}
				case 2: {
					cout |= (cin >> 6 & '?');
					out.append(Base64.encodeTab.charAt(cout));
					cout = (cin & '?');
					out.append(Base64.encodeTab.charAt(cout));
					break;
				}
				}
				mod = (mod + 1) % 3;
			} else {
				b = false;
			}
		}
		switch (mod) {
		case 1: {
			out.append(Base64.encodeTab.charAt(cout));
			out.append("==");
			break;
		}
		case 2: {
			out.append(Base64.encodeTab.charAt(cout));
			out.append("=");
			break;
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
		int cout = 0;
		int mod = 0;
		while (b) {
			if (idx < in.length) {
				final int cin = in[idx++] & 0xFF;
				switch (mod) {
				case 0: {
					cout = (cin >> 2 & 0x3F);
					out.append(Base64.encodeTab.charAt(cout));
					cout = (cin << 4 & 0x3F);
					break;
				}
				case 1: {
					cout |= (cin >> 4 & 0x3F);
					out.append(Base64.encodeTab.charAt(cout));
					cout = (cin << 2 & 0x3F);
					break;
				}
				case 2: {
					cout |= (cin >> 6 & 0x3F);
					out.append(Base64.encodeTab.charAt(cout));
					cout = (cin & 0x3F);
					out.append(Base64.encodeTab.charAt(cout));
					break;
				}
				}
				mod = (mod + 1) % 3;
			} else {
				b = false;
			}
		}
		switch (mod) {
		case 1: {
			out.append(Base64.encodeTab.charAt(cout));
			out.append("==");
			break;
		}
		case 2: {
			out.append(Base64.encodeTab.charAt(cout));
			out.append("=");
			break;
		}
		}
		return out.toString();
	}

	static {
		Base64.encodeTab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		Arrays.fill(Base64.decodeTab = new byte[256], (byte) (-1));
		for (int i = 0; i < Base64.encodeTab.length(); ++i) {
			Base64.decodeTab[Base64.encodeTab.charAt(i)] = (byte) i;
		}
	}
}