/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.security.NoSuchAlgorithmException;
import java.io.SequenceInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.io.Closeable;

public abstract class PayloadWithHeaderAbstract implements Closeable {
	protected boolean bClosed;
	protected ByteCountingPushBackInputStream in_pb;
	protected long totalLength;
	protected String digestAlgorithm;
	protected boolean bIsValid;
	protected MaxLengthRecordingInputStream in_flr;
	protected byte[] header;
	protected MessageDigest md;
	protected byte[] digest;
	protected DigestInputStream in_digest;
	protected boolean bNoSuchAlgorithmException;
	protected InputStream in_payload;
	protected ByteCountingPushBackInputStream in_pb_exposed;
	protected SequenceInputStream in_complete;
	public long payloadLength;
	public Diagnostics<Diagnosis> diagnostics;

	public PayloadWithHeaderAbstract() {
		this.payloadLength = 0L;
	}

	protected void initProcess() throws IOException {
		this.in_flr = new MaxLengthRecordingInputStream((InputStream) this.in_pb, (long) this.in_pb.getPushbackSize());
		this.bIsValid = this.readHeader(this.in_flr, this.totalLength);
		if (this.bIsValid) {
			if (this.digestAlgorithm != null) {
				try {
					this.md = MessageDigest.getInstance(this.digestAlgorithm);
				} catch (NoSuchAlgorithmException e) {
					this.bNoSuchAlgorithmException = true;
				}
			}
			if (this.md != null) {
				this.in_digest = (DigestInputStream) new DigestInputStreamNoSkip((InputStream) this.in_pb, this.md);
				this.in_payload = this.in_digest;
			} else {
				this.in_payload = (InputStream) this.in_pb;
			}
			this.in_payload = new FilterInputStream(this.in_payload) {
				@Override
				public void close() throws IOException {
				}
			};
			this.header = this.in_flr.getRecording();
			this.in_pb_exposed = new ByteCountingPushBackInputStream(this.in_payload, this.in_pb.getPushbackSize());
			this.in_complete = new SequenceInputStream(new ByteArrayInputStream(this.header), this.in_payload);
			this.in_flr = null;
		} else {
			this.header = this.in_flr.getRecording();
			this.in_pb.unread(this.header);
			this.in_flr = null;
			this.bClosed = true;
		}
	}

	protected abstract boolean readHeader(final MaxLengthRecordingInputStream p0, final long p1) throws IOException;

	public List<HeaderLine> getHeaderList() {
		throw new UnsupportedOperationException();
	}

	public HeaderLine getHeader(final String field) {
		throw new UnsupportedOperationException();
	}

	public boolean isValid() {
		return this.bIsValid;
	}

	public byte[] getHeader() {
		return this.header;
	}

	public byte[] getDigest() {
		if (this.digest == null && this.md != null) {
			this.digest = this.md.digest();
		}
		return this.digest;
	}

	public long getPayloadLength() {
		if (!this.bIsValid) {
			throw new IllegalStateException("HttpHeader not valid");
		}
		return this.payloadLength;
	}

	public long getTotalLength() {
		if (!this.bIsValid) {
			throw new IllegalStateException("HttpHeader not valid");
		}
		return this.totalLength;
	}

	public long getUnavailable() throws IOException {
		if (!this.bIsValid) {
			throw new IllegalStateException("HttpHeader not valid");
		}
		return this.totalLength - this.in_pb.getConsumed();
	}

	public InputStream getInputStreamComplete() {
		if (!this.bIsValid) {
			throw new IllegalStateException("HttpHeader not valid");
		}
		return this.in_complete;
	}

	public ByteCountingPushBackInputStream getPayloadInputStream() {
		if (!this.bIsValid) {
			throw new IllegalStateException("HttpHeader not valid");
		}
		return this.in_pb_exposed;
	}

	public long getRemaining() throws IOException {
		if (!this.bIsValid) {
			throw new IllegalStateException("HttpHeader not valid");
		}
		return this.totalLength - this.in_pb.getConsumed();
	}

	public boolean isClosed() {
		return this.bClosed;
	}

	@Override
	public void close() throws IOException {
		if (!this.bClosed) {
			if (this.md != null) {
				while (this.in_digest.skip(this.totalLength) > 0L) {
				}
			}
			if (this.in_pb != null) {
				this.in_pb.close();
				this.in_pb = null;
			}
			this.bClosed = true;
		}
	}
}