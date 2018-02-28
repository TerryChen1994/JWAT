/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.util.UUID;
import java.io.OutputStream;
import java.io.IOException;
import org.jwat.common.Uri;
import org.jwat.common.RandomAccessFileOutputStream;
import java.io.RandomAccessFile;
import java.io.File;

public class WarcFileWriter {
	public static final String ACTIVE_SUFFIX = ".open";
	protected WarcFileWriterConfig warcFileConfig;
	protected WarcFileNaming warcFileNaming;
	protected int sequenceNr;
	protected File writerFile;
	protected RandomAccessFile writer_raf;
	protected RandomAccessFileOutputStream writer_rafout;
	public WarcWriter writer;
	public Uri warcinfoRecordId;

	protected WarcFileWriter() {
		this.sequenceNr = -1;
	}

	public static WarcFileWriter getWarcWriterInstance(final WarcFileNaming warcFileNaming,
			final WarcFileWriterConfig warcFileConfig) {
		final WarcFileWriter wfw = new WarcFileWriter();
		wfw.warcFileNaming = warcFileNaming;
		wfw.warcFileConfig = warcFileConfig;
		return wfw;
	}

	public int getSequenceNr() {
		return this.sequenceNr;
	}

	public File getFile() {
		return this.writerFile;
	}

	public WarcWriter getWriter() {
		return this.writer;
	}

	public void open() throws IOException {
		if (this.writer == null) {
			++this.sequenceNr;
			final String finishedFilename = this.warcFileNaming.getFilename(this.sequenceNr,
					this.warcFileConfig.bCompression);
			final String activeFilename = finishedFilename + ".open";
			final File finishedFile = new File(this.warcFileConfig.targetDir, finishedFilename);
			this.writerFile = new File(this.warcFileConfig.targetDir, activeFilename);
			if (this.writerFile.exists()) {
				if (!this.warcFileConfig.bOverwrite) {
					throw new IOException("'" + this.writerFile + "' already exists, will not overwrite");
				}
				this.writerFile.delete();
			}
			if (finishedFile.exists()) {
				if (!this.warcFileConfig.bOverwrite) {
					throw new IOException("'" + finishedFile + "' already exists, will not overwrite");
				}
				finishedFile.delete();
			}
			(this.writer_raf = new RandomAccessFile(this.writerFile, "rw")).seek(0L);
			this.writer_raf.setLength(0L);
			this.writer_rafout = new RandomAccessFileOutputStream(this.writer_raf);
			this.writer = WarcWriterFactory.getWriter((OutputStream) this.writer_rafout, 8192,
					this.warcFileConfig.bCompression);
		}
	}

	public boolean nextWriter() throws Exception {
		boolean bNewWriter = false;
		if (this.writer_raf == null) {
			bNewWriter = true;
		} else if (this.warcFileNaming.supportMultipleFiles()
				&& this.writer_raf.length() > this.warcFileConfig.maxFileSize) {
			this.close();
			bNewWriter = true;
		}
		if (bNewWriter) {
			this.open();
			this.warcinfoRecordId = new Uri("urn:uuid:" + UUID.randomUUID());
		}
		return bNewWriter;
	}

	public void close() throws IOException {
		if (this.writer != null) {
			this.writer.close();
			this.writer = null;
		}
		if (this.writer_rafout != null) {
			this.writer_rafout.close();
			this.writer_rafout = null;
		}
		if (this.writer_raf != null) {
			this.writer_raf.close();
			this.writer_raf = null;
		}
		this.warcinfoRecordId = null;
		if (this.writerFile != null && this.writerFile.getName().endsWith(".open")) {
			final String finishedName = this.writerFile.getName().substring(0,
					this.writerFile.getName().length() - ".open".length());
			final File finishedFile = new File(this.writerFile.getParent(), finishedName);
			if (finishedFile.exists()) {
				throw new IOException("unable to rename '" + this.writerFile + "' to '" + finishedFile
						+ "' - destination file already exists");
			}
			final boolean success = this.writerFile.renameTo(finishedFile);
			if (!success) {
				throw new IOException(
						"unable to rename '" + this.writerFile + "' to '" + finishedFile + "' - unknown problem");
			}
		}
		this.writerFile = null;
	}
}