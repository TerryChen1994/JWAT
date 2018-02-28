/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jwat.common.ByteCountingPushBackInputStream;

public class WarcReaderUncompressed extends WarcReader {
	public static final int PUSHBACK_BUFFER_SIZE = 32;
	protected ByteCountingPushBackInputStream in;
	protected long startOffset = 0L;

	public WarcReaderUncompressed() {
		init();
	}

	public WarcReaderUncompressed(ByteCountingPushBackInputStream in) {
		if (in == null) {
			throw new IllegalArgumentException("The inputstream 'in' is null");
		}

		this.in = in;
		init();
	}

	public boolean isCompressed() {
		return false;
	}

	public void close() {
		if (this.currentRecord != null) {
			try {
				this.currentRecord.close();
			} catch (IOException localIOException) {
			}
			this.currentRecord = null;
		}
		if (this.in != null) {
			this.consumed = this.in.getConsumed();
			try {
				this.in.close();
			} catch (IOException localIOException1) {
			}
			this.in = null;
		}
	}

	protected void recordClosed() {
		if (this.currentRecord != null)
			this.consumed += this.currentRecord.consumed;
		else
			throw new IllegalStateException("'currentRecord' is null, this should never happen!");
	}

	public long getStartOffset() {
		return this.startOffset;
	}

	public long getOffset() {
		if (this.in != null) {
			return this.in.getConsumed();
		}
		return this.consumed;
	}

	public long getConsumed() {
		if (this.in != null) {
			return this.in.getConsumed();
		}
		return this.consumed;
	}

	public WarcRecord getNextRecord() throws IOException {
		if (this.currentRecord != null) {
			this.currentRecord.close();
		}
		if (this.in == null) {
			throw new IllegalStateException(
					"This reader has been initialized with an incompatible constructor, 'in' is null");
		}

		this.currentRecord = WarcRecord.parseRecord(this.in, this);
		if (this.currentRecord != null) {
			this.startOffset = this.currentRecord.getStartOffset();
		}
		return this.currentRecord;
	}

	public WarcRecord getNextRecordFrom(InputStream rin, long offset) throws IOException {
		if (this.currentRecord != null) {
			this.currentRecord.close();
		}
		if (this.in != null) {
			throw new IllegalStateException(
					"This reader has been initialized with an incompatible constructor, 'in' is not null");
		}

		if (rin == null) {
			throw new IllegalArgumentException("The inputstream 'rin' is null");
		}

		if (offset < -1L) {
			throw new IllegalArgumentException("The 'offset' is less than -1: " + offset);
		}

		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(rin, 32);

		this.currentRecord = WarcRecord.parseRecord(pbin, this);
		if (this.currentRecord != null) {
			this.startOffset = offset;
			this.currentRecord.header.startOffset = offset;
		}
		return this.currentRecord;
	}

	public WarcRecord getNextRecordFrom(InputStream rin, long offset, int buffer_size) throws IOException {
		if (this.currentRecord != null) {
			this.currentRecord.close();
		}
		if (this.in != null) {
			throw new IllegalStateException(
					"This reader has been initialized with an incompatible constructor, 'in' is not null");
		}

		if (rin == null) {
			throw new IllegalArgumentException("The inputstream 'rin' is null");
		}

		if (offset < -1L) {
			throw new IllegalArgumentException("The 'offset' is less than -1: " + offset);
		}

		if (buffer_size <= 0) {
			throw new IllegalArgumentException("The 'buffer_size' is less than or equal to zero: " + buffer_size);
		}

		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(
				new BufferedInputStream(rin, buffer_size), 32);

		this.currentRecord = WarcRecord.parseRecord(pbin, this);
		if (this.currentRecord != null) {
			this.startOffset = offset;
			this.currentRecord.header.startOffset = offset;
		}
		return this.currentRecord;
	}
}