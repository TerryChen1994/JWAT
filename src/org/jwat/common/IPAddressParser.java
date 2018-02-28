/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.common;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.regex.Pattern;

public final class IPAddressParser {
	private static final String IP_ADDRESS_REG_EXP = "([0-9a-fA-F]{0,4}:){0,6}(([0-9a-fA-F]{0,4}:[0-9a-fA-F]{1,4})|(([0-9]{1,3}\\.){3}[0-9]{1,3}))";
	private static final Pattern IP_ADDRESS_PATTERN;

	public static InetAddress getAddress(final String ipAddress) {
		boolean isValid = ipAddress != null && IPAddressParser.IP_ADDRESS_PATTERN.matcher(ipAddress).matches();
		InetAddress inetAddress = null;
		if (isValid) {
			try {
				inetAddress = InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e) {
				isValid = false;
			}
		}
		return isValid ? inetAddress : null;
	}

	static {
		IP_ADDRESS_PATTERN = Pattern.compile(
				"([0-9a-fA-F]{0,4}:){0,6}(([0-9a-fA-F]{0,4}:[0-9a-fA-F]{1,4})|(([0-9]{1,3}\\.){3}[0-9]{1,3}))");
	}
}