/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.io.OutputStream;

public class WarcWriterFactory {
	public static WarcWriter getWriter(final OutputStream out, final boolean compressed) {
		if (out == null) {
			throw new IllegalArgumentException("The 'out' parameter is null!");
		}
//		if (compressed) {
//			return (WarcWriter) new WarcWriterCompressed(out);
//		}
		return (WarcWriter) new WarcWriterUncompressed(out);
	}

	public static WarcWriter getWriter(final OutputStream out, final int buffer_size, final boolean compressed) {
		if (out == null) {
			throw new IllegalArgumentException("The 'out' parameter is null!");
		}
		if (buffer_size <= 0) {
			throw new IllegalArgumentException("The 'buffer_size' parameter is less than or equal to zero!");
		}
//		if (compressed) {
//			return (WarcWriter) new WarcWriterCompressed(out, buffer_size);
//		}
		return (WarcWriter) new WarcWriterUncompressed(out, buffer_size);
	}

	public static WarcWriter getWriterUncompressed(final OutputStream out) {
		if (out == null) {
			throw new IllegalArgumentException("The 'out' parameter is null!");
		}
		return (WarcWriter) new WarcWriterUncompressed(out);
	}

	public static WarcWriter getWriterUncompressed(final OutputStream out, final int buffer_size) {
		if (out == null) {
			throw new IllegalArgumentException("The 'out' parameter is null!");
		}
		if (buffer_size <= 0) {
			throw new IllegalArgumentException("The 'buffer_size' parameter is less than or equal to zero!");
		}
		return (WarcWriter) new WarcWriterUncompressed(out, buffer_size);
	}

//	public static WarcWriter getWriterCompressed(final OutputStream out) {
//		if (out == null) {
//			throw new IllegalArgumentException("The 'out' parameter is null!");
//		}
//		return (WarcWriter) new WarcWriterCompressed(out);
//	}

//	public static WarcWriter getWriterCompressed(final OutputStream out, final int buffer_size) {
//		if (out == null) {
//			throw new IllegalArgumentException("The 'out' parameter is null!");
//		}
//		if (buffer_size <= 0) {
//			throw new IllegalArgumentException("The 'buffer_size' parameter is less than or equal to zero!");
//		}
//		return (WarcWriter) new WarcWriterCompressed(out, buffer_size);
//	}
}