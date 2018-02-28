/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.File;

public class WarcFileNamingSingleFile implements WarcFileNaming {
	protected String filename;

	public WarcFileNamingSingleFile(final String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("filename argument null");
		}
		this.filename = filename;
	}

	public WarcFileNamingSingleFile(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("file argument null");
		}
		this.filename = file.getName();
	}

	public boolean supportMultipleFiles() {
		return false;
	}

	public String getFilename(final int sequenceNr, final boolean bCompressed) {
		return this.filename;
	}
}