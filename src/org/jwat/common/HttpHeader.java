/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.Collections;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public class HttpHeader extends PayloadWithHeaderAbstract {
	public static final int HT_RESPONSE = 1;
	public static final int HT_REQUEST = 2;
	public static final String PROTOCOL_HTTP = "http";
	public static final String PROTOCOL_HTTPS = "https";
	protected static final String HTTP_VERSION_SUFFIX = "HTTP/";
	protected static final String CONTENT_TYPE;
	public int headerType;
	public String method;
	public String requestUri;
	public String httpVersion;
	public Integer httpVersionMajor;
	public Integer httpVersionMinor;
	public String statusCodeStr;
	public Integer statusCode;
	public String reasonPhrase;
	protected List<HeaderLine> headerList;
	protected Map<String, HeaderLine> headerMap;
	public String contentType;

	protected HttpHeader() {
		this.headerList = new LinkedList<HeaderLine>();
		this.headerMap = new HashMap<String, HeaderLine>();
	}

	public static boolean isSupported(final String protocol) {
		return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
	}

	public static HttpHeader processPayload(final int headerType, final ByteCountingPushBackInputStream pbin,
			final long length, final String digestAlgorithm) throws IOException {
		if (headerType != 1 && headerType != 2) {
			throw new IllegalArgumentException("Invalid 'headerType' argument: " + headerType);
		}
		if (pbin == null) {
			throw new IllegalArgumentException("The inputstream 'pbin' is null");
		}
		if (length < 0L) {
			throw new IllegalArgumentException("The 'length' is less than zero: " + length);
		}
		final HttpHeader hh = new HttpHeader();
		hh.headerType = headerType;
		hh.in_pb = pbin;
		hh.totalLength = length;
		hh.digestAlgorithm = digestAlgorithm;
		hh.diagnostics = new Diagnostics();
		hh.initProcess();
		return hh;
	}

	protected boolean readHeader(final MaxLengthRecordingInputStream in, final long payloadLength) throws IOException {
		final PushbackInputStream pbin = new PushbackInputStream((InputStream) in, this.in_pb.getPushbackSize());
		final HeaderLineReader hlr = HeaderLineReader.getHeaderLineReader();
		hlr.bNameValue = false;
		hlr.encoding = 2;
		hlr.bLWS = false;
		hlr.bQuotedText = false;
		hlr.bEncodedWords = false;
		boolean bValidHttpHeader = false;
		HeaderLine line = hlr.readLine(pbin);
		int bfErrors = 0;
		if (!hlr.bEof && line.type == 1 && line.line != null && line.line.length() > 0) {
			bfErrors = (line.bfErrors & 0xFFFFFF7F);
			if (this.headerType == 1) {
				bValidHttpHeader = this.isHttpStatusLineValid(line.line);
			} else {
				if (this.headerType != 2) {
					throw new IllegalStateException("Invalid headerType!");
				}
				bValidHttpHeader = this.isHttpRequestLineValid(line.line);
			}
		}
		hlr.bNameValue = true;
		hlr.bLWS = true;
		hlr.bQuotedText = true;
		hlr.bEncodedWords = true;
		boolean bLoop = bValidHttpHeader;
		while (bLoop) {
			line = hlr.readLine(pbin);
			bfErrors |= line.bfErrors;
			if (!hlr.bEof) {
				switch (line.type) {
				case 2: {
					if (HttpHeader.CONTENT_TYPE.equals(line.name.toUpperCase())) {
						this.contentType = line.value;
					}
					final HeaderLine tmpLine = this.headerMap.get(line.name.toLowerCase());
					if (tmpLine == null) {
						this.headerMap.put(line.name.toLowerCase(), line);
					} else {
						tmpLine.lines.add(line);
					}
					this.headerList.add(line);
					continue;
				}
				case 1: {
					if (line.line.length() == 0) {
						bLoop = false;
						continue;
					}
					continue;
				}
				}
			} else {
				if ((bfErrors & 0x1) == 0x0 || in.record.size() != payloadLength) {
					bValidHttpHeader = false;
				}
				bLoop = false;
			}
		}
		HeaderLineReader.report_error(bfErrors, this.diagnostics);
		if (bValidHttpHeader) {
			this.payloadLength = payloadLength - in.record.size();
		}
		return bValidHttpHeader;
	}

	protected boolean isHttpStatusLineValid(final String statusLine) {
		boolean bIsHttpStatusLineValid = statusLine != null && statusLine.length() > 0;
		if (bIsHttpStatusLineValid) {
			int idx = statusLine.indexOf(32);
			if (idx > 0) {
				bIsHttpStatusLineValid = this.isHttpVersionValid(statusLine.substring(0, idx));
			} else {
				if (idx == -1) {
					this.httpVersion = statusLine;
				}
				bIsHttpStatusLineValid = false;
			}
			if (bIsHttpStatusLineValid) {
				final int prevIdx = ++idx;
				idx = statusLine.indexOf(32, idx);
				if (idx == -1) {
					idx = statusLine.length();
				}
				if (idx > prevIdx) {
					this.statusCodeStr = statusLine.substring(prevIdx, idx);
					try {
						this.statusCode = Integer.parseInt(this.statusCodeStr);
						if (this.statusCode < 100 || this.statusCode > 999) {
							bIsHttpStatusLineValid = false;
						}
					} catch (NumberFormatException e) {
						bIsHttpStatusLineValid = false;
					}
				} else {
					bIsHttpStatusLineValid = false;
				}
				if (bIsHttpStatusLineValid && idx < statusLine.length()) {
					++idx;
					this.reasonPhrase = statusLine.substring(idx);
				}
			}
		}
		return bIsHttpStatusLineValid;
	}

	protected boolean isHttpVersionValid(final String versionString) {
		this.httpVersion = versionString;
		boolean bIsHttpVersionValid = versionString.startsWith("HTTP/");
		if (bIsHttpVersionValid) {
			final int idx = versionString.indexOf(46, "HTTP/".length());
			if (idx > 0) {
				try {
					this.httpVersionMajor = Integer.parseInt(versionString.substring("HTTP/".length(), idx));
					if (this.httpVersionMajor < 0) {
						bIsHttpVersionValid = false;
					}
				} catch (NumberFormatException e) {
					bIsHttpVersionValid = false;
				}
				try {
					this.httpVersionMinor = Integer.parseInt(versionString.substring(idx + 1));
					if (this.httpVersionMinor < 0) {
						bIsHttpVersionValid = false;
					}
				} catch (NumberFormatException e) {
					bIsHttpVersionValid = false;
				}
			} else {
				bIsHttpVersionValid = false;
			}
		}
		return bIsHttpVersionValid;
	}

	protected boolean isHttpRequestLineValid(final String requestLine) {
		boolean bIsHttpRequestLineValid = requestLine != null && requestLine.length() > 0;
		if (bIsHttpRequestLineValid) {
			int idx = requestLine.indexOf(32);
			if (idx > 0) {
				this.method = requestLine.substring(0, idx);
			} else {
				if (idx == -1) {
					this.method = requestLine;
				}
				bIsHttpRequestLineValid = false;
			}
			if (bIsHttpRequestLineValid) {
				final int prevIdx = ++idx;
				idx = requestLine.indexOf(32, idx);
				if (idx > prevIdx) {
					this.requestUri = requestLine.substring(prevIdx, idx);
				} else {
					if (idx == -1) {
						this.requestUri = requestLine.substring(prevIdx);
					}
					bIsHttpRequestLineValid = false;
				}
				if (bIsHttpRequestLineValid) {
					++idx;
					bIsHttpRequestLineValid = this.isHttpVersionValid(requestLine.substring(idx));
				}
			}
		}
		return bIsHttpRequestLineValid;
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

	public String getProtocolStatusCodeStr() {
		return this.statusCodeStr;
	}

	public Integer getProtocolStatusCode() {
		return this.statusCode;
	}

	public String getProtocolVersion() {
		return this.httpVersion;
	}

	public String getProtocolContentType() {
		return this.contentType;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder(256);
		builder.append("\nHttpHeader : [\n");
		if (this.statusCode != null) {
			builder.append(", HttpResultCode: ").append(this.statusCode);
		}
		if (this.httpVersion != null) {
			builder.append(", HttpProtocolVersion: ").append(this.httpVersion);
		}
		if (this.contentType != null) {
			builder.append(", HttpContentType: ").append(this.contentType);
		}
		builder.append("]\n");
		return builder.toString();
	}

	static {
		CONTENT_TYPE = "Content-Type".toUpperCase();
	}
}