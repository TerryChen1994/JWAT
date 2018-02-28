/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.net.URISyntaxException;

public class UriProfile {
	public static final int B_ALPHAS = 1;
	public static final int B_DIGITS = 2;
	public static final int B_SCHEME_FIRST = 4;
	public static final int B_SCHEME_FOLLOW = 8;
	public static final int B_UNRESERVED = 16;
	public static final int B_GEN_DELIMS = 32;
	public static final int B_SUB_DELIMS = 64;
	public static final int B_RESERVED = 128;
	public static final int B_PCHAR = 256;
	public static final int B_USERINFO = 512;
	public static final int B_REGNAME = 1024;
	public static final int B_SEGMENT = 2048;
	public static final int B_SEGMENT_NZ = 4096;
	public static final int B_SEGMENT_NZ_NC = 8192;
	public static final int B_PATH = 16384;
	public static final int B_QUERY = 32768;
	public static final int B_FRAGMENT = 65536;
	protected final int[] charTypeMap;
	public boolean bAllowRelativeUris;
	public boolean bAllow16bitPercentEncoding;
	public boolean bAllowinvalidPercentEncoding;
	public static int[] asciiHexTab;
	public static char[] hexTab;
	protected static int[] defaultCharTypeMap;
	public static final UriProfile RFC3986;
	public static final UriProfile RFC3986_ABS_16BIT;
	public static final UriProfile RFC3986_ABS_16BIT_LAX;

	public UriProfile() {
		this.charTypeMap = new int[256];
		for (int i = 0; i < UriProfile.defaultCharTypeMap.length; ++i) {
			this.charTypeMap[i] = UriProfile.defaultCharTypeMap[i];
		}
		this.bAllowRelativeUris = true;
		this.bAllow16bitPercentEncoding = false;
		this.bAllowinvalidPercentEncoding = false;
	}

	public UriProfile(final UriProfile uriProfile) {
		this.charTypeMap = new int[256];
		for (int i = 0; i < this.charTypeMap.length; ++i) {
			this.charTypeMap[i] = uriProfile.charTypeMap[i];
		}
		this.bAllowRelativeUris = uriProfile.bAllowRelativeUris;
		this.bAllow16bitPercentEncoding = uriProfile.bAllow16bitPercentEncoding;
		this.bAllowinvalidPercentEncoding = uriProfile.bAllowinvalidPercentEncoding;
	}

	public void charTypeAddAndOr(final String chars, final int bw_and, final int bw_or) {
		charTypeAddAndOr(this.charTypeMap, chars, bw_and, bw_or);
	}

	public int indexOf(final int bw_and, final String str, int pos) throws URISyntaxException {
		for (int limit = str.length(); pos < limit; ++pos) {
			final char c = str.charAt(pos);
			if (c >= 'Ā') {
				throw new URISyntaxException(str, "Invalid URI character '"
						+ (Character.isISOControl(c) ? String.format("0x%02x", (int) c) : c) + "'");
			}
			if ((this.charTypeMap[c] & bw_and) != 0x0) {
				return pos;
			}
		}
		return -1;
	}

	public void validate_first_follow(final String str, final int bw_and_first, final int bw_and_follow)
			throws URISyntaxException {
		for (int pos = 0, limit = str.length(); pos < limit; ++pos) {
			final char c = str.charAt(pos);
			if (pos == 0 && (this.charTypeMap[c] & 0x4) == 0x0) {
				throw new URISyntaxException(str, "Invalid URI scheme component");
			}
			if ((this.charTypeMap[c] & 0x8) == 0x0) {
				throw new URISyntaxException(str, "Invalid URI scheme component");
			}
		}
	}

