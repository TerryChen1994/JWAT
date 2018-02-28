/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class HeaderLine {
	public static final int READLINE_INITIAL_SIZE = 128;
	public static final byte HLT_LINE = 1;
	public static final byte HLT_HEADERLINE = 2;
	public byte type;
	public String name;
	public String value;
	public String line;
	public byte[] raw;
	public int bfErrors;
	public List<HeaderLine> lines;

	public HeaderLine() {
		this.type = 0;
		this.lines = new LinkedList<HeaderLine>();
	}

	public static HeaderLine readLine(final InputStream in) throws IOException {
		final StringBuffer sb = new StringBuffer(128);
		final ByteArrayOutputStream out = new ByteArrayOutputStream(128);
		while (true) {
			final int b = in.read();
			if (b == -1) {
				return null;
			}
			out.write(b);
			if (b == 10) {
				final HeaderLine headerLine = new HeaderLine();
				headerLine.line = sb.toString();
				headerLine.raw = out.toByteArray();
				return headerLine;
			}
			if (b == 13) {
				continue;
			}
			sb.append((char) b);
		}
	}
}