/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLineReader;
import org.jwat.common.UriProfile;

public abstract class WarcReader implements Closeable {
	protected UriProfile warcTargetUriProfile;
	protected UriProfile uriProfile;
	protected String blockDigestAlgorithm;
	protected String blockDigestEncoding;
	protected boolean bPayloadDigest;
	protected String payloadDigestAlgorithm;
	protected String payloadDigestEncoding;
	protected boolean bBlockDigest;
	protected int recordHeaderMaxSize;
	protected int payloadHeaderMaxSize;
	protected HeaderLineReader lineReader;
	protected HeaderLineReader headerLineReader;
	protected WarcFieldParsers fieldParsers;
	public final Diagnostics<Diagnosis> diagnostics;
	protected boolean bIsCompliant;
	protected long consumed;
	protected int records;
	protected int errors;
	protected int warnings;
	protected WarcRecord currentRecord;
	protected Exception iteratorExceptionThrown;

	public WarcReader() {
		this.blockDigestEncoding = "base32";

		this.bPayloadDigest = false;

		this.payloadDigestEncoding = "base32";

		this.bBlockDigest = false;

		this.diagnostics = new Diagnostics();

		this.bIsCompliant = true;

		this.consumed = 0L;

		this.records = 0;

		this.errors = 0;

		this.warnings = 0;
	}

	protected void init() {
		this.warcTargetUriProfile = UriProfile.RFC3986;
		this.uriProfile = UriProfile.RFC3986;
		this.recordHeaderMaxSize = 8192;
		this.payloadHeaderMaxSize = 32768;
		this.lineReader = HeaderLineReader.getReader();
		this.lineReader.bNameValue = false;
		this.lineReader.encoding = 1;
		this.headerLineReader = HeaderLineReader.getReader();
		this.headerLineReader.bNameValue = true;
		this.headerLineReader.encoding = 3;
		this.headerLineReader.bLWS = true;
		this.headerLineReader.bQuotedText = true;
		this.headerLineReader.bEncodedWords = true;
		this.fieldParsers = new WarcFieldParsers();
	}

	public void reset() {
		this.diagnostics.reset();
		this.bIsCompliant = true;
		this.consumed = 0L;
		this.records = 0;
		this.errors = 0;
		this.warnings = 0;
		this.currentRecord = null;
	}

	public boolean isCompliant() {
		return this.bIsCompliant;
	}

	public abstract boolean isCompressed();

	public void setWarcTargetUriProfile(UriProfile uriProfile) {
		if (uriProfile == null) {
			uriProfile = UriProfile.RFC3986;
		}
		this.warcTargetUriProfile = uriProfile;
	}

	public UriProfile getWarcTargetUriProfile() {
		return this.warcTargetUriProfile;
	}

	public void setUriProfile(UriProfile uriProfile) {
		if (uriProfile == null) {
			uriProfile = UriProfile.RFC3986;
		}
		this.uriProfile = uriProfile;
	}

	public UriProfile getUriProfile() {
		return this.uriProfile;
	}

	public boolean getBlockDigestEnabled() {
		return this.bBlockDigest;
	}

	public void setBlockDigestEnabled(boolean enabled) {
		this.bBlockDigest = enabled;
	}

	public boolean getPayloadDigestEnabled() {
		return this.bPayloadDigest;
	}

	public void setPayloadDigestEnabled(boolean enabled) {
		this.bPayloadDigest = enabled;
	}

	public String getBlockDigestAlgorithm() {
		return this.blockDigestAlgorithm;
	}

	public boolean setBlockDigestAlgorithm(String digestAlgorithm) {
		if ((digestAlgorithm == null) || (digestAlgorithm.length() == 0)) {
			this.blockDigestAlgorithm = null;
			return true;
		}
		if (WarcDigest.digestAlgorithmLength(digestAlgorithm) > 0) {
			this.blockDigestAlgorithm = digestAlgorithm;
			return true;
		}
		return false;
	}

	public String getPayloadDigestAlgorithm() {
		return this.payloadDigestAlgorithm;
	}

	public boolean setPayloadDigestAlgorithm(String digestAlgorithm) {
		if ((digestAlgorithm == null) || (digestAlgorithm.length() == 0)) {
			this.payloadDigestAlgorithm = null;
			return true;
		}
		if (WarcDigest.digestAlgorithmLength(digestAlgorithm) > 0) {
			this.payloadDigestAlgorithm = digestAlgorithm;
			return true;
		}
		return false;
	}

	public String getBlockDigestEncoding() {
		return this.blockDigestEncoding;
	}

	public void setBlockDigestEncoding(String encodingScheme) {
		if ((encodingScheme != null) && (encodingScheme.length() > 0))
			this.blockDigestEncoding = encodingScheme.toLowerCase();
		else
			this.blockDigestEncoding = null;
	}

	public String getPayloadDigestEncoding() {
		return this.payloadDigestEncoding;
	}

	public void setPayloadDigestEncoding(String encodingScheme) {
		if ((encodingScheme != null) && (encodingScheme.length() > 0))
			this.payloadDigestEncoding = encodingScheme.toLowerCase();
		else
			this.payloadDigestEncoding = null;
	}

	public int getRecordHeaderMaxSize() {
		return this.recordHeaderMaxSize;
	}

	public void setRecordHeaderMaxSize(int size) {
		this.recordHeaderMaxSize = size;
	}

	public int getPayloadHeaderMaxSize() {
		return this.payloadHeaderMaxSize;
	}

	public void setPayloadHeaderMaxSize(int size) {
		this.payloadHeaderMaxSize = size;
	}

	public abstract void close();

	protected abstract void recordClosed();

	public abstract long getStartOffset();

	public abstract long getOffset();

	public abstract long getConsumed();

	public abstract WarcRecord getNextRecord() throws IOException;

	public abstract WarcRecord getNextRecordFrom(InputStream paramInputStream, long paramLong) throws IOException;

	public abstract WarcRecord getNextRecordFrom(InputStream paramInputStream, long paramLong, int paramInt)
			throws IOException;

	public Exception getIteratorExceptionThrown() {
		return this.iteratorExceptionThrown;
	}

	public Iterator<WarcRecord> iterator() {
		return new Iterator() {
			private WarcRecord next;
			private WarcRecord current;

			public boolean hasNext() {
				if (this.next == null) {
					WarcReader.this.iteratorExceptionThrown = null;
					try {
						this.next = WarcReader.this.getNextRecord();
					} catch (IOException e) {
						WarcReader.this.iteratorExceptionThrown = e;
					}
				}
				return (this.next != null);
			}

			public WarcRecord next() {
				if (this.next == null) {
					WarcReader.this.iteratorExceptionThrown = null;
					try {
						this.next = WarcReader.this.getNextRecord();
					} catch (IOException e) {
						WarcReader.this.iteratorExceptionThrown = e;
					}
				}
				if (this.next == null) {
					throw new NoSuchElementException();
				}
				this.current = this.next;
				this.next = null;
				return this.current;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}