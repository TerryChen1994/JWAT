/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;

public class NewlineParser {
	public boolean bMissingCr;
	public boolean bMissingLf;
	public boolean bMisplacedCr;
	public boolean bMisplacedLf;

	public NewlineParser() {
		this.bMissingCr = false;
		this.bMissingLf = false;
		this.bMisplacedCr = false;
		this.bMisplacedLf = false;
	}

	public int parseLFs(final ByteCountingPushBackInputStream in, final Diagnostics<Diagnosis> diagnostics)
			throws IOException {
		this.bMissingCr = false;
		this.bMissingLf = false;
		this.bMisplacedCr = false;
		this.bMisplacedLf = false;
		int newlines = 0;
		final byte[] buffer = new byte[2];
		boolean bLoop = true;
		while (bLoop) {
			final int read = in.read(buffer);
			switch (read) {
			case 1: {
				if (buffer[0] == 10) {
					++newlines;
					continue;
				}
				if (buffer[0] == 13) {
					++newlines;
					this.bMissingLf = true;
					this.bMisplacedCr = true;
					continue;
				}
				in.unread((int) buffer[0]);
				bLoop = false;
				continue;
			}
			case 2: {
				if (buffer[0] == 10) {
					if (buffer[1] == 10) {
						newlines += 2;
						continue;
					}
					if (buffer[1] != 13) {
						++newlines;
						in.unread((int) buffer[1]);
						continue;
					}
					++newlines;
					this.bMisplacedCr = true;
					continue;
				} else {
					if (buffer[0] != 13) {
						in.unread(buffer);
						bLoop = false;
						continue;
					}
					if (buffer[1] == 10) {
						++newlines;
						this.bMisplacedCr = true;
						this.bMisplacedLf = true;
						continue;
					}
					++newlines;
					this.bMisplacedCr = true;
					this.bMissingLf = true;
					in.unread((int) buffer[1]);
					continue;
				}
			}
			default: {
				bLoop = false;
				continue;
			}
			}
		}
		if (diagnostics != null) {
			if (this.bMissingLf) {
				diagnostics.addWarning(
						new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Missing LF", new String[] { "Sequence of LFs" }));
			}

			if (this.bMisplacedCr) {
				diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Misplaced CR",
						new String[] { "Sequence of LFs" }));
			}

			if (this.bMisplacedLf) {
				diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Misplaced LF",
						new String[] { "Sequence of LFs" }));
			}
		}
		return newlines;
	}

	public int parseCRLFs(final ByteCountingPushBackInputStream in, final Diagnostics<Diagnosis> diagnostics)
			throws IOException {
		this.bMissingCr = false;
		this.bMissingLf = false;
		this.bMisplacedCr = false;
		this.bMisplacedLf = false;
		int newlines = 0;
		final byte[] buffer = new byte[2];
		boolean bLoop = true;
		while (bLoop) {
			final int read = in.read(buffer);
			switch (read) {
			case 1: {
				if (buffer[0] == 10) {
					++newlines;
					this.bMissingCr = true;
					continue;
				}
				if (buffer[0] == 13) {
					++newlines;
					this.bMissingLf = true;
					continue;
				}
				in.unread((int) buffer[0]);
				bLoop = false;
				continue;
			}
			case 2: {
				if (buffer[0] == 13) {
					if (buffer[1] == 10) {
						++newlines;
						continue;
					}
					++newlines;
					this.bMissingLf = true;
					in.unread((int) buffer[1]);
					continue;
				} else {
					if (buffer[0] != 10) {
						in.unread(buffer);
						bLoop = false;
						continue;
					}
					if (buffer[1] == 13) {
						++newlines;
						this.bMisplacedCr = true;
						this.bMisplacedLf = true;
						continue;
					}
					++newlines;
					this.bMissingCr = true;
					in.unread((int) buffer[1]);
					continue;
				}
			}
			default: {
				bLoop = false;
				continue;
			}
			}
		}
		if (diagnostics != null) {
			if (this.bMissingCr) {
				diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Missing CR",
						new String[] { "Sequence of CRLFs" }));
			}

			if (this.bMissingLf) {
				diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Missing LF",
						new String[] { "Sequence of CRLFs" }));
			}

			if (this.bMisplacedCr) {
				diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Misplaced CR",
						new String[] { "Sequence of CRLFs" }));
			}

			if (this.bMisplacedLf) {
				diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "Misplaced LF",
						new String[] { "Sequence of CRLFs" }));
			}
		}
		return newlines;
	}
}