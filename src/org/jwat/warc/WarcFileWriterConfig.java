/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;

public class WarcFileWriterConfig {
	public static final long DEFAULT_MAX_FILE_SIZE = 1073741824L;
	protected File targetDir;
	protected boolean bCompression;
	protected Long maxFileSize;
	public boolean bOverwrite;
	protected LinkedHashMap<String, Map.Entry<String, String>> metadata;

	public WarcFileWriterConfig() {
		this.maxFileSize = 1073741824L;
		this.metadata = new LinkedHashMap<String, Map.Entry<String, String>>();
	}

	public WarcFileWriterConfig(final File targetDir, final boolean bCompression, final long maxFileSize,
			final boolean bOverwrite) {
		this.maxFileSize = 1073741824L;
		this.metadata = new LinkedHashMap<String, Map.Entry<String, String>>();
		this.targetDir = targetDir;
		this.bCompression = bCompression;
		this.maxFileSize = maxFileSize;
		this.bOverwrite = bOverwrite;
	}
}