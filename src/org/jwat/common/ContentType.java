/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class ContentType {
	protected static final int S_START = 0;
	protected static final int S_CONTENTTYPE = 1;
	protected static final int S_MEDIATYPE = 2;
	protected static final int S_MEDIATYPE_WHITESPACE = 3;
	protected static final int S_SEMICOLON = 4;
	protected static final int S_PARAM_NAME = 5;
	protected static final int S_PARAM_EQ = 6;
	protected static final int S_PARAM_VALUE = 7;
	protected static final int S_PARAM_QUOTED_VALUE = 8;
	protected static final int S_PARAM_QUOTED_PAIR = 9;
	protected static final int S_PARAM_VALUE_WHITESPACE = 10;
	protected static final int CC_CONTROL = 1;
	protected static final int CC_SEPARATOR_WS = 2;
	public String contentType;
	public String mediaType;
	public Map<String, String> parameters;
	protected static final String separators = "()<>@,;:\\\"/[]?={} \t";
	protected static final byte[] charCharacteristicsTab;

	public static boolean isTokenCharacter(final int c) {
		return (c >= 0 && c < 256 && ContentType.charCharacteristicsTab[c] == 0) || c >= 256;
	}

	public static ContentType parseContentType(final String contentTypeStr) {
		if (contentTypeStr == null || contentTypeStr.length() == 0) {
			return null;
		}
		final ContentType ct = new ContentType();
		final StringBuffer nameSb = new StringBuffer();
		StringBuffer valueSb = null;
		int state = 0;
		int idx = 0;
		boolean bLoop = true;
		while (bLoop) {
			int c;
			if (idx < contentTypeStr.length()) {
				c = contentTypeStr.charAt(idx);
			} else {
				c = -1;
				bLoop = false;
			}
			switch (state) {
			case 0: {
				if (c == 32 || c == 9) {
					++idx;
					continue;
				}
				if (isTokenCharacter(c)) {
					nameSb.setLength(0);
					nameSb.append((char) c);
					++idx;
					state = 1;
					continue;
				}
				return null;
			}
			case 1: {
				if (isTokenCharacter(c)) {
					nameSb.append((char) c);
					++idx;
					continue;
				}
				if (c == 47) {
					ct.contentType = nameSb.toString().toLowerCase();
					++idx;
					nameSb.setLength(0);
					state = 2;
					continue;
				}
				return null;
			}
			case 2: {
				if (isTokenCharacter(c)) {
					nameSb.append((char) c);
					++idx;
					continue;
				}
				if (c == -1) {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString().toLowerCase();
					continue;
				} else if (c == 59) {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString().toLowerCase();
					++idx;
					valueSb = new StringBuffer();
					ct.parameters = new HashMap<String, String>();
					state = 4;
					continue;
				} else {
					if (c != 32 && c != 9) {
						return null;
					}
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString().toLowerCase();
					++idx;
					state = 3;
					continue;
				}
			}
			case 3: {
				if (c == 32 || c == 9) {
					++idx;
					continue;
				}
				if (c != -1) {
					return null;
				}
				continue;
			}
			case 4: {
				if (c == 32 || c == 9) {
					++idx;
					continue;
				}
				if (isTokenCharacter(c)) {
					nameSb.setLength(0);
					valueSb.setLength(0);
					nameSb.append((char) c);
					++idx;
					state = 5;
					continue;
				}
				if (c == -1) {
					continue;
				}
				return null;
			}
			case 5: {
				if (isTokenCharacter(c)) {
					nameSb.append((char) c);
					++idx;
					continue;
				}
				if (c == 61) {
					++idx;
					state = 6;
					continue;
				}
				return null;
			}
			case 6: {
				if (isTokenCharacter(c)) {
					valueSb.append((char) c);
					++idx;
					state = 7;
					continue;
				}
				if (c == 34) {
					++idx;
					state = 8;
					continue;
				}
				return null;
			}
			case 7: {
				if (isTokenCharacter(c)) {
					valueSb.append((char) c);
					++idx;
					continue;
				}
				if (c == -1) {
					ct.parameters.put(nameSb.toString().toLowerCase(), valueSb.toString());
					continue;
				}
				if (c == 59) {
					ct.parameters.put(nameSb.toString().toLowerCase(), valueSb.toString());
					++idx;
					state = 4;
					continue;
				}
				if (c == 32 || c == 9) {
					ct.parameters.put(nameSb.toString().toLowerCase(), valueSb.toString());
					++idx;
					state = 10;
					continue;
				}
				return null;
			}
			case 8: {
				if (c == 34) {
					ct.parameters.put(nameSb.toString().toLowerCase(), valueSb.toString());
					++idx;
					state = 10;
					continue;
				}
				if (c == 92) {
					++idx;
					state = 9;
					continue;
				}
				if (c != -1) {
					valueSb.append((char) c);
					++idx;
					continue;
				}
				return null;
			}
			case 9: {
				if (c != -1) {
					valueSb.append((char) c);
					++idx;
					state = 8;
					continue;
				}
				return null;
			}
			case 10: {
				if (c == 32 || c == 9) {
					++idx;
					continue;
				}
				if (c == 59) {
					++idx;
					state = 4;
					continue;
				}
				if (c != -1) {
					return null;
				}
				continue;
			}
			}
		}
		return ct;
	}

	public String getParameter(final String name) {
		if (name == null || name.length() == 0 || this.parameters == null) {
			return null;
		}
		return this.parameters.get(name.toLowerCase());
	}

	public void setParameter(final String name, final String value) {
		if (name != null && name.length() > 0 && value != null) {
			if (this.parameters == null) {
				this.parameters = new HashMap<String, String>();
			}
			this.parameters.put(name.toLowerCase(), value);
		}
	}

	public static boolean quote(final String str) {
		boolean quote = false;
		if (str != null && str.length() > 0) {
			for (int idx = 0; idx < str.length() && !quote; quote = true) {
				final char c = str.charAt(idx++);
				if (c < 'Ā' && (ContentType.charCharacteristicsTab[c] & 0x2) != 0x0) {
				}
			}
		}
		return quote;
	}

	public String toStringShort() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.contentType);
		sb.append('/');
		sb.append(this.mediaType);
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.contentType);
		sb.append('/');
		sb.append(this.mediaType);
		if (this.parameters != null) {
			final Iterator<Map.Entry<String, String>> iter = this.parameters.entrySet().iterator();
			while (iter.hasNext()) {
				sb.append("; ");
				final Map.Entry<String, String> entry = iter.next();
				sb.append(entry.getKey());
				sb.append('=');
				if (quote(entry.getValue())) {
					sb.append('\"');
					sb.append(entry.getValue());
					sb.append('\"');
				} else {
					sb.append(entry.getValue());
				}
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof ContentType)) {
			return false;
		}
		final ContentType contentTypeObj = (ContentType) obj;
		if (this.contentType != null) {
			if (!this.contentType.equals(contentTypeObj.contentType)) {
				return false;
			}
		} else if (contentTypeObj.contentType != null) {
			return false;
		}
		if (this.mediaType != null) {
			if (!this.mediaType.equals(contentTypeObj.mediaType)) {
				return false;
			}
		} else if (contentTypeObj.mediaType != null) {
			return false;
		}
		if (this.parameters != null) {
			if (contentTypeObj.parameters == null) {
				return false;
			}
			if (this.parameters.size() != contentTypeObj.parameters.size()) {
				return false;
			}
			if (this.parameters.size() > 0) {
				for (final Map.Entry<String, String> entry : this.parameters.entrySet()) {
					if (!contentTypeObj.parameters.containsKey(entry.getKey())) {
						return false;
					}
					final String value = entry.getValue();
					if (value != null) {
						if (!value.equals(contentTypeObj.parameters.get(entry.getKey()))) {
							return false;
						}
						continue;
					} else {
						if (contentTypeObj.parameters.get(entry.getKey()) != null) {
							return false;
						}
						continue;
					}
				}
			}
		} else if (contentTypeObj.parameters != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.contentType != null) {
			hashCode ^= this.contentType.hashCode();
		}
		if (this.mediaType != null) {
			hashCode ^= this.mediaType.hashCode();
		}
		if (this.parameters != null) {
			hashCode ^= 0x7A63;
			for (final Map.Entry<String, String> entry : this.parameters.entrySet()) {
				hashCode ^= entry.getKey().hashCode();
				final String value = entry.getValue();
				if (value != null) {
					hashCode ^= value.hashCode();
				}
			}
		}
		return hashCode;
	}

	static {
		charCharacteristicsTab = new byte[256];
		for (int i = 0; i < "()<>@,;:\\\"/[]?={} \t".length(); ++i) {
			ContentType.charCharacteristicsTab["()<>@,;:\\\"/[]?={} \t".charAt(i)] = 2;
		}
		for (int i = 0; i < 32; ++i) {
			if (i != 9) {
				ContentType.charCharacteristicsTab[i] = 1;
			}
		}
	}
}