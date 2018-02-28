/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

public interface WarcFileNaming {
	boolean supportMultipleFiles();

	String getFilename(final int p0, final boolean p1);
}