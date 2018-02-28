/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.Charset;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EncodedWords {
	protected static final int S_START_EQ = 0;
	protected static final int S_START_QM = 1;
	protected static final int S_CHARSET = 2;
	protected static final int S_ENCODING = 3;
	protected static final int S_ENCODED_WORDS = 4;
	protected static final int S_END_EQ = 5;
	public static final int ENC_BASE64 = 1;
	public static final int ENC_QUOTEDPRINTABLE = 2;
	protected static final int CC_CONTROL = 1;
	protected static final int CC_SEPARATOR_WS = 2;
	protected static final String separators = "()<>@,;:\\\"/[]?={} \t";
	protected static final byte[] charCharacteristicsTab;
	public boolean bValidCharset;
	public String charsetStr;
	public int encoding;
	public String encodingStr;
	public String encoded_text;
	public boolean bConversionError;
	public String decoded_text;
	public byte[] line;
	public boolean bIsValid;

	public EncodedWords() {
		this.bIsValid = false;
	}

	public static EncodedWords parseEncodedWords(final InputStream in, final boolean bParseEqQm) throws IOException {
		final EncodedWords ew = new EncodedWords();
		final ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
		final StringBuffer sb = new StringBuffer();
		Charset charset = null;
		int state;
		if (bParseEqQm) {
			state = 0;
		} else {
			state = 2;
		}
		boolean bLoop = true;
		while (bLoop) {
			final int c = in.read();
			if (c != -1) {
				lineOut.write(c);
			}
			switch (state) {
			case 0: {
				if (c == 61) {
					state = 1;
					continue;
				}
				bLoop = false;
				continue;
			}
			case 1: {
				if (c == 63) {
					state = 2;
					continue;
				}
				bLoop = false;
				continue;
			}
			case 2: {
				switch (c) {
				case -1:
				case 10:
				case 13: {
					bLoop = false;
					continue;
				}
				case 63: {
					ew.charsetStr = sb.toString().toUpperCase();
					sb.setLength(0);
					if (ew.charsetStr.length() > 0) {
						try {
							charset = Charset.forName(ew.charsetStr);
							ew.bValidCharset = true;
						} catch (IllegalCharsetNameException ex) {
						} catch (UnsupportedCharsetException ex2) {
						}
						state = 3;
						continue;
					}
					bLoop = false;
					continue;
				}
				default: {
					if (EncodedWords.charCharacteristicsTab[c] == 0 && c < 127) {
						sb.append((char) c);
						continue;
					}
					bLoop = false;
					continue;
				}
				}
			}
			case 3: {
				switch (c) {
				case -1:
				case 10:
				case 13: {
					bLoop = false;
					continue;
				}
				case 63: {
					ew.encodingStr = sb.toString().toUpperCase();
					sb.setLength(0);
					if (ew.encodingStr.length() > 0) {
						if ("b".equalsIgnoreCase(ew.encodingStr)) {
							ew.encoding = 1;
						} else if ("q".equalsIgnoreCase(ew.encodingStr)) {
							ew.encoding = 2;
						}
						state = 4;
						continue;
					}
					bLoop = false;
					continue;
				}
				default: {
					if (EncodedWords.charCharacteristicsTab[c] == 0 && c < 127) {
						sb.append((char) c);
						continue;
					}
					bLoop = false;
					continue;
				}
				}
			}
			case 4: {
				switch (c) {
				case -1:
				case 10:
				case 13: {
					bLoop = false;
					continue;
				}
				case 63: {
					ew.encoded_text = sb.toString();
					sb.setLength(0);
					byte[] decoded = null;
					if (ew.encoding == 1) {
						decoded = Base64.decodeToArray(ew.encoded_text, true);
					} else if (ew.encoding == 2) {
						decoded = QuotedPrintable.decode(ew.encoded_text);
					}
					if (decoded != null) {
						final ByteBuffer bb = ByteBuffer.wrap(decoded);
						final CharBuffer cb = CharBuffer.allocate(bb.capacity());
						final CharsetDecoder decoder = charset.newDecoder();
						decoder.onMalformedInput(CodingErrorAction.REPORT);
						decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
						try {
							boolean bDecodeLoop = true;
							while (bDecodeLoop) {
								final CoderResult result = decoder.decode(bb, cb, true);
								sb.append(cb.array(), cb.arrayOffset(), cb.position());
								cb.clear();
								if (result == CoderResult.UNDERFLOW) {
									bDecodeLoop = false;
								} else {
									if (!result.isError()) {
										continue;
									}
									bb.position(Math.min(bb.position() + result.length(), bb.limit()));
									sb.append('?');
									ew.bConversionError = true;
								}
							}
						} catch (CoderMalfunctionError coderMalfunctionError) {
						}
						ew.decoded_text = sb.toString();
					}
					state = 5;
					continue;
				}
				default: {
					if (c > 32 && c < 127) {
						sb.append((char) c);
						continue;
					}
					bLoop = false;
					continue;
				}
				}
			}
			case 5: {
				if (c == -1) {
					bLoop = false;
					continue;
				}
				if (c == 61) {
					ew.bIsValid = true;
					bLoop = false;
					continue;
				}
				continue;
			}
			}
		}
		ew.line = lineOut.toByteArray();
		ew.bIsValid = ((ew.bIsValid & ew.bValidCharset & ew.encoding != 0) && ew.decoded_text != null);
		return ew;
	}

	static {
		charCharacteristicsTab = new byte[256];
		for (int i = 0; i < "()<>@,;:\\\"/[]?={} \t".length(); ++i) {
			EncodedWords.charCharacteristicsTab["()<>@,;:\\\"/[]?={} \t".charAt(i)] = 2;
		}
		for (int i = 0; i < 32; ++i) {
			EncodedWords.charCharacteristicsTab[i] = 1;
		}
	}
}