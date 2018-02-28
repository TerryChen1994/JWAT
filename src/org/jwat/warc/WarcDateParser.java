/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

public final class WarcDateParser {
	private final DateFormat dateFormat;
	private static final ThreadLocal<WarcDateParser> DateParserTL;

	private WarcDateParser() {
		(this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).setLenient(false);
		this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private Date parseDate(final String dateStr) {
		Date date = null;
		try {
			if (dateStr != null && dateStr.length() == "yyyy-MM-dd'T'HH:mm:ss'Z'".length() - 4) {
				date = this.dateFormat.parse(dateStr.toUpperCase());
			}
		} catch (Exception ex) {
		}
		return date;
	}

	public static Date getDate(final String dateStr) {
		final Date date = WarcDateParser.DateParserTL.get().parseDate(dateStr);
		final boolean isValid = date != null && date.getTime() > 0L;
		return isValid ? date : null;
	}

	public static DateFormat getDateFormat() {
		return WarcDateParser.DateParserTL.get().dateFormat;
	}

	static {
		DateParserTL = new ThreadLocal<WarcDateParser>() {
			public WarcDateParser initialValue() {
				return new WarcDateParser();
			}
		};
	}
}