/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.LinkedList;
import java.util.List;

public class ArrayUtils {
	protected static final byte[] CASE_SENSITIVE;
	protected static final byte[] CASE_INSENSITIVE;
	public static final byte[] SKIP_WHITESPACE;
	public static final byte[] SKIP_NONWHITESPACE;
	protected static final byte[] zeroArr;

	public static int skip(final byte[] skip, final byte[] arr, int fIdx) {
		for (int arrLen = arr.length; fIdx < arrLen && skip[arr[fIdx] & 0xFF] == 1; ++fIdx) {
		}
		return fIdx;
	}

	public static boolean startsWith(final byte[] subArr, final byte[] arr) {
		boolean bRes = false;
		int lIdx = subArr.length - 1;
		if (lIdx < arr.length && subArr[0] == arr[0]) {
			while (lIdx > 0 && subArr[lIdx] == arr[lIdx]) {
				--lIdx;
			}
			bRes = (lIdx == 0);
		}
		return bRes;
	}

	public static boolean startsWithIgnoreCase(final byte[] subArr, final byte[] arr) {
		boolean bRes = false;
		int lIdx = subArr.length - 1;
		if (lIdx < arr.length && ArrayUtils.CASE_INSENSITIVE[subArr[0]] == ArrayUtils.CASE_INSENSITIVE[arr[0]]) {
			while (lIdx > 0 && ArrayUtils.CASE_INSENSITIVE[subArr[lIdx]] == ArrayUtils.CASE_INSENSITIVE[arr[lIdx]]) {
				--lIdx;
			}
			bRes = (lIdx == 0);
		}
		return bRes;
	}

	public static boolean equalsAt(final byte[] subArr, final byte[] arr, final int fIdx) {
		boolean bRes = false;
		int lIdx = subArr.length - 1;
		int tIdx = fIdx + lIdx;
		if (tIdx < arr.length && subArr[0] == arr[fIdx]) {
			while (lIdx > 0 && subArr[lIdx] == arr[tIdx]) {
				--lIdx;
				--tIdx;
			}
			bRes = (lIdx == 0);
		}
		return bRes;
	}

	public static boolean equalsAtIgnoreCase(final byte[] subArr, final byte[] arr, final int fIdx) {
		boolean bRes = false;
		int lIdx = subArr.length - 1;
		int tIdx = fIdx + lIdx;
		if (tIdx < arr.length
				&& ArrayUtils.CASE_INSENSITIVE[subArr[0] & 0xFF] == ArrayUtils.CASE_INSENSITIVE[arr[fIdx] & 0xFF]) {
			while (lIdx > 0 && ArrayUtils.CASE_INSENSITIVE[subArr[lIdx] & 0xFF] == ArrayUtils.CASE_INSENSITIVE[arr[tIdx]
					& 0xFF]) {
				--lIdx;
				--tIdx;
			}
			bRes = (lIdx == 0);
		}
		return bRes;
	}

	public static int indexOf(final byte[] subArr, final byte[] arr, int fIdx) {
		int idx = -1;
		final int subArrLast = subArr.length - 1;
		final int arrLen = arr.length;
		int lIdx = fIdx + subArrLast;
		if (subArrLast > 0) {
			while (lIdx < arrLen && idx == -1) {
				if (subArr[0] == arr[fIdx]) {
					int csIdx = subArrLast;
					for (int caIdx = lIdx; csIdx > 0 && subArr[csIdx] == arr[caIdx]; --csIdx, --caIdx) {
					}
					if (csIdx == 0) {
						idx = fIdx;
					}
				}
				++fIdx;
				++lIdx;
			}
		} else if (subArrLast == 0) {
			while (fIdx < arrLen && idx == -1) {
				if (subArr[0] == arr[fIdx]) {
					idx = fIdx;
				}
				++fIdx;
			}
		}
		return idx;
	}

