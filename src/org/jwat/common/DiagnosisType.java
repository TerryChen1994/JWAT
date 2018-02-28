/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

public enum DiagnosisType {
	DUPLICATE(1), EMPTY(0), ERROR(1), ERROR_EXPECTED(1), INVALID(0), INVALID_DATA(1), INVALID_ENCODING(
			2), INVALID_EXPECTED(2), RECOMMENDED(2), RECOMMENDED_MISSING(
					0), REQUIRED_INVALID(1), REQUIRED_MISSING(0), RESERVED(1), UNDESIRED_DATA(1), UNKNOWN(1);

	public final int expected_information;

	private DiagnosisType(final int expected_information) {
		this.expected_information = expected_information;
	}
}