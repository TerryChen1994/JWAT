/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.util.Collections;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jwat.common.MaxLengthRecordingInputStream;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.DiagnosisType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.jwat.common.HeaderLine;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.net.InetAddress;
import org.jwat.common.ContentType;
import java.util.Date;
import org.jwat.common.Uri;
import java.text.DateFormat;
import org.jwat.common.UriProfile;
import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;

public class WarcHeader {
	public static final boolean URI_LTGT = true;
	public static final boolean URI_NAKED = false;
	protected WarcReader reader;
	protected Diagnostics<Diagnosis> diagnostics;
	protected UriProfile warcTargetUriProfile;
	protected UriProfile uriProfile;
	protected WarcFieldParsers fieldParsers;
	protected DateFormat warcDateFormat;
	protected long startOffset;
	public boolean bMagicIdentified;
	public boolean bVersionParsed;
	public boolean bValidVersionFormat;
	public boolean bValidVersion;
	public String versionStr;
	public int[] versionArr;
	public int major;
	public int minor;
	protected boolean[] seen;
	public boolean bMandatoryMissing;
	public String warcTypeStr;
	public Integer warcTypeIdx;
	public String warcFilename;
	public String warcRecordIdStr;
	public Uri warcRecordIdUri;
	public String warcDateStr;
	public Date warcDate;
	public String contentLengthStr;
	public Long contentLength;
	public String contentTypeStr;
	public ContentType contentType;
	public String warcTruncatedStr;
	public Integer warcTruncatedIdx;
	public String warcIpAddress;
	public InetAddress warcInetAddress;
	public List<WarcConcurrentTo> warcConcurrentToList;
	public String warcRefersToStr;
	public Uri warcRefersToUri;
	public String warcTargetUriStr;
	public Uri warcTargetUriUri;
	public String warcWarcinfoIdStr;
	public Uri warcWarcinfoIdUri;
	public String warcBlockDigestStr;
	public WarcDigest warcBlockDigest;
	public String warcPayloadDigestStr;
	public WarcDigest warcPayloadDigest;
	public String warcIdentifiedPayloadTypeStr;
	public ContentType warcIdentifiedPayloadType;
	public String warcProfileStr;
	public Uri warcProfileUri;
	public Integer warcProfileIdx;
	public String warcSegmentNumberStr;
	public Integer warcSegmentNumber;
	public String warcSegmentOriginIdStr;
	public Uri warcSegmentOriginIdUrl;
	public String warcSegmentTotalLengthStr;
	public Long warcSegmentTotalLength;
	public String warcRefersToTargetUriStr;
	public Uri warcRefersToTargetUriUri;
	public String warcRefersToDateStr;
	public Date warcRefersToDate;
	protected ByteArrayOutputStream headerBytesOut;
	public byte[] headerBytes;
	protected List<HeaderLine> headerList;
	protected Map<String, HeaderLine> headerMap;

	protected WarcHeader() {
		this.startOffset = -1L;
		this.major = -1;
		this.minor = -1;
		this.seen = new boolean[22];
		this.warcConcurrentToList = new LinkedList<WarcConcurrentTo>();
		this.headerBytesOut = new ByteArrayOutputStream();
		this.headerList = new LinkedList<HeaderLine>();
		this.headerMap = new HashMap<String, HeaderLine>();
	}

	public static WarcHeader initHeader(final WarcWriter writer, final Diagnostics<Diagnosis> diagnostics) {
		final WarcHeader header = new WarcHeader();
		header.major = 1;
		header.minor = 0;
		header.warcTargetUriProfile = writer.warcTargetUriProfile;
		header.uriProfile = writer.uriProfile;
		header.fieldParsers = writer.fieldParsers;
		header.warcDateFormat = writer.warcDateFormat;
		header.diagnostics = diagnostics;
		return header;
	}

