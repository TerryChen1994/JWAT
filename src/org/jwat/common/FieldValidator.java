/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.HashMap;
import java.util.Map;

public class FieldValidator {
	protected String[] fieldNames;
	protected Map<String, Integer> fieldIdxMap;

	protected FieldValidator() {
		this.fieldIdxMap = new HashMap<String, Integer>();
	}

	public static FieldValidator prepare(final String[] fieldNames) {
		final FieldValidator fv = new FieldValidator();
		fv.fieldNames = fieldNames;
		for (int i = 0; i < fieldNames.length; ++i) {
			fv.fieldIdxMap.put(fieldNames[i], i);
		}
		return fv;
	}

	public static String getArrayValue(final String[] array, final int idx) {
		return (array.length > idx && array[idx] != null) ? array[idx] : null;
	}
}