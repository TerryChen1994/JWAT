/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import org.jwat.common.DiagnosisType;
import java.io.IOException;
import java.io.OutputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;
import java.text.DateFormat;
import org.jwat.common.UriProfile;
import java.io.Closeable;

public abstract class WarcWriter implements Closeable {
	protected static final int S_INIT = 0;
	protected static final int S_HEADER_WRITTEN = 1;
	protected static final int S_PAYLOAD_WRITTEN = 2;
	protected static final int S_RECORD_CLOSED = 3;
	protected UriProfile warcTargetUriProfile;
	protected UriProfile uriProfile;
	protected DateFormat warcDateFormat;
	protected WarcFieldParsers fieldParsers;
	protected byte[] stream_copy_buffer;
	protected boolean bExceptionOnContentLengthMismatch;
	public final Diagnostics<Diagnosis> diagnostics;
	protected int state;
	protected OutputStream out;
	protected WarcHeader header;
	protected Long headerContentLength;
	protected long payloadWrittenTotal;

	public WarcWriter() {
		this.diagnostics = (Diagnostics<Diagnosis>) new Diagnostics();
		this.state = 0;
	}

	protected void init() {
		this.warcTargetUriProfile = UriProfile.RFC3986;
		this.uriProfile = UriProfile.RFC3986;
		this.warcDateFormat = WarcDateParser.getDateFormat();
		this.fieldParsers = new WarcFieldParsers();
		this.stream_copy_buffer = new byte[8192];
		this.bExceptionOnContentLengthMismatch = true;
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

	public boolean exceptionOnContentLengthMismatch() {
		return this.bExceptionOnContentLengthMismatch;
	}

	public void setExceptionOnContentLengthMismatch(final boolean enabled) {
		this.bExceptionOnContentLengthMismatch = enabled;
	}

	@Override
	public abstract void close() throws IOException;

	public abstract void closeRecord() throws IOException;

	protected void closeRecord_impl() throws IOException {
		Diagnosis diagnosis = null;
		this.out.write(WarcConstants.endMark);
		this.out.flush();
		if (this.headerContentLength == null) {
			diagnosis = new Diagnosis(DiagnosisType.ERROR_EXPECTED, "'Content-Length' header",
					new String[] { "Mandatory!" });
		} else if (this.headerContentLength != this.payloadWrittenTotal) {
			diagnosis = new Diagnosis(DiagnosisType.INVALID_EXPECTED, "'Content-Length' header",
					new String[] { Long.toString(this.payloadWrittenTotal), this.headerContentLength.toString() });
		}
		if (diagnosis != null) {
			if (this.header != null) {
				this.header.diagnostics.addError(diagnosis);
			} else {
				this.diagnostics.addError(diagnosis);
			}

			if (this.bExceptionOnContentLengthMismatch) {
				throw new IllegalStateException("Payload size does not match content-length!");
			}
		}
		this.header = null;
		this.headerContentLength = null;
	}

	public void writeRawHeader(final byte[] header_bytes, final Long contentLength) throws IOException {
		if (header_bytes == null) {
			throw new IllegalArgumentException("The 'header_bytes' parameter is null!");
		}
		if (contentLength != null && contentLength < 0L) {
			throw new IllegalArgumentException("The 'contentLength' parameter is negative!");
		}
		if (this.state == 1) {
			throw new IllegalStateException("Headers written back to back!");
		}
		if (this.state == 2) {
			this.closeRecord_impl();
		}
		this.out.write(header_bytes);
		this.state = 1;
		this.header = null;
		this.headerContentLength = contentLength;
		this.payloadWrittenTotal = 0L;
	}

	public abstract byte[] writeHeader(final WarcRecord p0) throws IOException;

	protected byte[] writeHeader_impl(final WarcRecord record) throws IOException {
		this.header = record.header;
		this.headerContentLength = this.header.contentLength;
		if (this.headerContentLength == null && this.header.contentLengthStr != null) {
			try {
				this.headerContentLength = Long.parseLong(this.header.contentLengthStr);
			} catch (NumberFormatException ex) {
			}
		}
		final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		final byte[] magicVersion = ("WARC/" + this.header.major + "." + this.header.minor + "\r\n").getBytes();
		outBuf.write(magicVersion);
		String warcTypeStr = null;
		if (this.header.warcTypeIdx != null && this.header.warcTypeIdx > 0
				&& this.header.warcTypeIdx < WarcConstants.RT_IDX_STRINGS.length) {
			warcTypeStr = WarcConstants.RT_IDX_STRINGS[this.header.warcTypeIdx];
		}
		if (warcTypeStr == null) {
			warcTypeStr = this.header.warcTypeStr;
		}
		if (warcTypeStr != null) {
			outBuf.write("WARC-Type".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcTypeStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcRecordIdStr = null;
		if (this.header.warcRecordIdUri != null) {
			warcRecordIdStr = this.header.warcRecordIdUri.toString();
		} else if (this.header.warcRecordIdStr != null) {
			warcRecordIdStr = this.header.warcRecordIdStr;
		}
		if (warcRecordIdStr != null) {
			outBuf.write("WARC-Record-ID".getBytes());
			outBuf.write(": <".getBytes());
			outBuf.write(warcRecordIdStr.getBytes());
			outBuf.write(">\r\n".getBytes());
		}
		String warcDateStr = null;
		if (this.header.warcDate != null) {
			warcDateStr = this.warcDateFormat.format(this.header.warcDate);
		} else if (this.header.warcDateStr != null) {
			warcDateStr = this.header.warcDateStr;
		}
		if (warcDateStr != null) {
			outBuf.write("WARC-Date".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcDateStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String contentLengthStr = null;
		if (this.header.contentLength != null) {
			contentLengthStr = this.header.contentLength.toString();
		} else if (this.header.contentLengthStr != null) {
			contentLengthStr = this.header.contentLengthStr;
		}
		if (contentLengthStr != null) {
			outBuf.write("Content-Length".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(contentLengthStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String contentTypeStr = null;
		if (this.header.contentType != null) {
			contentTypeStr = this.header.contentType.toString();
		} else if (this.header.contentTypeStr != null) {
			contentTypeStr = this.header.contentTypeStr;
		}
		if (contentTypeStr != null) {
			outBuf.write("Content-Type".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(contentTypeStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		if (this.header.warcConcurrentToList != null) {
			for (int i = 0; i < this.header.warcConcurrentToList.size(); ++i) {
				final WarcConcurrentTo warcConcurrentTo = this.header.warcConcurrentToList.get(i);
				String warcConcurrentToStr = null;
				if (warcConcurrentTo.warcConcurrentToUri != null) {
					warcConcurrentToStr = warcConcurrentTo.warcConcurrentToUri.toString();
				} else if (warcConcurrentTo.warcConcurrentToStr != null) {
					warcConcurrentToStr = warcConcurrentTo.warcConcurrentToStr;
				}
				if (warcConcurrentToStr != null) {
					outBuf.write("WARC-Concurrent-To".getBytes());
					outBuf.write(": <".getBytes());
					outBuf.write(warcConcurrentToStr.getBytes());
					outBuf.write(">\r\n".getBytes());
				}
			}
		}
		String warcBlockDigestStr = null;
		if (this.header.warcBlockDigest != null) {
			warcBlockDigestStr = this.header.warcBlockDigest.toString();
		} else if (this.header.warcBlockDigestStr != null) {
			warcBlockDigestStr = this.header.warcBlockDigestStr;
		}
		if (warcBlockDigestStr != null) {
			outBuf.write("WARC-Block-Digest".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcBlockDigestStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcPayloadDigestStr = null;
		if (this.header.warcPayloadDigest != null) {
			warcPayloadDigestStr = this.header.warcPayloadDigest.toString();
		} else if (this.header.warcPayloadDigestStr != null) {
			warcPayloadDigestStr = this.header.warcPayloadDigestStr;
		}
		if (warcPayloadDigestStr != null) {
			outBuf.write("WARC-Payload-Digest".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcPayloadDigestStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcIpAddress = null;
		if (this.header.warcInetAddress != null) {
			warcIpAddress = this.header.warcInetAddress.getHostAddress();
		} else if (this.header.warcIpAddress != null) {
			warcIpAddress = this.header.warcIpAddress;
		}
		if (warcIpAddress != null) {
			outBuf.write("WARC-IP-Address".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcIpAddress.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcRefersToUriStr = null;
		if (this.header.warcRefersToUri != null) {
			warcRefersToUriStr = this.header.warcRefersToUri.toString();
		} else if (this.header.warcRefersToStr != null) {
			warcRefersToUriStr = this.header.warcRefersToStr;
		}
		if (warcRefersToUriStr != null) {
			outBuf.write("WARC-Refers-To".getBytes());
			outBuf.write(": <".getBytes());
			outBuf.write(warcRefersToUriStr.getBytes());
			outBuf.write(">\r\n".getBytes());
		}
		String warcTargetUriStr = null;
		if (this.header.warcTargetUriUri != null) {
			warcTargetUriStr = this.header.warcTargetUriUri.toString();
		} else if (this.header.warcTargetUriStr != null) {
			warcTargetUriStr = this.header.warcTargetUriStr;
		}
		if (warcTargetUriStr != null) {
			outBuf.write("WARC-Target-URI".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcTargetUriStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcTruncatedStr = null;
		if (this.header.warcTruncatedIdx != null && this.header.warcTruncatedIdx > 0
				&& this.header.warcTruncatedIdx < WarcConstants.TT_IDX_STRINGS.length) {
			warcTruncatedStr = WarcConstants.TT_IDX_STRINGS[this.header.warcTruncatedIdx];
		}
		if (warcTruncatedStr == null) {
			warcTruncatedStr = this.header.warcTruncatedStr;
		}
		if (warcTruncatedStr != null) {
			outBuf.write("WARC-Truncated".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcTruncatedStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcWarcInfoIdStr = null;
		if (this.header.warcWarcinfoIdUri != null) {
			warcWarcInfoIdStr = this.header.warcWarcinfoIdUri.toString();
		} else if (this.header.warcWarcinfoIdStr != null) {
			warcWarcInfoIdStr = this.header.warcWarcinfoIdStr;
		}
		if (warcWarcInfoIdStr != null) {
			outBuf.write("WARC-Warcinfo-ID".getBytes());
			outBuf.write(": <".getBytes());
			outBuf.write(warcWarcInfoIdStr.getBytes());
			outBuf.write(">\r\n".getBytes());
		}
		if (this.header.warcFilename != null) {
			outBuf.write("WARC-Filename".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(this.header.warcFilename.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcProfileStr = null;
		if (this.header.warcProfileUri != null) {
			warcProfileStr = this.header.warcProfileUri.toString();
		} else if (this.header.warcProfileIdx != null && this.header.warcProfileIdx > 0
				&& this.header.warcProfileIdx < WarcConstants.P_IDX_STRINGS.length) {
			warcProfileStr = WarcConstants.P_IDX_STRINGS[this.header.warcProfileIdx];
		}
		if (warcProfileStr == null) {
			warcProfileStr = this.header.warcProfileStr;
		}
		if (warcProfileStr != null) {
			outBuf.write("WARC-Profile".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcProfileStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcIdentifiedPayloadTypeStr = null;
		if (this.header.warcIdentifiedPayloadType != null) {
			warcIdentifiedPayloadTypeStr = this.header.warcIdentifiedPayloadType.toString();
		} else if (this.header.warcIdentifiedPayloadTypeStr != null) {
			warcIdentifiedPayloadTypeStr = this.header.warcIdentifiedPayloadTypeStr;
		}
		if (warcIdentifiedPayloadTypeStr != null) {
			outBuf.write("WARC-Identified-Payload-Type".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcIdentifiedPayloadTypeStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcSegmentNumberStr = null;
		if (this.header.warcSegmentNumber != null) {
			warcSegmentNumberStr = this.header.warcSegmentNumber.toString();
		} else if (this.header.warcSegmentNumberStr != null) {
			warcSegmentNumberStr = this.header.warcSegmentNumberStr;
		}
		if (warcSegmentNumberStr != null) {
			outBuf.write("WARC-Segment-Number".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcSegmentNumberStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcSegmentOriginIdStr = null;
		if (this.header.warcSegmentOriginIdUrl != null) {
			warcSegmentOriginIdStr = this.header.warcSegmentOriginIdUrl.toString();
		} else if (this.header.warcSegmentOriginIdStr != null) {
			warcSegmentOriginIdStr = this.header.warcSegmentOriginIdStr;
		}
		if (warcSegmentOriginIdStr != null) {
			outBuf.write("WARC-Segment-Origin-ID".getBytes());
			outBuf.write(": <".getBytes());
			outBuf.write(warcSegmentOriginIdStr.getBytes());
			outBuf.write(">\r\n".getBytes());
		}
		String warcSegmentTotalLengthStr = null;
		if (this.header.warcSegmentTotalLength != null) {
			warcSegmentTotalLengthStr = this.header.warcSegmentTotalLength.toString();
		} else if (this.header.warcSegmentTotalLengthStr != null) {
			warcSegmentTotalLengthStr = this.header.warcSegmentTotalLengthStr;
		}
		if (warcSegmentTotalLengthStr != null) {
			outBuf.write("WARC-Segment-Total-Length".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcSegmentTotalLengthStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcRefersToTargetUriStr = null;
		if (this.header.warcRefersToTargetUriUri != null) {
			warcRefersToTargetUriStr = this.header.warcRefersToTargetUriUri.toString();
		} else if (this.header.warcRefersToTargetUriStr != null) {
			warcRefersToTargetUriStr = this.header.warcRefersToTargetUriStr;
		}
		if (warcRefersToTargetUriStr != null) {
			outBuf.write("WARC-Refers-To-Target-URI".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcRefersToTargetUriStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		String warcRefersToDateStr = null;
		if (this.header.warcRefersToDate != null) {
			warcRefersToDateStr = this.warcDateFormat.format(this.header.warcRefersToDate);
		} else if (this.header.warcRefersToDateStr != null) {
			warcRefersToDateStr = this.header.warcRefersToDateStr;
		}
		if (warcRefersToDateStr != null) {
			outBuf.write("WARC-Refers-To-Date".getBytes());
			outBuf.write(": ".getBytes());
			outBuf.write(warcRefersToDateStr.getBytes());
			outBuf.write("\r\n".getBytes());
		}
		outBuf.write("\r\n".getBytes());
		final byte[] headerBytes = outBuf.toByteArray();
		this.out.write(headerBytes);
		this.state = 1;
		this.payloadWrittenTotal = 0L;
		return headerBytes;
	}

	public long streamPayload(final InputStream in) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("The 'in' parameter is null!");
		}
		if (this.state != 1 && this.state != 2) {
			throw new IllegalStateException("Write a header before writing payload!");
		}
		long written = 0L;
		for (int read = 0; read != -1; read = in.read(this.stream_copy_buffer)) {
			this.out.write(this.stream_copy_buffer, 0, read);
			written += read;
		}
		this.state = 2;
		this.payloadWrittenTotal += written;
		return written;
	}

	public long writePayload(final byte[] b) throws IOException {
		if (this.state != 1 && this.state != 2) {
			throw new IllegalStateException("Write a header before writing payload!");
		}
		this.out.write(b);
		this.state = 2;
		this.payloadWrittenTotal += b.length;
		return b.length;
	}

	public long writePayload(final byte[] b, final int offset, final int len) throws IOException {
		if (this.state != 1 && this.state != 2) {
			throw new IllegalStateException("Write a header before writing payload!");
		}
		this.out.write(b, offset, len);
		this.state = 2;
		this.payloadWrittenTotal += len;
		return len;
	}
}