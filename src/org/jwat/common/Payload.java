/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.io.Closeable;

public class Payload implements Closeable {
	private static final int BUFFER_SIZE = 8192;
	protected boolean bClosed;
	protected long length;
	protected FixedLengthInputStream in_fl;
	protected MessageDigest md;
	protected byte[] digest;
	protected DigestInputStream in_digest;
	protected boolean bNoSuchAlgorithmException;
	protected BufferedInputStream in_buffered;
	protected ByteCountingPushBackInputStream in_pb_exposed;
	protected int pushback_size;
	protected PayloadWithHeaderAbstract payloadHeaderWrapped;
	protected PayloadOnClosedHandler onClosedHandler;

	public static Payload processPayload(final InputStream in, final long length, final int pushback_size,
			final String digestAlgorithm) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("The inputstream 'in' is null");
		}
		if (length < 0L) {
			throw new IllegalArgumentException("The 'length' is less than zero: " + length);
		}
		if (pushback_size <= 0) {
			throw new IllegalArgumentException("The 'pushback_size' is less than or equal to zero: " + pushback_size);
		}
		final Payload pl = new Payload();
		pl.length = length;
		pl.pushback_size = pushback_size;
		pl.in_fl = new FixedLengthInputStream(in, length);
		if (digestAlgorithm != null) {
			try {
				pl.md = MessageDigest.getInstance(digestAlgorithm);
			} catch (NoSuchAlgorithmException e) {
				pl.bNoSuchAlgorithmException = true;
			}
		}
		if (pl.md != null) {
			pl.in_digest = (DigestInputStream) new DigestInputStreamNoSkip((InputStream) pl.in_fl, pl.md);
			pl.in_buffered = new BufferedInputStream(pl.in_digest, 8192);
		} else {
			pl.in_buffered = new BufferedInputStream((InputStream) pl.in_fl, 8192);
		}
		pl.in_pb_exposed = new ByteCountingPushBackInputStream(pl.in_buffered, pushback_size) {
			public void close() throws IOException {
			}
		};
		return pl;
	}

	public void setOnClosedHandler(final PayloadOnClosedHandler onClosedHandler) {
		this.onClosedHandler = onClosedHandler;
	}

	public byte[] getDigest() {
		if (this.digest == null && this.md != null) {
			this.digest = this.md.digest();
		}
		return this.digest;
	}

	public long getTotalLength() {
		return this.length;
	}

	public long getUnavailable() throws IOException {
		return this.in_fl.available();
	}

	public int getPushbackSize() {
		return this.pushback_size;
	}

	public void setPayloadHeaderWrapped(final PayloadWithHeaderAbstract payloadHeaderWrapped) {
		this.payloadHeaderWrapped = payloadHeaderWrapped;
	}

	public PayloadWithHeaderAbstract getPayloadHeaderWrapped() {
		return this.payloadHeaderWrapped;
	}

	public InputStream getInputStreamComplete() {
		if (this.payloadHeaderWrapped != null) {
			return this.payloadHeaderWrapped.getInputStreamComplete();
		}
		return (InputStream) this.in_pb_exposed;
	}

	public ByteCountingPushBackInputStream getInputStream() {
		if (this.payloadHeaderWrapped != null) {
			return this.payloadHeaderWrapped.getPayloadInputStream();
		}
		return this.in_pb_exposed;
	}

	public long getRemaining() throws IOException {
		if (this.payloadHeaderWrapped != null) {
			return this.payloadHeaderWrapped.getPayloadInputStream().available();
		}
		return this.in_pb_exposed.available();
	}

	public boolean isClosed() {
		return this.bClosed;
	}

	@Override
	public void close() throws IOException {
		if (!this.bClosed) {
			if (this.payloadHeaderWrapped != null) {
				this.payloadHeaderWrapped.close();
			}
			if (this.md != null) {
				while (this.in_digest.skip(this.length) > 0L) {
				}
			}
			if (this.in_buffered != null) {
				this.in_buffered.close();
				this.in_buffered = null;
			}
			if (this.onClosedHandler != null) {
				this.onClosedHandler.payloadClosed();
				this.onClosedHandler = null;
			}
			this.bClosed = true;
		}
	}
}