/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.net.URISyntaxException;

public class Uri implements Comparable<Uri> {
	protected String hierPartRaw;
	protected String schemeSpecificPartRaw;
	protected String authorityRaw;
	protected String userinfoRaw;
	protected String hostRaw;
	protected String portRaw;
	protected String pathRaw;
	protected String queryRaw;
	protected String fragmentRaw;
	protected String scheme;
	protected String schemeSpecificPart;
	protected String authority;
	protected String userinfo;
	protected String host;
	protected int port;
	protected String path;
	protected String query;
	protected String fragment;
	protected boolean bAbsolute;
	protected boolean bOpaque;
	protected UriProfile uriProfile;

	protected Uri() {
		this.port = -1;
		this.uriProfile = UriProfile.RFC3986;
	}

	public static Uri create(final String str) throws IllegalArgumentException {
		try {
			return new Uri(str, UriProfile.RFC3986);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URI", e);
		}
	}

	public static Uri create(final String str, final UriProfile uriProfile) throws IllegalArgumentException {
		try {
			return new Uri(str, uriProfile);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URI", e);
		}
	}

	public Uri(final String str) throws URISyntaxException {
		this(str, UriProfile.RFC3986);
	}

	public Uri(final String str, final UriProfile uriProfile) throws URISyntaxException {
		this.port = -1;
		this.uriProfile = UriProfile.RFC3986;
		this.uriProfile = uriProfile;
		int idx = uriProfile.indexOf(32, str, 0);
		if (idx != -1 && str.charAt(idx) == ':') {
			this.scheme = str.substring(0, idx++);
			this.validate_absoluteUri(str, idx);
			this.bAbsolute = true;
			if (this.schemeSpecificPart.length() > 0 && !this.schemeSpecificPart.startsWith("/")) {
				this.bOpaque = true;
			}
		} else {
			if (!uriProfile.bAllowRelativeUris) {
				throw new URISyntaxException(str, "Invalid URI - relative URIs not allowed");
			}
			this.validate_relativeUri(str, 0);
		}
	}

	protected void validate_absoluteUri(final String uriStr, final int uIdx) throws URISyntaxException {
		if (this.scheme.length() > 0) {
			this.uriProfile.validate_first_follow(this.scheme, 4, 8);
			this.validate_relativeUri(uriStr, uIdx);
			return;
		}
		throw new URISyntaxException(this.scheme, "Empty URI scheme component");
	}

	protected void validate_relativeUri(final String uriStr, final int uIdx) throws URISyntaxException {
		int qfIdx = uriStr.length();
		int qIdx = uriStr.indexOf(63, uIdx);
		int fIdx;
		if (qIdx != -1) {
			qfIdx = qIdx++;
			fIdx = uriStr.indexOf(35, qIdx);
			if (fIdx != -1) {
				this.queryRaw = uriStr.substring(qIdx, fIdx);
			} else {
				this.queryRaw = uriStr.substring(qIdx);
			}
		} else {
			fIdx = uriStr.indexOf(35, uIdx);
		}
		if (fIdx != -1) {
			if (fIdx < qfIdx) {
				qfIdx = fIdx;
			}
			++fIdx;
			this.fragmentRaw = uriStr.substring(fIdx);
		}
		this.hierPartRaw = uriStr.substring(uIdx, qfIdx);
		if (this.hierPartRaw.startsWith("//")) {
			final int pIdx = this.hierPartRaw.indexOf(47, 2);
			if (pIdx != -1) {
				this.authorityRaw = this.hierPartRaw.substring(2, pIdx);
				this.pathRaw = this.hierPartRaw.substring(pIdx);
			} else {
				this.authorityRaw = this.hierPartRaw.substring(2);
				this.pathRaw = "";
			}
		} else {
			this.pathRaw = this.hierPartRaw;
		}
		if (this.queryRaw != null) {
			this.schemeSpecificPartRaw = this.hierPartRaw + '?' + this.queryRaw;
		} else {
			this.schemeSpecificPartRaw = this.hierPartRaw;
		}
		if (this.authorityRaw != null) {
			int aIdx = this.authorityRaw.indexOf(64);
			if (aIdx != -1) {
				this.userinfoRaw = this.authorityRaw.substring(0, aIdx++);
			} else {
				aIdx = 0;
			}
			if (aIdx < this.authorityRaw.length() && this.authorityRaw.charAt(aIdx) == '[') {
				int bIdx = this.authorityRaw.indexOf(93, aIdx);
				if (bIdx == -1) {
					throw new URISyntaxException(this.authorityRaw,
							"Invalid URI authority/host component - missing ']'");
				}
				++bIdx;
				this.hostRaw = this.authorityRaw.substring(aIdx, bIdx);
				this.host = this.hostRaw;
				if (bIdx < this.authorityRaw.length()) {
					if (this.authorityRaw.charAt(bIdx++) != ':') {
						throw new URISyntaxException(this.authorityRaw,
								"Invalid URI authority/port component - expected a ':'");
					}
					this.portRaw = this.authorityRaw.substring(bIdx);
				}
			} else {
				int pIdx2 = this.authorityRaw.indexOf(58, aIdx);
				if (pIdx2 != -1) {
					this.hostRaw = this.authorityRaw.substring(aIdx, pIdx2++);
					this.portRaw = this.authorityRaw.substring(pIdx2);
				} else {
					this.hostRaw = this.authorityRaw.substring(aIdx);
				}
				this.host = this.uriProfile.validate_decode(1024, "host", this.hostRaw);
			}
			this.authority = "";
			if (this.userinfoRaw != null) {
				this.userinfo = this.uriProfile.validate_decode(512, "userinfo", this.userinfoRaw);
				this.authority = this.authority + this.userinfo + '@';
			}
			this.authority += this.host;
			Label_0768: {
				if (this.portRaw != null) {
					if (this.portRaw.length() > 0) {
						try {
							this.port = Integer.parseInt(this.portRaw);
							if (this.port < 1 || this.port > 65535) {
								throw new URISyntaxException(this.portRaw,
										"Invalid URI port component - port is not within range [1-65535]");
							}
							this.authority = this.authority + ':' + this.portRaw;
							break Label_0768;
						} catch (NumberFormatException e) {
							throw new URISyntaxException(this.portRaw, "Invalid URI port component");
						}
					}
					this.authority += ':';
				}
			}
			this.schemeSpecificPart = "//" + this.authority;
			if (this.pathRaw.length() > 0) {
				this.path = this.uriProfile.validate_decode(16384, "path", this.pathRaw);
			} else {
				this.path = "";
			}
			this.schemeSpecificPart += this.path;
		} else {
			if (this.pathRaw.length() > 0) {
				this.path = this.uriProfile.validate_decode(16384, "path", this.pathRaw);
			} else {
				this.path = "";
			}
			this.schemeSpecificPart = this.path;
		}
		if (this.queryRaw != null) {
			this.query = this.uriProfile.validate_decode(32768, "query", this.queryRaw);
			this.schemeSpecificPart = this.schemeSpecificPart + '?' + this.query;
		}
		if (this.fragmentRaw != null) {
			this.fragment = this.uriProfile.validate_decode(65536, "fragment", this.fragmentRaw);
		}
	}

