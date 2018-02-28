/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

public class Diagnosis {
	public final DiagnosisType type;
	public final String entity;
	public final String[] information;

	public Diagnosis(final DiagnosisType type, final String entity, final String... information) {
		if (type == null) {
			throw new IllegalArgumentException("'type' is null!");
		}
		if (entity == null) {
			throw new IllegalArgumentException("'entity' is null!");
		}
		this.type = type;
		this.entity = entity;
		this.information = information;
		if (type.expected_information > 0 && (information == null || information.length < type.expected_information)) {
			throw new IllegalArgumentException("Missing information!");
		}
	}

	public Object[] getMessageArgs() {
		final Object[] messageArgs = new Object[this.information.length + 1];
		messageArgs[0] = this.entity;
		if (this.information.length > 0) {
			System.arraycopy(this.information, 0, messageArgs, 1, this.information.length);
		}
		return messageArgs;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Diagnosis)) {
			return false;
		}
		final Diagnosis diagnosisObj = (Diagnosis) obj;
		if (!this.type.equals((Object) diagnosisObj.type)) {
			return false;
		}
		if (!this.entity.equals(diagnosisObj.entity)) {
			return false;
		}
		if (this.information != null && diagnosisObj.information != null) {
			if (this.information.length != diagnosisObj.information.length) {
				return false;
			}
			for (int i = 0; i < this.information.length; ++i) {
				if (this.information[i] != null) {
					if (!this.information[i].equals(diagnosisObj.information[i])) {
						return false;
					}
				} else if (diagnosisObj.information[i] != null) {
					return false;
				}
			}
		} else if (this.information != null || diagnosisObj.information != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = this.type.hashCode();
		hashCode ^= this.entity.hashCode();
		if (this.information != null) {
			hashCode ^= 0x7A63;
			for (int i = 0; i < this.information.length; ++i) {
				if (this.information[i] != null) {
					hashCode ^= this.information[i].hashCode();
				}
			}
		}
		return hashCode;
	}
}