	public static WarcHeader initHeader(final WarcReader reader, final long startOffset,
			final Diagnostics<Diagnosis> diagnostics) {
		final WarcHeader header = new WarcHeader();
		header.reader = reader;
		header.warcTargetUriProfile = reader.warcTargetUriProfile;
		header.uriProfile = reader.uriProfile;
		header.fieldParsers = reader.fieldParsers;
		header.diagnostics = diagnostics;
		header.startOffset = startOffset;
		return header;
	}

	protected void addErrorDiagnosis(DiagnosisType type, String entity, String... information) {
		this.diagnostics.addError(new Diagnosis(type, entity, information));
	}

	protected void addWarningDiagnosis(DiagnosisType type, String entity, String... information) {
		this.diagnostics.addWarning(new Diagnosis(type, entity, information));
	}

	public long getStartOffset() {
		return this.startOffset;
	}

	public boolean parseHeader(final ByteCountingPushBackInputStream in) throws IOException {
		if (this.parseVersion(in)) {
			if (this.bVersionParsed && this.versionArr.length == 2) {
				switch (this.major) {
				case 1: {
					if (this.minor == 0) {
						this.bValidVersion = true;
						break;
					}
					break;
				}
				case 0: {
					switch (this.minor) {
					case 17:
					case 18: {
						this.bValidVersion = true;
						break;
					}
					}
					break;
				}
				}
				if (!this.bValidVersion) {
					this.diagnostics.addError(new Diagnosis(DiagnosisType.UNKNOWN, "Magic version number",
							new String[] { this.versionStr }));
				}
			} else {
				this.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "Magic Version string",
						new String[] { this.versionStr }));
			}
			final MaxLengthRecordingInputStream mrin = new MaxLengthRecordingInputStream((InputStream) in,
					(long) this.reader.recordHeaderMaxSize);
			final ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream((InputStream) mrin,
					this.reader.recordHeaderMaxSize);
			this.parseHeaders(pbin);
			
