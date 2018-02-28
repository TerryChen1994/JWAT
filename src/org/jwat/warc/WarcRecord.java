/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jwat.common.Base16;
import org.jwat.common.Base32;
import org.jwat.common.Base64;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLine;
import org.jwat.common.HttpHeader;
import org.jwat.common.NewlineParser;
import org.jwat.common.Payload;
import org.jwat.common.PayloadOnClosedHandler;

public class WarcRecord implements PayloadOnClosedHandler, Closeable {
	protected WarcReader reader;
	protected ByteCountingPushBackInputStream in;
	protected boolean bIsCompliant;
	protected long startOffset = -1L;
	protected long consumed;
	public final Diagnostics<Diagnosis> diagnostics = new Diagnostics();

	public NewlineParser nlp = new NewlineParser();

	public Boolean isValidBlockDigest = null;

	public Boolean isValidPayloadDigest = null;
	public int trailingNewlines;
	public WarcHeader header;
	protected boolean bPayloadClosed;
	protected boolean bClosed;
	protected Payload payload;
	protected HttpHeader httpHeader;
	public WarcDigest computedBlockDigest;
	public WarcDigest computedPayloadDigest;

	public static WarcRecord createRecord(WarcWriter writer) {
		WarcRecord record = new WarcRecord();
		record.header = WarcHeader.initHeader(writer, record.diagnostics);
		writer.fieldParsers.diagnostics = record.diagnostics;
		return record;
	}

	public static WarcRecord parseRecord(ByteCountingPushBackInputStream in, WarcReader reader) throws IOException {
		WarcRecord record = new WarcRecord();
		record.in = in;
		record.reader = reader;
		record.startOffset = in.getConsumed();

		record.header = WarcHeader.initHeader(reader, in.getConsumed(), record.diagnostics);
		WarcHeader header = record.header;

		reader.fieldParsers.diagnostics = record.diagnostics;
		if (header.parseHeader(in)) {
			reader.records += 1;

			if ((header.contentLength != null) && (header.contentLength.longValue() > 0L)) {
				String digestAlgorithm = null;
				if (reader.bBlockDigest) {
					if ((header.warcBlockDigest != null) && (header.warcBlockDigest.algorithm != null)) {
						digestAlgorithm = header.warcBlockDigest.algorithm;
					} else {
						digestAlgorithm = reader.blockDigestAlgorithm;
					}
				}
				record.payload = Payload.processPayload(in, header.contentLength.longValue(),
						reader.payloadHeaderMaxSize, digestAlgorithm);

				record.payload.setOnClosedHandler(record);

				if ((header.contentType != null) && (header.contentType.contentType.equals("application"))
						&& (header.contentType.mediaType.equals("http"))) {
					String value = header.contentType.getParameter("msgtype");

					int httpHeaderType = 0;
					if ("response".equalsIgnoreCase(value))
						httpHeaderType = 1;
					else if ("request".equalsIgnoreCase(value)) {
						httpHeaderType = 2;
					}
					if (httpHeaderType != 0) {
						digestAlgorithm = null;
						if (reader.bPayloadDigest) {
							if ((header.warcPayloadDigest != null) && (header.warcPayloadDigest.algorithm != null)) {
								digestAlgorithm = header.warcPayloadDigest.algorithm;
							} else {
								digestAlgorithm = reader.payloadDigestAlgorithm;
							}
						}

						record.httpHeader = HttpHeader.processPayload(httpHeaderType, record.payload.getInputStream(),
								header.contentLength.longValue(), digestAlgorithm);

						if (record.httpHeader != null) {
							if (record.httpHeader.isValid())
								record.payload.setPayloadHeaderWrapped(record.httpHeader);
							else {
								record.diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "http header",
										new String[] { "Unable to parse http header!" }));
							}

						}

					}
					
				}

			}