	public String validate_decode(final int bw_and, final String componentName, final String str)
			throws URISyntaxException {
		final StringBuilder sb = new StringBuilder();
		int pos = 0;
		final int limit = str.length();
		int decode = 0;
		while (pos < limit) {
			char c = str.charAt(pos++);
			if (c >= 'Ā') {
				throw new URISyntaxException(str, "Invalid URI " + componentName + " component - invalid character '"
						+ (Character.isISOControl(c) ? String.format("0x%02x", (int) c) : c) + "'");
			}
			if ((this.charTypeMap[c] & bw_and) == 0x0) {
				if (c != '%') {
					throw new URISyntaxException(str,
							"Invalid URI " + componentName + " component - invalid character '"
									+ (Character.isISOControl(c) ? String.format("0x%02x", (int) c) : c) + "'");
				}
				int ppos = pos - 1;
				boolean bValid;
				if (pos < limit) {
					c = str.charAt(pos);
					if (c == 'u' || c == 'U') {
						if (!this.bAllow16bitPercentEncoding) {
							if (!this.bAllowinvalidPercentEncoding) {
								throw new URISyntaxException(str, "Invalid URI " + componentName
										+ " component - 16-bit percent encoding not allowed");
							}
							bValid = false;
						} else {
							++pos;
							decode = 4;
							bValid = true;
						}
					} else {
						decode = 2;
						bValid = true;
					}
					char decodedC = '\0';
					while (bValid && decode > 0) {
						if (pos < limit) {
							c = str.charAt(pos++);
							decodedC <<= 4;
							if (c < 'Ā') {
								final int tmpC = UriProfile.asciiHexTab[c];
								if (tmpC != -1) {
									decodedC |= (char) tmpC;
									--decode;
								} else {
									bValid = false;
								}
							} else {
								bValid = false;
							}
						} else {
							bValid = false;
						}
					}
					if (!bValid && !this.bAllowinvalidPercentEncoding) {
						throw new URISyntaxException(str,
								"Invalid URI " + componentName + " component - invalid percent encoding");
					}
					sb.append(decodedC);
				} else {
					if (!this.bAllowinvalidPercentEncoding) {
						throw new URISyntaxException(str,
								"Invalid URI " + componentName + " component - incomplete percent encoding");
					}
					bValid = false;
				}
				if (bValid) {
					continue;
				}
				while (ppos < pos) {
					sb.append(str.charAt(ppos++));
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static void charTypeAddAndOr(final int[] charTypeMap, final String chars, final int bw_and,
			final int bw_or) {
		if (chars != null) {
			for (int i = 0; i < chars.length(); ++i) {
				final char char1 = chars.charAt(i);
				charTypeMap[char1] |= bw_or;
			}
		}
		if (bw_and != 0) {
			for (int i = 0; i < charTypeMap.length; ++i) {
				if ((charTypeMap[i] & bw_and) != 0x0) {
					final int n = i;
					charTypeMap[n] |= bw_or;
				}
			}
		}
	}

	static {
		UriProfile.asciiHexTab = new int[256];
		UriProfile.hexTab = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
				'F' };
		String hex = "0123456789abcdef";
		for (int i = 0; i < UriProfile.asciiHexTab.length; ++i) {
			UriProfile.asciiHexTab[i] = hex.indexOf(i);
		}
		hex = hex.toUpperCase();
		for (int i = 0; i < hex.length(); ++i) {
			UriProfile.asciiHexTab[hex.charAt(i)] = i;
		}
		UriProfile.defaultCharTypeMap = new int[256];
		final String alphas = "abcdefghijklmnopqrstuvwxyz";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, alphas, 0, 1);
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, alphas.toUpperCase(), 0, 1);
		final String digits = "1234567890";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, digits, 0, 2);
		final String scheme = "+-.";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, null, 1, 12);
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, scheme, 2, 8);
		final String unreserved = "-._~";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, unreserved, 3, 16);
		final String genDelims = ":/?#[]@";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, genDelims, 0, 160);
		final String subDelims = "!$&'()*+,;=";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, subDelims, 0, 192);
		final String pchar = ":@";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, pchar, 80, 256);
		final String userinfo = ":";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, userinfo, 80, 512);
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, null, 80, 1024);
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, null, 256, 2048);
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, null, 256, 4096);
		final String segment_nz_nc = "@";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, segment_nz_nc, 80, 8192);
		final String path = "/";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, path, 256, 16384);
		final String query = "/?";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, query, 256, 32768);
		final String fragment = "/?";
		charTypeAddAndOr(UriProfile.defaultCharTypeMap, fragment, 256, 65536);
		RFC3986 = new UriProfile();
		RFC3986_ABS_16BIT = new UriProfile();
		UriProfile.RFC3986_ABS_16BIT.bAllowRelativeUris = false;
		UriProfile.RFC3986_ABS_16BIT.bAllow16bitPercentEncoding = true;
		UriProfile.RFC3986_ABS_16BIT.bAllowinvalidPercentEncoding = false;
		final StringBuilder sb = new StringBuilder("[]");
		for (int i = 33; i < 127; ++i) {
			if ((UriProfile.defaultCharTypeMap[i] & 0x10080) == 0x0 && i != 37) {
				sb.append((char) i);
			}
		}
		for (int i = 161; i < 255; ++i) {
			sb.append((char) i);
		}
		RFC3986_ABS_16BIT_LAX = new UriProfile();
		UriProfile.RFC3986_ABS_16BIT_LAX.bAllowRelativeUris = false;
		UriProfile.RFC3986_ABS_16BIT_LAX.bAllow16bitPercentEncoding = true;
		UriProfile.RFC3986_ABS_16BIT_LAX.bAllowinvalidPercentEncoding = true;
		UriProfile.RFC3986_ABS_16BIT_LAX.charTypeAddAndOr(sb.toString(), 0, 114688);
		UriProfile.RFC3986_ABS_16BIT_LAX.charTypeAddAndOr("#", 0, 65536);
	}
}