/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Diagnostics<T> {
	protected List<T> errors;
	protected List<T> warnings;

	public Diagnostics() {
		this.errors = new LinkedList<T>();
		this.warnings = new LinkedList<T>();
	}

	public void reset() {
		this.errors.clear();
		this.warnings.clear();
	}

	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public boolean hasWarnings() {
		return !this.warnings.isEmpty();
	}

	public void addAll(final Diagnostics<T> diagnostics) {
		if (diagnostics != null && diagnostics != this) {
			this.errors.addAll((Collection<? extends T>) diagnostics.errors);
			this.warnings.addAll((Collection<? extends T>) diagnostics.warnings);
		}
	}

	public void addError(final T d) {
		this.errors.add(d);
	}

	public void addWarning(final T d) {
		this.warnings.add(d);
	}

	public List<T> getErrors() {
		return Collections.unmodifiableList((List<? extends T>) this.errors);
	}

	public List<T> getWarnings() {
		return Collections.unmodifiableList((List<? extends T>) this.warnings);
	}
}