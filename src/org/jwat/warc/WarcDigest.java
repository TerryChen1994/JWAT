/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import org.jwat.common.Digest;

public class WarcDigest extends Digest {
	protected WarcDigest() {
	}

	protected WarcDigest(final String algorithm, final String digestValue) {
		this.algorithm = algorithm;
		this.digestString = digestValue;
	}

	public static WarcDigest parseWarcDigest(final String labelledDigest) {
		if (labelledDigest == null || labelledDigest.length() == 0) {
			return null;
		}
		final int cIdx = labelledDigest.indexOf(58);
		if (cIdx != -1) {
			final String algorithm = labelledDigest.substring(0, cIdx).trim().toLowerCase();
			final String digestValue = labelledDigest.substring(cIdx + 1).trim();
			if (algorithm.length() > 0 && digestValue.length() > 0) {
				return new WarcDigest(algorithm, digestValue);
			}
		}
		return null;
	}

	public static WarcDigest createWarcDigest(final String algorithm, final byte[] digestBytes, final String encoding,
			final String digestValue) {
		if (algorithm == null || algorithm.length() == 0) {
			throw new IllegalArgumentException("'algorithm' is empty or null");
		}
		if (digestBytes == null || digestBytes.length == 0) {
			throw new IllegalArgumentException("'digestBytes' is empty or null");
		}
		if (encoding == null || encoding.length() == 0) {
			throw new IllegalArgumentException("'encoding' is empty or null");
		}
		if (digestValue == null || digestValue.length() == 0) {
			throw new IllegalArgumentException("'digestValue' is empty or null");
		}
		final WarcDigest digest = new WarcDigest();
		digest.algorithm = algorithm.toLowerCase();
		digest.digestBytes = digestBytes;
		digest.encoding = encoding.toLowerCase();
		digest.digestString = digestValue;
		return digest;
	}

	public String toString() {
		return this.algorithm + ":" + this.digestString;
	}

	public String toStringFull() {
		return this.algorithm + ":" + this.encoding + ":" + this.digestString;
	}
}