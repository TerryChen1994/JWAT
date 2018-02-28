/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class HeaderLineReader {
	protected static final int S_LINE = 0;
	protected static final int S_NAME = 1;
	protected static final int S_VALUE = 2;
	protected static final int S_LWS = 3;
	protected static final int S_QUOTED_TEXT = 4;
	protected static final int S_QUOTED_PAIR = 5;
	protected static final int S_QUOTED_LWS = 6;
	protected static final int S_ENCODED_WORD_EQ = 7;
	protected static final int CC_CONTROL = 1;
	protected static final int CC_SEPARATOR_WS = 2;
	public static final String separatorsWs = "()<>@,;:\\\"/[]?={} \t";
	public static final byte[] charCharacteristicsTab = new byte[256];
	public static final int ENC_RAW = 0;
	public static final int ENC_US_ASCII = 1;
	public static final int ENC_ISO8859_1 = 2;
	public static final int ENC_UTF8 = 3;
	protected final UTF8 utf8 = new UTF8();
	public static final int EOL_LF = 0;
	public static final int EOL_CRLF = 1;
	public boolean bNameValue;
	public int encoding = 0;

	public int eol = 1;
	public boolean bLWS;
	public boolean bQuotedText;
	public boolean bEncodedWords;
	protected final StringBuffer lineSb = new StringBuffer();

	protected final StringBuffer nvSb = new StringBuffer();

	protected ByteArrayOutputStreamWithUnread bytesOut = new ByteArrayOutputStreamWithUnread();
	public static final int E_BIT_EOF = 1;
	public static final int E_BIT_MISPLACED_CR = 2;
	public static final int E_BIT_MISSING_CR = 4;
	public static final int E_BIT_UNEXPECTED_CR = 8;
	public static final int E_BIT_INVALID_UTF8_ENCODING = 16;
	public static final int E_BIT_INVALID_US_ASCII_CHAR = 32;
	public static final int E_BIT_INVALID_CONTROL_CHAR = 64;
	public static final int E_BIT_INVALID_SEPARATOR_CHAR = 128;
	public static final int E_BIT_MISSING_QUOTE = 256;
	public static final int E_BIT_MISSING_QUOTED_PAIR_CHAR = 512;
	public static final int E_BIT_INVALID_QUOTED_PAIR_CHAR = 1024;
	protected boolean bCr = false;
	protected boolean bValidChar;
	public boolean bEof;
	public int bfErrors;

	public static HeaderLineReader getReader() {
		return new HeaderLineReader();
	}

	public static HeaderLineReader getLineReader() {
		HeaderLineReader hlr = new HeaderLineReader();
		hlr.bNameValue = false;
		hlr.encoding = 1;
		return hlr;
	}

	public static HeaderLineReader getHeaderLineReader() {
		HeaderLineReader hlr = new HeaderLineReader();
		hlr.bNameValue = true;
		hlr.encoding = 2;

		hlr.bLWS = true;
		hlr.bQuotedText = true;
		hlr.bEncodedWords = true;
		return hlr;
	}

	public HeaderLine readLine(PushbackInputStream in) throws IOException {
		HeaderLine headerLine = new HeaderLine();
		int state;
		if (!(this.bNameValue))
			state = 0;
		else {
			state = 1;
		}
		this.lineSb.setLength(0);
		this.nvSb.setLength(0);
		this.bytesOut = new ByteArrayOutputStreamWithUnread();
		this.bfErrors = 0;

		this.bCr = false;
		boolean bLoop = true;
		while (bLoop) {
			int c = in.read();
			if (c != -1) {
				this.bytesOut.write(c);
			}
			switch (state) {
			case 0:
				switch (c) {
				case -1:
					this.bfErrors |= 1;
					headerLine.type = 1;
					headerLine.line = this.lineSb.toString();
					this.lineSb.setLength(0);
					bLoop = false;
					break;
				case 13:
					this.bCr = true;
					break;
				case 10:
					headerLine.type = 1;
					headerLine.line = this.lineSb.toString();
					this.lineSb.setLength(0);

					check_eol();
					bLoop = false;
					break;
				default:
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}

					c = decode(c, in);
					if (c == -1) {
						this.bfErrors |= 1;
						headerLine.type = 1;
						headerLine.line = this.lineSb.toString();
						this.lineSb.setLength(0);
						bLoop = false;
					} else {
						if ((this.bValidChar) && (this.encoding != 0) && (c < 256)
								&& ((charCharacteristicsTab[c] & 0x1) == 1)) {
							this.bValidChar = false;

							this.bfErrors |= 64;
						}

						if (this.bValidChar)
							this.lineSb.append((char) c);
					}
				}
				break;
			case 1:
				switch (c) {
				case -1:
					this.bfErrors |= 1;
					headerLine.type = 1;
					headerLine.line = this.lineSb.toString();
					this.lineSb.setLength(0);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				case 13:
					this.bCr = true;
					break;
				case 10:
					headerLine.type = 1;
					headerLine.line = this.lineSb.toString();
					this.lineSb.setLength(0);
					this.nvSb.setLength(0);

					check_eol();
					bLoop = false;
					break;
				case 58:
					headerLine.type = 2;
					headerLine.name = this.nvSb.toString();
					this.lineSb.setLength(0);
					this.nvSb.setLength(0);
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}
					state = 2;
					break;
				default:
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}

					c = decode(c, in);
					if (c == -1) {
						this.bfErrors |= 1;
						headerLine.type = 1;
						headerLine.line = this.lineSb.toString();
						this.lineSb.setLength(0);
						this.nvSb.setLength(0);
						bLoop = false;
					} else {
						if ((this.bValidChar) && (this.encoding != 0) && (c < 256)
								&& ((charCharacteristicsTab[c] & 0x1) == 1)) {
							this.bValidChar = false;

							this.bfErrors |= 64;
						}

						if (this.bValidChar) {
							this.lineSb.append((char) c);
							if ((c < 256) && ((charCharacteristicsTab[c] & 0x2) == 2)) {
								this.bValidChar = false;

								this.bfErrors |= 128;
							}
						}
						if (this.bValidChar)
							this.nvSb.append((char) c);
					}
				}
				break;
			case 2:
				switch (c) {
				case -1:
					this.bfErrors |= 1;
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				case 13:
					this.bCr = true;
					break;
				case 10:
					check_eol();
					if (this.bLWS) {
						state = 3;
					} else {
						headerLine.value = trim(this.nvSb);
						this.nvSb.setLength(0);
						bLoop = false;
					}
					break;
				default:
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}

					c = decode(c, in);
					if (c == -1) {
						this.bfErrors |= 1;
						headerLine.value = trim(this.nvSb);
						this.nvSb.setLength(0);
						bLoop = false;
					} else {
						if ((this.bValidChar) && (this.encoding != 0) && (c < 256)
								&& ((charCharacteristicsTab[c] & 0x1) == 1)) {
							this.bValidChar = false;

							this.bfErrors |= 64;
						}

						if (this.bValidChar)
							switch (c) {
							case 34:
								this.nvSb.append((char) c);
								if (this.bQuotedText)
									state = 4;
								break;
							case 61:
								if (this.bEncodedWords)
									state = 7;
								else {
									this.nvSb.append((char) c);
								}
								break;
							default:
								this.nvSb.append((char) c);
							}
					}
				}
				break;
			case 3:
				switch (c) {
				case -1:
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				case 9:
				case 32:
					this.nvSb.append(" ");
					state = 2;
					break;
				default:
					in.unread(c);
					this.bytesOut.unread(c);
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
				}
				break;
			case 4:
				switch (c) {
				case -1:
					this.bfErrors |= 257;
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				case 34:
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}
					this.nvSb.append((char) c);
					state = 2;
					break;
				case 92:
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}
					state = 5;
					break;
				case 13:
					this.bCr = true;
					break;
				case 10:
					check_eol();
					if (this.bLWS) {
						state = 6;
					} else {
						headerLine.value = trim(this.nvSb);
						this.nvSb.setLength(0);
						bLoop = false;
					}
					break;
				default:
					if (this.bCr) {
						this.bfErrors |= 2;
						this.bCr = false;
					}

					c = decode(c, in);
					if (c == -1) {
						this.bfErrors |= 257;
						headerLine.value = trim(this.nvSb);
						this.nvSb.setLength(0);
						bLoop = false;
					} else {
						if ((this.bValidChar) && (this.encoding != 0) && (c < 256)
								&& ((charCharacteristicsTab[c] & 0x1) == 1)) {
							this.bValidChar = false;

							this.bfErrors |= 64;
						}

						if (this.bValidChar)
							this.nvSb.append((char) c);
					}
				}
				break;
			case 5:
				switch (c) {
				case -1:
					this.nvSb.append('\\');

					this.bfErrors |= 769;
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				default:
					c = decode(c, in);
					if (c == -1) {
						this.bfErrors |= 769;
						headerLine.value = trim(this.nvSb);
						this.nvSb.setLength(0);
						bLoop = false;
					} else {
						this.nvSb.append('\\');
						this.nvSb.append((char) c);
						if (!(this.bValidChar)) {
							this.bfErrors |= 1024;
						}
						state = 4;
					}
				}
				break;
			case 6:
				switch (c) {
				case -1:
					this.bfErrors |= 256;
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				case 9:
				case 32:
					this.nvSb.append(" ");
					state = 4;
					break;
				default:
					in.unread(c);
					this.bytesOut.unread(c);
					this.bfErrors |= 256;
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
				}
				break;
			case 7:
				switch (c) {
				case -1:
					this.nvSb.append('=');

					this.bfErrors |= 1;
					headerLine.value = trim(this.nvSb);
					this.nvSb.setLength(0);
					bLoop = false;
					break;
				case 63:
					in.unread(63);
					in.unread(61);
					this.bytesOut.unread(63);
					this.bytesOut.unread(61);
					EncodedWords ew = EncodedWords.parseEncodedWords(in, true);

					this.nvSb.append("=?");
					in.unread(ew.line, 2, ew.line.length - 2);
					this.bytesOut.write("=?".getBytes());
					state = 2;
					break;
				default:
					this.nvSb.append('=');
					in.unread(c);
					this.bytesOut.unread(c);
					state = 2;
				}
			}

		}

		headerLine.raw = this.bytesOut.toByteArray();
		headerLine.bfErrors = this.bfErrors;
		this.bEof = (headerLine.raw.length == 0);
		return headerLine;
	}

	protected int decode(int c, InputStream in) throws IOException {
		switch (this.encoding) {
		case 3:
			c = this.utf8.readUtf8(c, in);
			this.bytesOut.write(this.utf8.chars_read);
			this.bValidChar = this.utf8.bValidChar;
			if ((c == -1) || (this.bValidChar))
				break;
			this.bfErrors |= 16;
			break;
		case 1:
			this.bValidChar = (c <= 127);
			if (this.bValidChar)
				break;
			this.bfErrors |= 32;
			break;
		case 0:
		case 2:
		default:
			this.bValidChar = true;
		}

		label138: return c;
	}

	protected void check_eol() {
		switch (this.eol) {
		case 0:
			if (!(this.bCr)) {
				this.bfErrors |= 8;
			}
			break;
		case 1:
			if (!(this.bCr)) {
				this.bfErrors |= 4;
			}
		}

		this.bCr = false;
	}

	public static String trim(StringBuffer sb) {
		int sIdx = 0;
		int eIdx = sb.length();
		while ((sIdx < eIdx) && (sb.charAt(sIdx) == ' ')) {
			++sIdx;
		}
		while ((eIdx > sIdx) && (sb.charAt(eIdx - 1) == ' ')) {
			--eIdx;
		}
		return sb.substring(sIdx, eIdx);
	}

	public static void report_error(int bfErrors, Diagnostics<Diagnosis> diagnostics) {
		if (diagnostics == null) {
			throw new IllegalArgumentException("'diagnostics' argument is null");
		}
		if ((bfErrors & 0x1) != 0) {
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Unexpected EOF" }));
		}
		if ((bfErrors & 0x2) != 0) {
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Misplaced CR" }));
		}
		if ((bfErrors & 0x4) != 0) {
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Missing CR" }));
		}
		if ((bfErrors & 0x8) != 0) {
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Unexpected CR" }));
		}
		if ((bfErrors & 0x10) != 0) {
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line",
					new String[] { "Invalid UTF-8 encoded character" }));
		}
		if ((bfErrors & 0x20) != 0) {
			diagnostics.addError(
					new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Invalid US-ASCII character" }));
		}
		if ((bfErrors & 0x40) != 0) {
			diagnostics.addError(
					new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Invalid control character" }));
		}
		if ((bfErrors & 0x80) != 0) {
			diagnostics.addError(
					new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Invalid separator character" }));
		}
		if ((bfErrors & 0x100) != 0) {
			diagnostics.addError(
					new Diagnosis(DiagnosisType.ERROR, "header/line", new String[] { "Missing quote character" }));
		}
		if ((bfErrors & 0x200) != 0) {
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line",
					new String[] { "Missing quoted pair character" }));
		}
		if ((bfErrors & 0x400) != 0)
			diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line",
					new String[] { "Invalid quoted pair character" }));
	}

	static {
		for (int i = 0; i < 32; ++i)
			if (i != 9) {
				int tmp26_25 = i;
				byte[] tmp26_22 = charCharacteristicsTab;
				tmp26_22[tmp26_25] = (byte) (tmp26_22[tmp26_25] | 0x1);
			}
		byte[] tmp43_38 = charCharacteristicsTab;
		tmp43_38[127] = (byte) (tmp43_38[127] | 0x1);
		for (int i = 0; i < "()<>@,;:\\\"/[]?={} \t".length(); ++i) {
			char tmp69_66 = "()<>@,;:\\\"/[]?={} \t".charAt(i);
			byte[] tmp69_60 = charCharacteristicsTab;
			tmp69_60[tmp69_66] = (byte) (tmp69_60[tmp69_66] | 0x2);
		}
	}
}