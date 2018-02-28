/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;

public class WarcFileNamingDefault implements WarcFileNaming {
	protected DateFormat dateFormat;
	protected String filePrefix;
	protected Date date;
	protected String dateStr;
	protected String hostname;
	protected String extension;

	public WarcFileNamingDefault(final String filePrefix, final Date date, final String hostname,
			final String extension) {
		this.dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		if (filePrefix != null) {
			this.filePrefix = filePrefix;
		} else {
			this.filePrefix = "JWAT";
		}
		if (date != null) {
			this.date = date;
		} else {
			this.date = new Date();
		}
		if (hostname != null) {
			this.hostname = hostname;
		} else {
			try {
				this.hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
			} catch (UnknownHostException e) {
				this.hostname = "unknown";
			}
		}
		if (extension != null) {
			this.extension = extension;
		} else {
			this.extension = ".warc";
		}
		this.dateStr = this.dateFormat.format(this.date);
	}

	public boolean supportMultipleFiles() {
		return true;
	}

	public String getFilename(int sequenceNr, final boolean bCompressed) {
		String filename = this.filePrefix + "-" + this.dateStr + "-" + String.format("%05d", sequenceNr++) + "-"
				+ this.hostname + this.extension;
		if (bCompressed) {
			filename += ".gz";
		}
		return filename;
	}
}