	public boolean isAbsolute() {
		return this.bAbsolute;
	}

	public boolean isOpaque() {
		return this.bOpaque;
	}

	public String getScheme() {
		return this.scheme;
	}

	public String getRawSchemeSpecificPart() {
		return this.schemeSpecificPartRaw;
	}

	public String getSchemeSpecificPart() {
		return this.schemeSpecificPart;
	}

	public String getRawAuthority() {
		return this.authorityRaw;
	}

	public String getAuthority() {
		return this.authority;
	}

	public String getRawUserInfo() {
		return this.userinfoRaw;
	}

	public String getUserInfo() {
		return this.userinfo;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getRawPath() {
		return this.pathRaw;
	}

	public String getPath() {
		return this.path;
	}

	public String getRawQuery() {
		return this.queryRaw;
	}

	public String getQuery() {
		return this.query;
	}

	public String getRawFragment() {
		return this.fragmentRaw;
	}

	public String getFragment() {
		return this.fragment;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (this.scheme != null) {
			sb.append(this.scheme);
			sb.append(':');
		}
		if (this.hostRaw != null) {
			sb.append("//");
			if (this.userinfoRaw != null) {
				sb.append(this.userinfoRaw);
				sb.append('@');
			}
			sb.append(this.hostRaw);
			if (this.portRaw != null) {
				sb.append(':');
				sb.append(this.portRaw);
			}
		}
		sb.append(this.pathRaw);
		if (this.queryRaw != null) {
			sb.append('?');
			sb.append(this.queryRaw);
		}
		if (this.fragmentRaw != null) {
			sb.append('#');
			sb.append(this.fragmentRaw);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Uri)) {
			return false;
		}
		final Uri uriObj = (Uri) obj;
		if (this.scheme != null) {
			if (!this.scheme.equals(uriObj.scheme)) {
				return false;
			}
		} else if (uriObj.scheme != null) {
			return false;
		}
		if (this.userinfo != null) {
			if (!this.userinfo.equals(uriObj.userinfo)) {
				return false;
			}
		} else if (uriObj.userinfo != null) {
			return false;
		}
		if (this.host != null) {
			if (!this.host.equals(uriObj.host)) {
				return false;
			}
		} else if (uriObj.host != null) {
			return false;
		}
		if (this.port != uriObj.port) {
			return false;
		}
		if (!this.path.equals(uriObj.path)) {
			return false;
		}
		if (this.query != null) {
			if (!this.query.equals(uriObj.query)) {
				return false;
			}
		} else if (uriObj.query != null) {
			return false;
		}
		if (this.fragment != null) {
			if (!this.fragment.equals(uriObj.fragment)) {
				return false;
			}
		} else if (uriObj.fragment != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.scheme != null) {
			hashCode ^= this.scheme.hashCode();
		}
		hashCode ^= this.schemeSpecificPart.hashCode();
		if (this.fragment != null) {
			hashCode ^= this.fragment.hashCode();
		}
		return hashCode;
	}

	@Override
	public int compareTo(final Uri uri) {
		if (uri == null) {
			return 1;
		}
		int res = 0;
		if (this.scheme != null) {
			if (uri.scheme == null) {
				return 1;
			}
			res = this.scheme.compareTo(uri.scheme);
			if (res != 0) {
				return res;
			}
		} else if (uri.scheme != null) {
			return -1;
		}
		res = this.schemeSpecificPart.compareTo(uri.schemeSpecificPart);
		if (res != 0) {
			return res;
		}
		if (this.fragment != null) {
			if (uri.fragment == null) {
				return 1;
			}
			res = this.fragment.compareTo(uri.fragment);
			if (res != 0) {
				return res;
			}
		} else if (uri.fragment != null) {
			return -1;
		}
		return res;
	}
}