/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.util.Date;
import org.jwat.common.Uri;
import org.jwat.common.UriProfile;
import org.jwat.common.IPAddressParser;
import java.net.InetAddress;
import org.jwat.common.ContentType;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;

public class WarcFieldParsers {
	protected Diagnostics<Diagnosis> diagnostics;

	protected void addInvalidExpectedError(String entity, String... information) {
		this.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED, entity, information));
	}

	protected void addEmptyWarning(String entity) {
		this.diagnostics.addWarning(new Diagnosis(DiagnosisType.EMPTY, entity, new String[0]));
	}

	protected String parseString(final String str, final String field) {
		if (str == null || str.trim().length() == 0) {
			this.addEmptyWarning("'" + field + "' field");
		}
		return str;
	}

	protected Integer parseInteger(final String intStr, final String field) {
		Integer iVal = null;
		if (intStr != null && intStr.length() > 0) {
			try {
				iVal = Integer.valueOf(intStr);
			} catch (Exception e) {
				this.addInvalidExpectedError("'" + field + "' value", intStr, "Numeric format");
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return iVal;
	}

	protected Long parseLong(final String longStr, final String field) {
		Long lVal = null;
		if (longStr != null && longStr.length() > 0) {
			try {
				lVal = Long.valueOf(longStr);
			} catch (Exception e) {
				this.addInvalidExpectedError("'" + field + "' value", longStr, "Numeric format");
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return lVal;
	}

	protected ContentType parseContentType(final String contentTypeStr, final String field) {
		ContentType contentType = null;
		if (contentTypeStr != null && contentTypeStr.length() != 0) {
			contentType = ContentType.parseContentType(contentTypeStr);
			if (contentType == null) {
				this.addInvalidExpectedError("'" + field + "' value", contentTypeStr,
						"<type>/<sub-type>(; <argument>=<value>)*");
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return contentType;
	}

	protected InetAddress parseIpAddress(final String ipAddress, final String field) {
		InetAddress inetAddr = null;
		if (ipAddress != null && ipAddress.length() > 0) {
			inetAddr = IPAddressParser.getAddress(ipAddress);
			if (inetAddr == null) {
				this.addInvalidExpectedError("'" + field + "' value", ipAddress, "IPv4 or IPv6 format");
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return inetAddr;
	}

	protected Uri parseUri(final String uriStr, final boolean bLtGt, final UriProfile uriProfile, final String field) {
		Uri uri = null;
		String uriStrClean = uriStr;
		int ltGtBf = 0;
		if (uriStrClean != null && uriStrClean.length() != 0) {
			int fIdx = 0;
			int tIdx = uriStrClean.length();
			if (uriStrClean.startsWith("<")) {
				ltGtBf |= 0x2;
				++fIdx;
			}
			if (uriStrClean.endsWith(">")) {
				ltGtBf |= 0x1;
				--tIdx;
			}
			if (ltGtBf != 0) {
				uriStrClean = uriStrClean.substring(fIdx, tIdx);
			}
			if (bLtGt) {
				switch (ltGtBf) {
				case 2: {
					this.addInvalidExpectedError("'" + field + "' value", uriStr, "Missing trailing '>' character");
					break;
				}
				case 1: {
					this.addInvalidExpectedError("'" + field + "' value", uriStr, "Missing leading '<' character");
					break;
				}
				case 0: {
					this.addInvalidExpectedError("'" + field + "' value", uriStr,
							"Missing encapsulating '<' and '>' characters");
					break;
				}
				}
			} else {
				switch (ltGtBf) {
				case 2: {
					this.addInvalidExpectedError("'" + field + "' value", uriStr, "Unexpected leading '<' character");
					break;
				}
				case 1: {
					this.addInvalidExpectedError("'" + field + "' value", uriStr, "Unexpected trailing '>' character");
					break;
				}
				case 3: {
					this.addInvalidExpectedError("'" + field + "' value", uriStr,
							"Unexpected encapsulating '<' and '>' characters");
					break;
				}
				}
			}
			try {
				uri = new Uri(uriStrClean, uriProfile);
			} catch (Exception e) {
				this.addInvalidExpectedError("'" + field + "' value", uriStrClean, e.getMessage());
			}
			if (uri != null) {
				String scheme = uri.getScheme();
				if (scheme == null) {
					uri = null;
					this.addInvalidExpectedError("'" + field + "' value", uriStrClean, "Absolute URI");
				} else {
					scheme = scheme.toLowerCase();
				}
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return uri;
	}

	protected Date parseDate(final String dateStr, final String field) {
		Date date = null;
		if (dateStr != null && dateStr.length() > 0) {
			date = WarcDateParser.getDate(dateStr);
			if (date == null) {
				this.addInvalidExpectedError("'" + field + "' value", dateStr, "yyyy-MM-dd'T'HH:mm:ss'Z'");
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return date;
	}

	protected WarcDigest parseDigest(final String labelledDigest, final String field) {
		WarcDigest digest = null;
		if (labelledDigest != null && labelledDigest.length() > 0) {
			digest = WarcDigest.parseWarcDigest(labelledDigest);
			if (digest == null) {
				this.addInvalidExpectedError("'" + field + "' value", labelledDigest,
						"<digest-algorithm>:<digest-encoded>");
			}
		} else {
			this.addEmptyWarning("'" + field + "' field");
		}
		return digest;
	}
}