/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public class ANVLRecord {
	public static final String CRLF = "\r\n";
	protected List<String> list;
	protected Map<String, String> map;

	public ANVLRecord() {
		this.list = new LinkedList<String>();
		this.map = new HashMap<String, String>();
	}

	public void addLabelValue(final String name, final String value) {
		int c = -1;
		if (value == null) {
			throw new IllegalArgumentException("Invalid value!");
		}
		if (value.length() > 0) {
			c = value.charAt(0);
		}
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Invalid name!");
		}
		if (c != -1 && c != 32 && c != 9) {
			this.list.add(name + ": " + value);
		} else {
			this.list.add(name + ":" + value);
		}
		this.map.put(name, value);
	}

	public void addValue(final String value) {
		if (value == null || value.length() == 0) {
			throw new IllegalArgumentException("Invalid value!");
		}
		this.list.add(value);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final Iterator<String> iter = this.list.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public byte[] getBytes(final String charsetName) throws UnsupportedEncodingException {
		return this.toString().getBytes(charsetName);
	}

	public byte[] getUTF8Bytes() throws UnsupportedEncodingException {
		return this.getBytes("UTF-8");
	}

	public ANVLRecord parse() {
		return null;
	}
}