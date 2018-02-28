/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.TreeMap;
import java.util.zip.Checksum;
import java.util.zip.CRC32;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Map;

public class Digest {
	public String algorithm;
	public byte[] digestBytes;
	public String digestString;
	public String encoding;
	protected static Map<String, Integer> digestAlgoLengthache;

	public static synchronized int digestAlgorithmLength(final String digestAlgorithm) {
		if (digestAlgorithm == null || digestAlgorithm.length() == 0) {
			throw new IllegalArgumentException("'digestAlgorithm' is empty or null");
		}
		Integer cachedLen = Digest.digestAlgoLengthache.get(digestAlgorithm);
		if (cachedLen == null) {
			try {
				final MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
				final byte[] digest = md.digest(new byte[16]);
				cachedLen = digest.length;
				md.reset();
			} catch (NoSuchAlgorithmException ex) {
			}
			if (cachedLen == null) {
				cachedLen = -1;
			}
			Digest.digestAlgoLengthache.put(digestAlgorithm, cachedLen);
		}
		return cachedLen;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Digest)) {
			return false;
		}
		final Digest digestObj = (Digest) obj;
		if (!Arrays.equals(this.digestBytes, digestObj.digestBytes)) {
			return false;
		}
		if (this.algorithm != null) {
			if (!this.algorithm.equals(digestObj.algorithm)) {
				return false;
			}
		} else if (digestObj.algorithm != null) {
			return false;
		}
		if (this.digestString != null) {
			if (!this.digestString.equals(digestObj.digestString)) {
				return false;
			}
		} else if (digestObj.digestString != null) {
			return false;
		}
		if (this.encoding != null) {
			if (!this.encoding.equals(digestObj.encoding)) {
				return false;
			}
		} else if (digestObj.encoding != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.digestBytes != null) {
			final Checksum checksum = new CRC32();
			checksum.update(this.digestBytes, 0, this.digestBytes.length);
			hashCode ^= (int) checksum.getValue();
		}
		if (this.algorithm != null) {
			hashCode ^= this.algorithm.hashCode();
		}
		if (this.digestString != null) {
			hashCode ^= this.digestString.hashCode();
		}
		if (this.encoding != null) {
			hashCode ^= this.encoding.hashCode();
		}
		return hashCode;
	}

	static {
		Digest.digestAlgoLengthache = new TreeMap<String, Integer>();
	}
}