			if ((record.diagnostics.hasErrors()) || (record.diagnostics.hasWarnings()))
				record.bIsCompliant = false;
			else {
				record.bIsCompliant = true;
			}
			reader.bIsCompliant &= record.bIsCompliant;
		} else {
			reader.diagnostics.addAll(record.diagnostics);
			if ((record.diagnostics.hasErrors()) || (record.diagnostics.hasWarnings())) {
				reader.errors += record.diagnostics.getErrors().size();
				reader.warnings += record.diagnostics.getWarnings().size();
				reader.bIsCompliant = false;
			}

			if (reader.records == 0) {
				reader.diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "WARC file",
						new String[] { "One or more records" }));
				reader.errors += 1;
				reader.bIsCompliant = false;
			}

			record = null;
		}
		return record;
	}

	public void payloadClosed() throws IOException {
		if (!(this.bPayloadClosed)) {
			if (this.payload != null) {
				if (this.payload.getUnavailable() > 0L) {
					addErrorDiagnosis(DiagnosisType.INVALID_DATA, "Payload length mismatch",
							new String[] { "Payload truncated" });
				}

				byte[] digest = this.payload.getDigest();

				if (digest != null) {
					this.computedBlockDigest = new WarcDigest();
					this.computedBlockDigest.digestBytes = digest;
				}

				if ((this.header.warcBlockDigest != null) && (this.header.warcBlockDigest.digestString != null)) {
					this.isValidBlockDigest = processWarcDigest(this.header.warcBlockDigest, this.computedBlockDigest,
							"block");
				}

				if (this.computedBlockDigest != null) {
					processComputedDigest(this.computedBlockDigest, this.reader.blockDigestAlgorithm,
							this.reader.blockDigestEncoding, "block");
				}

				if ((this.httpHeader != null) && (this.httpHeader.isValid())) {
					digest = this.httpHeader.getDigest();

					if (digest != null) {
						this.computedPayloadDigest = new WarcDigest();
						this.computedPayloadDigest.digestBytes = digest;
					}

					if ((this.header.warcPayloadDigest != null)
							&& (this.header.warcPayloadDigest.digestString != null)) {
						this.isValidPayloadDigest = processWarcDigest(this.header.warcPayloadDigest,
								this.computedPayloadDigest, "payload");
					}

					if (this.computedPayloadDigest != null) {
						processComputedDigest(this.computedPayloadDigest, this.reader.payloadDigestAlgorithm,
								this.reader.payloadDigestEncoding, "payload");
					}
				}

			}

			this.trailingNewlines = this.nlp.parseCRLFs(this.in, this.diagnostics);
			if (this.trailingNewlines != 2) {
				addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED, "Trailing newlines",
						new String[] { Integer.toString(this.trailingNewlines), Integer.toString(2) });
			}

			if ((this.diagnostics.hasErrors()) || (this.diagnostics.hasWarnings())) {
				this.bIsCompliant = false;
				this.reader.errors += this.diagnostics.getErrors().size();
				this.reader.warnings += this.diagnostics.getWarnings().size();
			} else {
				this.bIsCompliant = true;
			}
			this.reader.bIsCompliant &= this.bIsCompliant;

			this.consumed = (this.in.getConsumed() - this.startOffset);

			this.bPayloadClosed = true;

			this.reader.recordClosed();
		}
	}

	protected Boolean processWarcDigest(WarcDigest warcDigest, WarcDigest computedDigest, String digestName) {
		Boolean isValidDigest = null;
		int digestAlgorithmLength = WarcDigest.digestAlgorithmLength(warcDigest.algorithm);
		byte[] digest = Base16.decodeToArray(warcDigest.digestString);
		if ((digest != null) && (digest.length == digestAlgorithmLength)) {
			warcDigest.digestBytes = digest;
			warcDigest.encoding = "base16";
		}
		if (warcDigest.digestBytes == null) {
			digest = Base32.decodeToArray(warcDigest.digestString, true);
			if ((digest != null) && (digest.length == digestAlgorithmLength)) {
				warcDigest.digestBytes = digest;
				warcDigest.encoding = "base32";
			}
			if (warcDigest.digestBytes == null) {
				digest = Base64.decodeToArray(warcDigest.digestString, true);
				if ((digest != null) && (digest.length == digestAlgorithmLength)) {
					warcDigest.digestBytes = digest;
					warcDigest.encoding = "base64";
				}
			}
		}
		if (warcDigest.encoding == null) {
			addErrorDiagnosis(DiagnosisType.UNKNOWN, "Record " + digestName + " digest encoding scheme",
					new String[] { warcDigest.digestString });
		}

		if (computedDigest != null) {
			computedDigest.algorithm = warcDigest.algorithm;
			computedDigest.encoding = warcDigest.encoding;
			if (warcDigest.digestBytes != null)
				if (!(Arrays.equals(computedDigest.digestBytes, warcDigest.digestBytes))) {
					addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED, "Incorrect " + digestName + " digest",
							new String[] { Base16.encodeArray(warcDigest.digestBytes),
									Base16.encodeArray(computedDigest.digestBytes) });

					isValidDigest = Boolean.valueOf(false);
				} else {
					isValidDigest = Boolean.valueOf(true);
				}
			else {
				isValidDigest = Boolean.valueOf(false);
			}
		}
		return isValidDigest;
	}

	protected void processComputedDigest(WarcDigest computedDigest, String digestAlgorithm, String digestEncoding,
			String digestName) {
		if (computedDigest.algorithm == null) {
			computedDigest.algorithm = digestAlgorithm;
		}
		if ((computedDigest.encoding == null) && (digestEncoding != null)) {
			if ("base32".equals(digestEncoding))
				computedDigest.encoding = "base32";
			else if ("base64".equals(digestEncoding))
				computedDigest.encoding = "base64";
			else if ("base16".equals(digestEncoding)) {
				computedDigest.encoding = "base16";
			} else {
				addErrorDiagnosis(DiagnosisType.UNKNOWN, "Default " + digestName + " digest encoding scheme",
						new String[] { digestEncoding });
			}

		}

		if (computedDigest.encoding != null)
			if ("base32".equals(computedDigest.encoding))
				computedDigest.digestString = Base32.encodeArray(computedDigest.digestBytes);
			else if ("base64".equals(computedDigest.encoding))
				computedDigest.digestString = Base64.encodeArray(computedDigest.digestBytes);
			else if ("base16".equals(computedDigest.encoding))
				computedDigest.digestString = Base16.encodeArray(computedDigest.digestBytes);
	}

	public boolean isClosed() {
		return this.bClosed;
	}

	public void close() throws IOException {
		if (this.bClosed)
			return;
		if (this.payload != null) {
			this.payload.close();
		}
		payloadClosed();
		this.reader = null;
		this.in = null;
		this.bClosed = true;
	}

	public boolean isCompliant() {
		return this.bIsCompliant;
	}

	public long getStartOffset() {
		return this.header.startOffset;
	}

	public long getConsumed() {
		return this.consumed;
	}

	public List<HeaderLine> getHeaderList() {
		return Collections.unmodifiableList(this.header.headerList);
	}

	public HeaderLine getHeader(String field) {
		if ((field != null) && (field.length() > 0)) {
			return ((HeaderLine) this.header.headerMap.get(field.toLowerCase()));
		}
		return null;
	}

	public boolean hasPayload() {
		return (this.payload != null);
	}

	public Payload getPayload() {
		return this.payload;
	}

	public InputStream getPayloadContent() {
		return ((this.payload != null) ? this.payload.getInputStream() : null);
	}

	public HttpHeader getHttpHeader() {
		return this.httpHeader;
	}

	protected void addErrorDiagnosis(DiagnosisType type, String entity, String[] information) {
		this.diagnostics.addError(new Diagnosis(type, entity, information));
	}
}