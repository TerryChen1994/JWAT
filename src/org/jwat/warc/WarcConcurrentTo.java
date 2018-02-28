/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import org.jwat.common.Uri;

public class WarcConcurrentTo {
	public String warcConcurrentToStr;
	public Uri warcConcurrentToUri;

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WarcConcurrentTo)) {
			return false;
		}
		final WarcConcurrentTo concurrentToObj = (WarcConcurrentTo) obj;
		if (this.warcConcurrentToStr != null) {
			if (!this.warcConcurrentToStr.equals(concurrentToObj.warcConcurrentToStr)) {
				return false;
			}
		} else if (concurrentToObj.warcConcurrentToStr != null) {
			return false;
		}
		if (this.warcConcurrentToUri != null) {
			if (!this.warcConcurrentToUri.equals((Object) concurrentToObj.warcConcurrentToUri)) {
				return false;
			}
		} else if (concurrentToObj.warcConcurrentToUri != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.warcConcurrentToStr != null) {
			hashCode ^= this.warcConcurrentToStr.hashCode();
		}
		if (this.warcConcurrentToUri != null) {
			hashCode ^= this.warcConcurrentToUri.hashCode();
		}
		return hashCode;
	}
}