	public static int indexOfIgnoreCase(final byte[] subArr, final byte[] arr, int fIdx) {
		int idx = -1;
		final int subArrLast = subArr.length - 1;
		final int arrLen = arr.length;
		int lIdx = fIdx + subArrLast;
		if (subArrLast > 0) {
			while (lIdx < arrLen && idx == -1) {
				if (ArrayUtils.CASE_INSENSITIVE[subArr[0]] == ArrayUtils.CASE_INSENSITIVE[arr[fIdx]]) {
					int csIdx = subArrLast;
					for (int caIdx = lIdx; csIdx > 0
							&& ArrayUtils.CASE_INSENSITIVE[subArr[csIdx]] == ArrayUtils.CASE_INSENSITIVE[arr[caIdx]]; --csIdx, --caIdx) {
					}
					if (csIdx == 0) {
						idx = fIdx;
					}
				}
				++fIdx;
				++lIdx;
			}
		} else if (subArrLast == 0) {
			while (fIdx < arrLen && idx == -1) {
				if (ArrayUtils.CASE_INSENSITIVE[subArr[0]] == ArrayUtils.CASE_INSENSITIVE[arr[fIdx]]) {
					idx = fIdx;
				}
				++fIdx;
			}
		}
		return idx;
	}

	public static List<byte[]> split(final byte[] arr, final byte[] subArr, int fIdx, int tIdx) {
		final List<byte[]> list = new LinkedList<byte[]>();
		final int subArrLen = subArr.length;
		final int subArrLast = subArrLen - 1;
		if (arr.length < tIdx) {
			tIdx = arr.length;
		}
		if (fIdx > tIdx) {
			throw new IllegalArgumentException("Reverse interval!");
		}
		int lIdx = fIdx + subArrLast;
		int pIdx = fIdx;
		if (subArrLast > 0) {
			while (lIdx < tIdx) {
				if (subArr[0] == arr[fIdx]) {
					int csIdx = subArrLast;
					for (int caIdx = lIdx; csIdx > 0 && subArr[csIdx] == arr[caIdx]; --csIdx, --caIdx) {
					}
					if (csIdx == 0) {
						final byte[] tmpArr = new byte[fIdx - pIdx];
						System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
						list.add(tmpArr);
						fIdx += subArrLen;
						lIdx += subArrLen;
						pIdx = fIdx;
					} else {
						++fIdx;
						++lIdx;
					}
				} else {
					++fIdx;
					++lIdx;
				}
			}
			if (pIdx < tIdx) {
				final byte[] tmpArr = new byte[tIdx - pIdx];
				System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
				list.add(tmpArr);
			}
		} else {
			while (fIdx < tIdx) {
				if (subArr[0] == arr[fIdx]) {
					final byte[] tmpArr = new byte[fIdx - pIdx];
					System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
					list.add(tmpArr);
					pIdx = ++fIdx;
				} else {
					++fIdx;
				}
			}
			if (pIdx < tIdx) {
				final byte[] tmpArr = new byte[tIdx - pIdx];
				System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
				list.add(tmpArr);
			}
		}
		if (pIdx == fIdx) {
			list.add(ArrayUtils.zeroArr);
		}
		return list;
	}

	static {
		CASE_SENSITIVE = new byte[256];
		CASE_INSENSITIVE = new byte[256];
		SKIP_WHITESPACE = new byte[256];
		SKIP_NONWHITESPACE = new byte[256];
		zeroArr = new byte[0];
		for (int i = 0; i < 256; ++i) {
			ArrayUtils.CASE_SENSITIVE[i] = (byte) i;
			ArrayUtils.CASE_INSENSITIVE[i] = (byte) Character.toLowerCase(i);
			ArrayUtils.SKIP_WHITESPACE[i] = 0;
			ArrayUtils.SKIP_NONWHITESPACE[i] = 1;
		}
		ArrayUtils.SKIP_WHITESPACE[32] = 1;
		ArrayUtils.SKIP_WHITESPACE[9] = 1;
		ArrayUtils.SKIP_WHITESPACE[13] = 1;
		ArrayUtils.SKIP_WHITESPACE[10] = 1;
		ArrayUtils.SKIP_NONWHITESPACE[32] = 0;
		ArrayUtils.SKIP_NONWHITESPACE[9] = 0;
		ArrayUtils.SKIP_NONWHITESPACE[13] = 0;
		ArrayUtils.SKIP_NONWHITESPACE[10] = 0;
	}
}