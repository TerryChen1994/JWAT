/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.io.IOException;

public interface PayloadOnClosedHandler {
	void payloadClosed() throws IOException;
}