			pbin.close();
			this.checkFields();
			this.headerBytes = this.headerBytesOut.toByteArray();
		}
		return this.bMagicIdentified;
	}

	protected boolean parseVersion(final ByteCountingPushBackInputStream in) throws IOException {
		this.bMagicIdentified = false;
		this.bVersionParsed = false;
		boolean bInvalidDataBeforeVersion = false;
		boolean bEmptyLinesBeforeVersion = false;
		boolean bSeekMagic = true;
		while (bSeekMagic) {
			this.startOffset = in.getConsumed();
			final HeaderLine line = this.reader.lineReader.readLine((PushbackInputStream) in);
			if (!this.reader.lineReader.bEof) {
				switch (line.type) {
				case 1: {
					final String tmpStr = line.line;
					if (tmpStr.length() <= 0) {
						bEmptyLinesBeforeVersion = true;
						continue;
					}
					if (tmpStr.toUpperCase().startsWith("WARC/")) {
						this.bMagicIdentified = true;
						this.versionStr = tmpStr.substring("WARC/".length());
						final String[] tmpArr = this.versionStr.split("\\.", -1);
						if (tmpArr.length >= 2 && tmpArr.length <= 4) {
							this.bVersionParsed = true;
							this.bValidVersionFormat = true;
							this.versionArr = new int[tmpArr.length];
							for (int i = 0; i < tmpArr.length; ++i) {
								try {
									this.versionArr[i] = Integer.parseInt(tmpArr[i]);
								} catch (NumberFormatException e) {
									this.versionArr[i] = -1;
									this.bValidVersionFormat = false;
								}
							}
							this.major = this.versionArr[0];
							this.minor = this.versionArr[1];
						}
						this.headerBytesOut.write(line.raw);
						bSeekMagic = false;
						continue;
					}
					bInvalidDataBeforeVersion = true;
					continue;
				}
				case 2: {
					bInvalidDataBeforeVersion = true;
					continue;
				}
				}
			} else {
				bSeekMagic = false;
			}
		}
		if (bInvalidDataBeforeVersion) {
			this.addErrorDiagnosis(DiagnosisType.INVALID, "Data before WARC version", new String[0]);
		}
		if (bEmptyLinesBeforeVersion) {
			this.addErrorDiagnosis(DiagnosisType.INVALID, "Empty lines before WARC version", new String[0]);
		}
		return this.bMagicIdentified;
	}

	protected void parseHeaders(final ByteCountingPushBackInputStream in) throws IOException {
		boolean bLoop = true;
		while (bLoop) {
			final HeaderLine headerLine = this.reader.headerLineReader.readLine((PushbackInputStream) in);
			if (!this.reader.headerLineReader.bEof) {
				this.headerBytesOut.write(headerLine.raw);
				switch (headerLine.type) {
				case 2: {
					if (headerLine.name != null && headerLine.name.length() > 0) {
						this.addHeader(headerLine);
						continue;
					}
					this.addWarningDiagnosis(DiagnosisType.EMPTY, "Header line", new String[0]);
					continue;
				}
				case 1: {
					if (headerLine.line.length() == 0) {
						bLoop = false;
						continue;
					}
					this.addWarningDiagnosis(DiagnosisType.UNKNOWN, "Header line", headerLine.line);
					continue;
				}
				default: {
					throw new IllegalStateException("Invalid HeaderLine output!");
				}
				}
			} else {
				bLoop = false;
			}
		}
	}

	protected void addHeader(final HeaderLine headerLine) {
		final String fieldName = headerLine.name;
		final String fieldValue = headerLine.value;
		final Integer fn_idx = WarcConstants.fieldNameIdxMap.get(fieldName.toLowerCase());
		if (fn_idx != null) {
			if (!this.seen[fn_idx] || WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
				this.seen[fn_idx] = true;
				switch (fn_idx) {
				case 1: {
					this.warcTypeStr = this.fieldParsers.parseString(fieldValue, "WARC-Type");
					if (this.warcTypeStr != null) {
						this.warcTypeIdx = WarcConstants.recordTypeIdxMap.get(this.warcTypeStr.toLowerCase());
					}
					if (this.warcTypeIdx == null && this.warcTypeStr != null && this.warcTypeStr.length() > 0) {
						this.warcTypeIdx = 0;
						break;
					}
					break;
				}
				case 2: {
					this.warcRecordIdStr = fieldValue;
					this.warcRecordIdUri = this.fieldParsers.parseUri(fieldValue, true, this.uriProfile,
							"WARC-Record-ID");
					break;
				}
				case 3: {
					this.warcDateStr = fieldValue;
					this.warcDate = this.fieldParsers.parseDate(fieldValue, "WARC-Date");
					break;
				}
				case 4: {
					this.contentLengthStr = fieldValue;
					this.contentLength = this.fieldParsers.parseLong(fieldValue, "Content-Length");
					break;
				}
				case 5: {
					this.contentTypeStr = fieldValue;
					this.contentType = this.fieldParsers.parseContentType(fieldValue, "Content-Type");
					break;
				}
				case 6: {
					final Uri tmpUri = this.fieldParsers.parseUri(fieldValue, true, this.uriProfile,
							"WARC-Concurrent-To");
					if (fieldValue != null && fieldValue.trim().length() > 0) {
						final WarcConcurrentTo warcConcurrentTo = new WarcConcurrentTo();
						warcConcurrentTo.warcConcurrentToStr = fieldValue;
						warcConcurrentTo.warcConcurrentToUri = tmpUri;
						this.warcConcurrentToList.add(warcConcurrentTo);
						break;
					}
					break;
				}
				case 7: {
					this.warcBlockDigestStr = fieldValue;
					this.warcBlockDigest = this.fieldParsers.parseDigest(fieldValue, "WARC-Block-Digest");
					break;
				}
				case 8: {
					this.warcPayloadDigestStr = fieldValue;
					this.warcPayloadDigest = this.fieldParsers.parseDigest(fieldValue, "WARC-Payload-Digest");
					break;
				}
				case 9: {
					this.warcIpAddress = fieldValue;
					this.warcInetAddress = this.fieldParsers.parseIpAddress(fieldValue, "WARC-IP-Address");
					break;
				}
				case 10: {
					this.warcRefersToStr = fieldValue;
					this.warcRefersToUri = this.fieldParsers.parseUri(fieldValue, true, this.uriProfile,
							"WARC-Refers-To");
					break;
				}
				case 11: {
					this.warcTargetUriStr = fieldValue;
					this.warcTargetUriUri = this.fieldParsers.parseUri(fieldValue, false, this.warcTargetUriProfile,
							"WARC-Target-URI");
					break;
				}
				case 12: {
					this.warcTruncatedStr = this.fieldParsers.parseString(fieldValue, "WARC-Truncated");
					if (this.warcTruncatedStr != null) {
						this.warcTruncatedIdx = WarcConstants.truncatedTypeIdxMap
								.get(this.warcTruncatedStr.toLowerCase());
					}
					if (this.warcTruncatedIdx == null && this.warcTruncatedStr != null
							&& this.warcTruncatedStr.length() > 0) {
						this.warcTruncatedIdx = 0;
						break;
					}
					break;
				}
				case 13: {
					this.warcWarcinfoIdStr = fieldValue;
					this.warcWarcinfoIdUri = this.fieldParsers.parseUri(fieldValue, true, this.uriProfile,
							"WARC-Warcinfo-ID");
					break;
				}
				case 14: {
					this.warcFilename = this.fieldParsers.parseString(fieldValue, "WARC-Filename");
					break;
				}
				case 15: {
					this.warcProfileStr = fieldValue;
					this.warcProfileUri = this.fieldParsers.parseUri(fieldValue, false, this.uriProfile,
							"WARC-Profile");
					if (this.warcProfileStr != null) {
						this.warcProfileIdx = WarcConstants.profileIdxMap.get(this.warcProfileStr.toLowerCase());
					}
					if (this.warcProfileIdx == null && this.warcProfileStr != null
							&& this.warcProfileStr.length() > 0) {
						this.warcProfileIdx = 0;
						break;
					}
					break;
				}
				case 16: {
					this.warcIdentifiedPayloadTypeStr = fieldValue;
					this.warcIdentifiedPayloadType = this.fieldParsers.parseContentType(fieldValue,
							"WARC-Identified-Payload-Type");
					break;
				}
				case 17: {
					this.warcSegmentOriginIdStr = fieldValue;
					this.warcSegmentOriginIdUrl = this.fieldParsers.parseUri(fieldValue, true, this.uriProfile,
							"WARC-Segment-Origin-ID");
					break;
				}
				case 18: {
					this.warcSegmentNumberStr = fieldValue;
					this.warcSegmentNumber = this.fieldParsers.parseInteger(fieldValue, "WARC-Segment-Number");
					break;
				}
				case 19: {
					this.warcSegmentTotalLengthStr = fieldValue;
					this.warcSegmentTotalLength = this.fieldParsers.parseLong(fieldValue, "WARC-Segment-Total-Length");
					break;
				}
				case 20: {
					this.warcRefersToTargetUriStr = fieldValue;
					this.warcRefersToTargetUriUri = this.fieldParsers.parseUri(fieldValue, false, this.uriProfile,
							"WARC-Refers-To-Target-URI");
					break;
				}
				case 21: {
					this.warcRefersToDateStr = fieldValue;
					this.warcRefersToDate = this.fieldParsers.parseDate(fieldValue, "WARC-Refers-To-Date");
					break;
				}
				}
			} else {
				this.addErrorDiagnosis(DiagnosisType.DUPLICATE, "'" + fieldName + "' header", fieldValue);
			}
		}
		final HeaderLine tmpLine = this.headerMap.get(fieldName.toLowerCase());
		if (tmpLine == null) {
			this.headerMap.put(fieldName.toLowerCase(), headerLine);
		} else {
			tmpLine.lines.add(headerLine);
		}
		this.headerList.add(headerLine);
	}

	public List<HeaderLine> getHeaderList() {
		return Collections.unmodifiableList((List<? extends HeaderLine>) this.headerList);
	}

	public HeaderLine getHeader(final String field) {
		if (field != null && field.length() > 0) {
			return this.headerMap.get(field.toLowerCase());
		}
		return null;
	}

	public HeaderLine addHeader(final String fieldName, final String fieldValue) {
		final HeaderLine headerLine = new HeaderLine();
		headerLine.name = fieldName;
		headerLine.value = fieldValue;
		this.addHeader(headerLine);
		return headerLine;
	}

	public HeaderLine addHeader(final String fieldName, Integer integerFieldValue, String fieldValueStr) {
		if (integerFieldValue == null && fieldValueStr != null) {
			integerFieldValue = this.fieldParsers.parseInteger(fieldValueStr, fieldName);
		} else if (fieldValueStr == null && integerFieldValue != null) {
			fieldValueStr = integerFieldValue.toString();
		}
		return this.addHeader(fieldName, fieldValueStr, 1, integerFieldValue, null, null, null, null, null, null);
	}

	public HeaderLine addHeader(final String fieldName, Long longFieldValue, String fieldValueStr) {
		if (longFieldValue == null && fieldValueStr != null) {
			longFieldValue = this.fieldParsers.parseLong(fieldValueStr, fieldName);
		} else if (fieldValueStr == null && longFieldValue != null) {
			fieldValueStr = longFieldValue.toString();
		}
		return this.addHeader(fieldName, fieldValueStr, 2, null, longFieldValue, null, null, null, null, null);
	}

	public HeaderLine addHeader(final String fieldName, WarcDigest digestFieldValue, String fieldValueStr) {
		if (digestFieldValue == null && fieldValueStr != null) {
			digestFieldValue = this.fieldParsers.parseDigest(fieldValueStr, fieldName);
		} else if (fieldValueStr == null && digestFieldValue != null) {
			fieldValueStr = digestFieldValue.toString();
		}
		return this.addHeader(fieldName, fieldValueStr, 3, null, null, digestFieldValue, null, null, null, null);
	}

	public HeaderLine addHeader(final String fieldName, ContentType contentTypeFieldValue, String fieldValueStr) {
		if (contentTypeFieldValue == null && fieldValueStr != null) {
			contentTypeFieldValue = this.fieldParsers.parseContentType(fieldValueStr, fieldName);
		} else if (fieldValueStr == null && contentTypeFieldValue != null) {
			fieldValueStr = contentTypeFieldValue.toString();
		}
		return this.addHeader(fieldName, fieldValueStr, 4, null, null, null, contentTypeFieldValue, null, null, null);
	}

	public HeaderLine addHeader(final String fieldName, Date dateFieldValue, String fieldValueStr) {
		if (dateFieldValue == null && fieldValueStr != null) {
			dateFieldValue = this.fieldParsers.parseDate(fieldValueStr, fieldName);
		} else if (fieldValueStr == null && dateFieldValue != null) {
			fieldValueStr = this.warcDateFormat.format(dateFieldValue);
		}
		return this.addHeader(fieldName, fieldValueStr, 5, null, null, null, null, dateFieldValue, null, null);
	}

	public HeaderLine addHeader(final String fieldName, InetAddress inetAddrFieldValue, String fieldValueStr) {
		if (inetAddrFieldValue == null && fieldValueStr != null) {
			inetAddrFieldValue = this.fieldParsers.parseIpAddress(fieldValueStr, fieldName);
		} else if (fieldValueStr == null && inetAddrFieldValue != null) {
			fieldValueStr = inetAddrFieldValue.getHostAddress();
		}
		return this.addHeader(fieldName, fieldValueStr, 6, null, null, null, null, null, inetAddrFieldValue, null);
	}

	public HeaderLine addHeader(final String fieldName, Uri uriFieldValue, String fieldValueStr) {
		if (uriFieldValue == null && fieldValueStr != null) {
			if ("WARC-Target-URI".equalsIgnoreCase(fieldName)) {
				uriFieldValue = this.fieldParsers.parseUri(fieldValueStr, false, this.warcTargetUriProfile, fieldName);
			} else if ("WARC-Profile".equalsIgnoreCase(fieldName)) {
				uriFieldValue = this.fieldParsers.parseUri(fieldValueStr, false, this.uriProfile, fieldName);
			} else if ("WARC-Refers-To-Target-URI".equalsIgnoreCase(fieldName)) {
				uriFieldValue = this.fieldParsers.parseUri(fieldValueStr, false, this.warcTargetUriProfile, fieldName);
			} else {
				uriFieldValue = this.fieldParsers.parseUri(fieldValueStr, true, this.uriProfile, fieldName);
			}
		} else if (fieldValueStr == null && uriFieldValue != null) {
			if ("WARC-Target-URI".equalsIgnoreCase(fieldName) || "WARC-Profile".equalsIgnoreCase(fieldName)
					|| "WARC-Refers-To-Target-URI".equalsIgnoreCase(fieldName)) {
				fieldValueStr = uriFieldValue.toString();
			} else {
				fieldValueStr = "<" + uriFieldValue.toString() + ">";
			}
		}
		return this.addHeader(fieldName, fieldValueStr, 7, null, null, null, null, null, null, uriFieldValue);
	}

	public HeaderLine addHeader(final String fieldName, final String fieldValueStr, int dt,
			final Integer integerFieldValue, Long longFieldValue, final WarcDigest digestFieldValue,
			final ContentType contentTypeFieldValue, final Date dateFieldValue, final InetAddress inetAddrFieldValue,
			final Uri uriFieldValue) {
		final Integer fn_idx = WarcConstants.fieldNameIdxMap.get(fieldName.toLowerCase());
		if (fn_idx != null) {
			if (WarcConstants.FN_IDX_DT[fn_idx] == 2 && dt == 1) {
				longFieldValue = (long) integerFieldValue;
				dt = 2;
			}
			if (dt == WarcConstants.FN_IDX_DT[fn_idx]) {
				if (this.seen[fn_idx] && !WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
					this.addErrorDiagnosis(DiagnosisType.DUPLICATE, "'" + fieldName + "' header", fieldValueStr);
				}
				this.seen[fn_idx] = true;
				switch (fn_idx) {
				case 18: {
					this.warcSegmentNumberStr = fieldValueStr;
					this.warcSegmentNumber = integerFieldValue;
					break;
				}
				case 4: {
					this.contentLengthStr = fieldValueStr;
					this.contentLength = longFieldValue;
					break;
				}
				case 19: {
					this.warcSegmentTotalLengthStr = fieldValueStr;
					this.warcSegmentTotalLength = longFieldValue;
					break;
				}
				case 7: {
					this.warcBlockDigestStr = fieldValueStr;
					this.warcBlockDigest = digestFieldValue;
					break;
				}
				case 8: {
					this.warcPayloadDigestStr = fieldValueStr;
					this.warcPayloadDigest = digestFieldValue;
					break;
				}
				case 5: {
					this.contentTypeStr = fieldValueStr;
					this.contentType = contentTypeFieldValue;
					break;
				}
				case 16: {
					this.warcIdentifiedPayloadTypeStr = fieldValueStr;
					this.warcIdentifiedPayloadType = contentTypeFieldValue;
					break;
				}
				case 3: {
					this.warcDateStr = fieldValueStr;
					this.warcDate = dateFieldValue;
					break;
				}
				case 21: {
					this.warcRefersToDateStr = fieldValueStr;
					this.warcRefersToDate = dateFieldValue;
					break;
				}
				case 9: {
					this.warcIpAddress = fieldValueStr;
					this.warcInetAddress = inetAddrFieldValue;
					break;
				}
				case 2: {
					this.warcRecordIdStr = fieldValueStr;
					this.warcRecordIdUri = uriFieldValue;
					break;
				}
				case 6: {
					if (fieldValueStr != null || uriFieldValue != null) {
						final WarcConcurrentTo warcConcurrentTo = new WarcConcurrentTo();
						warcConcurrentTo.warcConcurrentToStr = fieldValueStr;
						warcConcurrentTo.warcConcurrentToUri = uriFieldValue;
						this.warcConcurrentToList.add(warcConcurrentTo);
						break;
					}
					break;
				}
				case 10: {
					this.warcRefersToStr = fieldValueStr;
					this.warcRefersToUri = uriFieldValue;
					break;
				}
				case 11: {
					this.warcTargetUriStr = fieldValueStr;
					this.warcTargetUriUri = uriFieldValue;
					break;
				}
				case 13: {
					this.warcWarcinfoIdStr = fieldValueStr;
					this.warcWarcinfoIdUri = uriFieldValue;
					break;
				}
				case 15: {
					this.warcProfileStr = fieldValueStr;
					this.warcProfileUri = uriFieldValue;
					if (this.warcProfileStr != null) {
						this.warcProfileIdx = WarcConstants.profileIdxMap.get(this.warcProfileStr.toLowerCase());
					}
					if (this.warcProfileIdx == null && this.warcProfileStr != null
							&& this.warcProfileStr.length() > 0) {
						this.warcProfileIdx = 0;
						break;
					}
					break;
				}
				case 17: {
					this.warcSegmentOriginIdStr = fieldValueStr;
					this.warcSegmentOriginIdUrl = uriFieldValue;
					break;
				}
				case 20: {
					this.warcRefersToTargetUriStr = fieldValueStr;
					this.warcRefersToTargetUriUri = uriFieldValue;
					break;
				}
				}
			} else {
				this.addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
						"Invalid datatype for '" + fieldName + "' header",
						WarcConstants.FDT_IDX_STRINGS[WarcConstants.FN_IDX_DT[fn_idx]],
						WarcConstants.FDT_IDX_STRINGS[dt]);
			}
		}
		final HeaderLine headerLine = new HeaderLine();
		headerLine.name = fieldName;
		headerLine.value = fieldValueStr;
		final HeaderLine tmpLine = this.headerMap.get(fieldName.toLowerCase());
		if (tmpLine == null) {
			this.headerMap.put(fieldName.toLowerCase(), headerLine);
		} else {
			tmpLine.lines.add(headerLine);
		}
		return headerLine;
	}

	protected void checkFields() {
		this.bMandatoryMissing = false;
		if (this.warcTypeIdx != null && this.warcTypeIdx == 0) {
			this.addWarningDiagnosis(DiagnosisType.UNKNOWN, "'WARC-Type' value", this.warcTypeStr);
		}
		if (this.warcProfileIdx != null && this.warcProfileIdx == 0) {
			this.addWarningDiagnosis(DiagnosisType.UNKNOWN, "'WARC-Profile' value", this.warcProfileStr);
		}
		if (this.warcTypeIdx == null) {
			this.addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'WARC-Type' header", this.warcTypeStr);
			this.bMandatoryMissing = true;
		}
		if (this.warcRecordIdUri == null) {
			this.addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'WARC-Record-ID' header", this.warcRecordIdStr);
			this.bMandatoryMissing = true;
		}
		if (this.warcDate == null) {
			this.addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'WARC-Date' header", this.warcDateStr);
			this.bMandatoryMissing = true;
		}
		if (this.contentLength == null) {
			this.addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'Content-Length' header", this.contentLengthStr);
			this.bMandatoryMissing = true;
		}
		if (this.contentLength != null && this.contentLength > 0L
				&& (this.contentTypeStr == null || this.contentTypeStr.length() == 0)
				&& (this.warcTypeIdx == null || this.warcTypeIdx != 8)) {
			this.addWarningDiagnosis(DiagnosisType.RECOMMENDED_MISSING, "'Content-Type' header", new String[0]);
		}
		if (this.warcTypeIdx != null) {
			if (this.warcTypeIdx == 1 && this.contentType != null
					&& (!this.contentType.contentType.equals("application")
							|| !this.contentType.mediaType.equals("warc-fields"))) {
				this.addWarningDiagnosis(DiagnosisType.RECOMMENDED, "'Content-Type' value", "application/warc-fields",
						this.contentTypeStr);
			}
			if (this.warcTypeIdx == 2 && this.warcSegmentNumber != null && this.warcSegmentNumber != 1) {
				this.addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED, "'WARC-Segment-Number' value",
						this.warcSegmentNumber.toString(), "1");
			}
			if (this.warcTypeIdx == 8 && this.warcSegmentNumber != null && this.warcSegmentNumber < 2) {
				this.addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED, "'WARC-Segment-Number' value",
						this.warcSegmentNumber.toString(), ">1");
			}
			if (this.warcTypeIdx > 0) {
				this.checkFieldPolicy(this.warcTypeIdx, 5, this.contentType, this.contentTypeStr);
				this.checkFieldPolicy(this.warcTypeIdx, 9, this.warcInetAddress, this.warcIpAddress);
				for (int i = 0; i < this.warcConcurrentToList.size(); ++i) {
					final WarcConcurrentTo warcConcurrentTo = this.warcConcurrentToList.get(0);
					this.checkFieldPolicy(this.warcTypeIdx, 6, warcConcurrentTo.warcConcurrentToUri,
							warcConcurrentTo.warcConcurrentToStr);
				}
				this.checkFieldPolicy(this.warcTypeIdx, 10, this.warcRefersToUri, this.warcRefersToStr);
				this.checkFieldPolicy(this.warcTypeIdx, 11, this.warcTargetUriUri, this.warcTargetUriStr);
				this.checkFieldPolicy(this.warcTypeIdx, 12, this.warcTruncatedIdx, this.warcTruncatedStr);
				this.checkFieldPolicy(this.warcTypeIdx, 13, this.warcWarcinfoIdUri, this.warcWarcinfoIdStr);
				this.checkFieldPolicy(this.warcTypeIdx, 7, this.warcBlockDigest, this.warcBlockDigestStr);
				this.checkFieldPolicy(this.warcTypeIdx, 8, this.warcPayloadDigest, this.warcPayloadDigestStr);
				this.checkFieldPolicy(this.warcTypeIdx, 14, this.warcFilename, this.warcFilename);
				this.checkFieldPolicy(this.warcTypeIdx, 15, this.warcProfileUri, this.warcProfileStr);
				this.checkFieldPolicy(this.warcTypeIdx, 16, this.warcIdentifiedPayloadType,
						this.warcIdentifiedPayloadTypeStr);
				this.checkFieldPolicy(this.warcTypeIdx, 18, this.warcSegmentNumber, this.warcSegmentNumberStr);
				this.checkFieldPolicy(this.warcTypeIdx, 17, this.warcSegmentOriginIdUrl, this.warcSegmentOriginIdStr);
				this.checkFieldPolicy(this.warcTypeIdx, 19, this.warcSegmentTotalLength,
						this.warcSegmentTotalLengthStr);
				this.checkFieldPolicy(this.warcTypeIdx, 20, this.warcRefersToTargetUriUri,
						this.warcRefersToTargetUriStr);
				this.checkFieldPolicy(this.warcTypeIdx, 21, this.warcRefersToDate, this.warcRefersToDateStr);
			}
		}
	}

	protected void checkFieldPolicy(final int recordType, final int fieldType, final Object fieldObj,
			final String valueStr) {
		final int policy = WarcConstants.field_policy[recordType][fieldType];
		switch (policy) {
		case 1: {
			if (fieldObj == null) {
				this.addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID,
						"'" + WarcConstants.FN_IDX_STRINGS[fieldType] + "' value", valueStr);
				break;
			}
			break;
		}
		case 2: {
			if (fieldObj == null) {
				this.addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID,
						"'" + WarcConstants.FN_IDX_STRINGS[fieldType] + "' value", valueStr);
				break;
			}
			break;
		}
		case 3: {
			if (fieldObj != null) {
				this.addErrorDiagnosis(DiagnosisType.UNDESIRED_DATA,
						"'" + WarcConstants.FN_IDX_STRINGS[fieldType] + "' value", valueStr);
				break;
			}
			break;
		}
		case 5: {
			if (fieldObj != null) {
				this.addWarningDiagnosis(DiagnosisType.UNDESIRED_DATA,
						"'" + WarcConstants.FN_IDX_STRINGS[fieldType] + "' value", valueStr);
				break;
			}
			break;
		}
		}
	}
}