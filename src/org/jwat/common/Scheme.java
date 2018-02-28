/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

public class Scheme {
	protected static int[] bf;

	public static boolean startsWithScheme(final byte[] bytes) {
		boolean result = false;
		if (bytes != null) {
			int idx = 0;
			boolean bLoop = true;
			while (bLoop) {
				if (idx < bytes.length) {
					final int c = bytes[idx];
					if (c == 58) {
						bLoop = false;
						if (idx > 0) {
							result = true;
						}
					} else if (idx > 0) {
						bLoop = ((Scheme.bf[c & 0xFF] & 0x2) != 0x0);
					} else {
						bLoop = ((Scheme.bf[c & 0xFF] & 0x1) != 0x0);
					}
				} else {
					bLoop = false;
				}
				++idx;
			}
		}
		return result;
	}

	public static String getScheme(final String uri) {
		if (uri != null) {
			final StringBuilder sb = new StringBuilder();
			int idx = 0;
			boolean bLoop = true;
			while (bLoop) {
				if (idx < uri.length()) {
					final int c = uri.charAt(idx);
					if (c < 256) {
						if (c == 58) {
							if (idx > 0) {
								return sb.toString();
							}
						} else if (idx > 0) {
							sb.append((char) c);
							bLoop = ((Scheme.bf[c] & 0x2) != 0x0);
						} else {
							sb.append((char) c);
							bLoop = ((Scheme.bf[c] & 0x1) != 0x0);
						}
					} else {
						bLoop = false;
					}
				} else {
					bLoop = false;
				}
				++idx;
			}
		}
		return null;
	}

	static {
		Scheme.bf = new int[256];
		String alphas = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < alphas.length(); ++i) {
			Scheme.bf[alphas.charAt(i)] = 3;
		}
		alphas = alphas.toUpperCase();
		for (int i = 0; i < alphas.length(); ++i) {
			Scheme.bf[alphas.charAt(i)] = 3;
		}
		final String digits = "1234567890";
		for (int j = 0; j < digits.length(); ++j) {
			Scheme.bf[digits.charAt(j)] = 2;
		}
		final String scheme = "+-.";
		for (int k = 0; k < scheme.length(); ++k) {
			Scheme.bf[scheme.charAt(k)] = 2;
		}
	}
}