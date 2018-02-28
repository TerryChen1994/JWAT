/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class Base32 {
	public static String encodeTab = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

	public static byte[] decodeTab = new byte[256];

	public static String decodeToString(String in, boolean bStrict) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return "";
		}

		StringBuffer out = new StringBuffer(256);

		boolean b = true;
		int idx = 0;

		int mod = 0;

		int cout = 0;

		while (b) {
			if (idx < in.length()) {
				char cin = in.charAt(idx);
				if (cin == '=') {
					b = false;
				}

				++idx;
				int cIdx = decodeTab[cin];
				if (cIdx != -1) {
					switch (mod) {
					case 0:
						cout = cIdx << 3;
						break;
					case 1:
						cout |= cIdx >> 2;
						out.append((char) cout);
						cout = cIdx << 6 & 0xFF;
						break;
					case 2:
						cout |= cIdx << 1;
						break;
					case 3:
						cout |= cIdx >> 4;
						out.append((char) cout);
						cout = cIdx << 4 & 0xFF;
						break;
					case 4:
						cout |= cIdx >> 1;
						out.append((char) cout);
						cout = cIdx << 7 & 0xFF;
						break;
					case 5:
						cout |= cIdx << 2;
						break;
					case 6:
						cout |= cIdx >> 3;
						out.append((char) cout);
						cout = cIdx << 5 & 0xFF;
						break;
					case 7:
						cout |= cIdx;
						out.append((char) cout);
					}

					mod = (mod + 1) % 8;
				}

				return null;
			}

			b = false;
		}

		switch (mod) {
		case 1:
		case 3:
		case 6:
			return null;
		}
		if (bStrict) {
			while ((mod != 0) && (idx < in.length()) && (in.charAt(idx) == '=')) {
				++idx;
				mod = (mod + 1) % 8;
			}
			if ((mod != 0) || (idx < in.length())) {
				return null;
			}

		}

		return out.toString();
	}

	public static byte[] decodeToArray(String in, boolean bStrict) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return new byte[0];
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		boolean b = true;
		int idx = 0;

		int mod = 0;

		int cout = 0;

		while (b) {
			if (idx < in.length()) {
				char cin = in.charAt(idx);
				if (cin == '=') {
					b = false;
				}

				++idx;
				int cIdx = decodeTab[cin];
				if (cIdx != -1) {
					switch (mod) {
					case 0:
						cout = cIdx << 3;
						break;
					case 1:
						cout |= cIdx >> 2;
						out.write(cout);
						cout = cIdx << 6 & 0xFF;
						break;
					case 2:
						cout |= cIdx << 1;
						break;
					case 3:
						cout |= cIdx >> 4;
						out.write(cout);
						cout = cIdx << 4 & 0xFF;
						break;
					case 4:
						cout |= cIdx >> 1;
						out.write(cout);
						cout = cIdx << 7 & 0xFF;
						break;
					case 5:
						cout |= cIdx << 2;
						break;
					case 6:
						cout |= cIdx >> 3;
						out.write(cout);
						cout = cIdx << 5 & 0xFF;
						break;
					case 7:
						cout |= cIdx;
						out.write(cout);
					}

					mod = (mod + 1) % 8;
				}

				return null;
			}

			b = false;
		}

		switch (mod) {
		case 1:
		case 3:
		case 6:
			return null;
		}
		if (bStrict) {
			while ((mod != 0) && (idx < in.length()) && (in.charAt(idx) == '=')) {
				++idx;
				mod = (mod + 1) % 8;
			}
			if ((mod != 0) || (idx < in.length())) {
				return null;
			}

		}

		return out.toByteArray();
	}

	public static String encodeString(String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return "";
		}

		StringBuffer out = new StringBuffer(256);

		boolean b = true;
		int idx = 0;

		int cout = 0;
		int mod = 0;

		while (b) {
			if (idx < in.length()) {
				int cin = in.charAt(idx++);
				if (cin < 256) {
					switch (mod) {
					case 0:
						cout = cin >> 3 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin << 2 & 0x1F;
						break;
					case 1:
						cout |= cin >> 6 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin >> 1 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin << 4 & 0x1F;
						break;
					case 2:
						cout |= cin >> 4 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin << 1 & 0x1F;
						break;
					case 3:
						cout |= cin >> 7 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin >> 2 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin << 3 & 0x1F;
						break;
					case 4:
						cout |= cin >> 5 & 0x1F;
						out.append(encodeTab.charAt(cout));
						cout = cin & 0x1F;
						out.append(encodeTab.charAt(cout));
					}

					mod = (mod + 1) % 5;
				}

				return null;
			}

			b = false;
		}

		switch (mod) {
		case 0:
			break;
		case 1:
			out.append(encodeTab.charAt(cout));
			out.append("======");
			break;
		case 2:
			out.append(encodeTab.charAt(cout));
			out.append("====");
			break;
		case 3:
			out.append(encodeTab.charAt(cout));
			out.append("===");
			break;
		case 4:
			out.append(encodeTab.charAt(cout));
			out.append("=");
		}

		return out.toString();
	}

	public static String encodeArray(byte[] in) {
		if (in == null) {
			return null;
		}
		if (in.length == 0) {
			return "";
		}

		StringBuffer out = new StringBuffer(256);

		boolean b = true;
		int idx = 0;

		int cout = 0;
		int mod = 0;

		while (b) {
			if (idx < in.length) {
				int cin = in[(idx++)] & 0xFF;
				switch (mod) {
				case 0:
					cout = cin >> 3 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin << 2 & 0x1F;
					break;
				case 1:
					cout |= cin >> 6 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin >> 1 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin << 4 & 0x1F;
					break;
				case 2:
					cout |= cin >> 4 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin << 1 & 0x1F;
					break;
				case 3:
					cout |= cin >> 7 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin >> 2 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin << 3 & 0x1F;
					break;
				case 4:
					cout |= cin >> 5 & 0x1F;
					out.append(encodeTab.charAt(cout));
					cout = cin & 0x1F;
					out.append(encodeTab.charAt(cout));
				}

				mod = (mod + 1) % 5;
			}

			b = false;
		}

		switch (mod) {
		case 0:
			break;
		case 1:
			out.append(encodeTab.charAt(cout));
			out.append("======");
			break;
		case 2:
			out.append(encodeTab.charAt(cout));
			out.append("====");
			break;
		case 3:
			out.append(encodeTab.charAt(cout));
			out.append("===");
			break;
		case 4:
			out.append(encodeTab.charAt(cout));
			out.append("=");
		}

		return out.toString();
	}

	static {
		for (int i = 0; i < encodeTab.length(); ++i) {
			decodeTab[java.lang.Character.toUpperCase(encodeTab.charAt(i))] = (byte) i;
			decodeTab[java.lang.Character.toLowerCase(encodeTab.charAt(i))] = (byte) i;
		}
	}
}