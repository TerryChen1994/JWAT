/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.jwat.warc;

import java.util.HashMap;
import java.util.Map;

public class WarcConstants {
	public static final String WARC_MAGIC_HEADER = "WARC/";
	protected static byte[] endMark;
	public static final String WARC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String WARC_DIGEST_FORMAT = "<digest-algorithm>:<digest-encoded>";
	public static final String CONTENT_TYPE_FORMAT = "<type>/<sub-type>(; <argument>=<value>)*";
	public static final String WARC_MIME_TYPE = "application/warc";
	public static final String CT_APP_WARC_FIELDS = "application/warc-fields";
	public static final String CONTENT_TYPE_METADATA = "application";
	public static final String MEDIA_TYPE_METADATA = "warc-fields";
	public static final int WARC_RECORD_TRAILING_NEWLINES = 2;
	public static final int FN_NUMBER = 21;
	public static final int FN_INDEX_OF_LAST = 22;
	public static final int RT_NUMBER = 8;
	public static final int RT_INDEX_OF_LAST = 9;
	public static final String FN_WARC_TYPE = "WARC-Type";
	public static final String FN_WARC_RECORD_ID = "WARC-Record-ID";
	public static final String FN_WARC_DATE = "WARC-Date";
	public static final String FN_CONTENT_LENGTH = "Content-Length";
	public static final String FN_CONTENT_TYPE = "Content-Type";
	public static final String FN_WARC_CONCURRENT_TO = "WARC-Concurrent-To";
	public static final String FN_WARC_BLOCK_DIGEST = "WARC-Block-Digest";
	public static final String FN_WARC_PAYLOAD_DIGEST = "WARC-Payload-Digest";
	public static final String FN_WARC_IP_ADDRESS = "WARC-IP-Address";
	public static final String FN_WARC_REFERS_TO = "WARC-Refers-To";
	public static final String FN_WARC_TARGET_URI = "WARC-Target-URI";
	public static final String FN_WARC_TRUNCATED = "WARC-Truncated";
	public static final String FN_WARC_WARCINFO_ID = "WARC-Warcinfo-ID";
	public static final String FN_WARC_FILENAME = "WARC-Filename";
	public static final String FN_WARC_PROFILE = "WARC-Profile";
	public static final String FN_WARC_IDENTIFIED_PAYLOAD_TYPE = "WARC-Identified-Payload-Type";
	public static final String FN_WARC_SEGMENT_ORIGIN_ID = "WARC-Segment-Origin-ID";
	public static final String FN_WARC_SEGMENT_NUMBER = "WARC-Segment-Number";
	public static final String FN_WARC_SEGMENT_TOTAL_LENGTH = "WARC-Segment-Total-Length";
	public static final String FN_WARC_REFERS_TO_TARGET_URI = "WARC-Refers-To-Target-URI";
	public static final String FN_WARC_REFERS_TO_DATE = "WARC-Refers-To-Date";
	public static final String[] FN_IDX_STRINGS;
	public static final int FN_IDX_WARC_TYPE = 1;
	public static final int FN_IDX_WARC_RECORD_ID = 2;
	public static final int FN_IDX_WARC_DATE = 3;
	public static final int FN_IDX_CONTENT_LENGTH = 4;
	public static final int FN_IDX_CONTENT_TYPE = 5;
	public static final int FN_IDX_WARC_CONCURRENT_TO = 6;
	public static final int FN_IDX_WARC_BLOCK_DIGEST = 7;
	public static final int FN_IDX_WARC_PAYLOAD_DIGEST = 8;
	public static final int FN_IDX_WARC_IP_ADDRESS = 9;
	public static final int FN_IDX_WARC_REFERS_TO = 10;
	public static final int FN_IDX_WARC_TARGET_URI = 11;
	public static final int FN_IDX_WARC_TRUNCATED = 12;
	public static final int FN_IDX_WARC_WARCINFO_ID = 13;
	public static final int FN_IDX_WARC_FILENAME = 14;
	public static final int FN_IDX_WARC_PROFILE = 15;
	public static final int FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE = 16;
	public static final int FN_IDX_WARC_SEGMENT_ORIGIN_ID = 17;
	public static final int FN_IDX_WARC_SEGMENT_NUMBER = 18;
	public static final int FN_IDX_WARC_SEGMENT_TOTAL_LENGTH = 19;
	public static final int FN_IDX_WARC_REFERS_TO_TARGET_URI = 20;
	public static final int FN_IDX_WARC_REFERS_TO_DATE = 21;
	public static final Map<String, Integer> fieldNameIdxMap;
	public static final int FDT_STRING = 0;
	public static final int FDT_INTEGER = 1;
	public static final int FDT_LONG = 2;
	public static final int FDT_DIGEST = 3;
	public static final int FDT_CONTENTTYPE = 4;
	public static final int FDT_DATE = 5;
	public static final int FDT_INETADDRESS = 6;
	public static final int FDT_URI = 7;
	public static final String[] FDT_IDX_STRINGS;
	public static final int[] FN_IDX_DT;
	public static final boolean[] fieldNamesRepeatableLookup;
	public static final String RT_WARCINFO = "warcinfo";
	public static final String RT_RESPONSE = "response";
	public static final String RT_RESOURCE = "resource";
	public static final String RT_REQUEST = "request";
	public static final String RT_METADATA = "metadata";
	public static final String RT_REVISIT = "revisit";
	public static final String RT_CONVERSION = "conversion";
	public static final String RT_CONTINUATION = "continuation";
	public static final String[] RT_IDX_STRINGS;
	public static final int RT_IDX_UNKNOWN = 0;
	public static final int RT_IDX_WARCINFO = 1;
	public static final int RT_IDX_RESPONSE = 2;
	public static final int RT_IDX_RESOURCE = 3;
	public static final int RT_IDX_REQUEST = 4;
	public static final int RT_IDX_METADATA = 5;
	public static final int RT_IDX_REVISIT = 6;
	public static final int RT_IDX_CONVERSION = 7;
	public static final int RT_IDX_CONTINUATION = 8;
	public static final Map<String, Integer> recordTypeIdxMap;
	public static final String TT_LENGTH = "length";
	public static final String TT_TIME = "time";
	public static final String TT_DISCONNECT = "disconnect";
	public static final String TT_UNSPECIFIED = "unspecified";
	public static final String[] TT_IDX_STRINGS;
	public static final int TT_IDX_FUTURE_REASON = 0;
	public static final int TT_IDX_LENGTH = 1;
	public static final int TT_IDX_TIME = 2;
	public static final int TT_IDX_DISCONNECT = 3;
	public static final int TT_IDX_UNSPECIFIED = 4;
	public static final Map<String, Integer> truncatedTypeIdxMap;
	public static final String PROFILE_IDENTICAL_PAYLOAD_DIGEST = "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest";
	public static final String PROFILE_SERVER_NOT_MODIFIED = "http://netpreserve.org/warc/1.0/revisit/server-not-modified";
	public static final String[] P_IDX_STRINGS;
	public static final int PROFILE_IDX_UNKNOWN = 0;
	public static final int PROFILE_IDX_IDENTICAL_PAYLOAD_DIGEST = 1;
	public static final int PROFILE_IDX_SERVER_NOT_MODIFIED = 2;
	public static final Map<String, Integer> profileIdxMap;
	public static final int POLICY_IGNORE = 0;
	public static final int POLICY_MANDATORY = 1;
	public static final int POLICY_SHALL = 2;
	public static final int POLICY_SHALL_NOT = 3;
	public static final int POLICY_MAY = 4;
	public static final int POLICY_MAY_NOT = 5;
	public static final int[][] field_policy;

	static {
		WarcConstants.endMark = "\r\n\r\n".getBytes();
		FN_IDX_STRINGS = new String[] { null, "WARC-Type", "WARC-Record-ID", "WARC-Date", "Content-Length",
				"Content-Type", "WARC-Concurrent-To", "WARC-Block-Digest", "WARC-Payload-Digest", "WARC-IP-Address",
				"WARC-Refers-To", "WARC-Target-URI", "WARC-Truncated", "WARC-Warcinfo-ID", "WARC-Filename",
				"WARC-Profile", "WARC-Identified-Payload-Type", "WARC-Segment-Origin-ID", "WARC-Segment-Number",
				"WARC-Segment-Total-Length", "WARC-Refers-To-Target-URI", "WARC-Refers-To-Date" };
		(fieldNameIdxMap = new HashMap<String, Integer>()).put("WARC-Type".toLowerCase(), 1);
		WarcConstants.fieldNameIdxMap.put("WARC-Record-ID".toLowerCase(), 2);
		WarcConstants.fieldNameIdxMap.put("WARC-Date".toLowerCase(), 3);
		WarcConstants.fieldNameIdxMap.put("Content-Length".toLowerCase(), 4);
		WarcConstants.fieldNameIdxMap.put("Content-Type".toLowerCase(), 5);
		WarcConstants.fieldNameIdxMap.put("WARC-Concurrent-To".toLowerCase(), 6);
		WarcConstants.fieldNameIdxMap.put("WARC-Block-Digest".toLowerCase(), 7);
		WarcConstants.fieldNameIdxMap.put("WARC-Payload-Digest".toLowerCase(), 8);
		WarcConstants.fieldNameIdxMap.put("WARC-IP-Address".toLowerCase(), 9);
		WarcConstants.fieldNameIdxMap.put("WARC-Refers-To".toLowerCase(), 10);
		WarcConstants.fieldNameIdxMap.put("WARC-Target-URI".toLowerCase(), 11);
		WarcConstants.fieldNameIdxMap.put("WARC-Truncated".toLowerCase(), 12);
		WarcConstants.fieldNameIdxMap.put("WARC-Warcinfo-ID".toLowerCase(), 13);
		WarcConstants.fieldNameIdxMap.put("WARC-Filename".toLowerCase(), 14);
		WarcConstants.fieldNameIdxMap.put("WARC-Profile".toLowerCase(), 15);
		WarcConstants.fieldNameIdxMap.put("WARC-Identified-Payload-Type".toLowerCase(), 16);
		WarcConstants.fieldNameIdxMap.put("WARC-Segment-Origin-ID".toLowerCase(), 17);
		WarcConstants.fieldNameIdxMap.put("WARC-Segment-Number".toLowerCase(), 18);
		WarcConstants.fieldNameIdxMap.put("WARC-Segment-Total-Length".toLowerCase(), 19);
		WarcConstants.fieldNameIdxMap.put("WARC-Refers-To-Target-URI".toLowerCase(), 20);
		WarcConstants.fieldNameIdxMap.put("WARC-Refers-To-Date".toLowerCase(), 21);
		FDT_IDX_STRINGS = new String[] { "String", "Integer", "Long", "Digest", "ContentType", "Date", "InetAddress",
				"URI" };
		FN_IDX_DT = new int[] { -1, 0, 7, 5, 2, 4, 7, 3, 3, 6, 7, 7, 0, 7, 0, 7, 4, 7, 1, 2, 7, 5 };
		(fieldNamesRepeatableLookup = new boolean[22])[6] = true;
		RT_IDX_STRINGS = new String[] { null, "warcinfo", "response", "resource", "request", "metadata", "revisit",
				"conversion", "continuation" };
		(recordTypeIdxMap = new HashMap<String, Integer>()).put("warcinfo".toLowerCase(), 1);
		WarcConstants.recordTypeIdxMap.put("response".toLowerCase(), 2);
		WarcConstants.recordTypeIdxMap.put("resource".toLowerCase(), 3);
		WarcConstants.recordTypeIdxMap.put("request".toLowerCase(), 4);
		WarcConstants.recordTypeIdxMap.put("metadata".toLowerCase(), 5);
		WarcConstants.recordTypeIdxMap.put("revisit".toLowerCase(), 6);
		WarcConstants.recordTypeIdxMap.put("conversion".toLowerCase(), 7);
		WarcConstants.recordTypeIdxMap.put("continuation".toLowerCase(), 8);
		TT_IDX_STRINGS = new String[] { null, "length", "time", "disconnect", "unspecified" };
		(truncatedTypeIdxMap = new HashMap<String, Integer>()).put("length".toLowerCase(), 1);
		WarcConstants.truncatedTypeIdxMap.put("time".toLowerCase(), 2);
		WarcConstants.truncatedTypeIdxMap.put("disconnect".toLowerCase(), 3);
		WarcConstants.truncatedTypeIdxMap.put("unspecified".toLowerCase(), 4);
		P_IDX_STRINGS = new String[] { null, "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest",
				"http://netpreserve.org/warc/1.0/revisit/server-not-modified" };
		(profileIdxMap = new HashMap<String, Integer>())
				.put("http://netpreserve.org/warc/1.0/revisit/identical-payload-digest".toLowerCase(), 1);
		WarcConstants.profileIdxMap.put("http://netpreserve.org/warc/1.0/revisit/server-not-modified".toLowerCase(), 2);
		field_policy = new int[9][22];
		for (int i = 0; i <= 8; ++i) {
			WarcConstants.field_policy[i][2] = 1;
			WarcConstants.field_policy[i][1] = 1;
			WarcConstants.field_policy[i][3] = 1;
			WarcConstants.field_policy[i][4] = 1;
		}
		WarcConstants.field_policy[8][5] = 3;
		WarcConstants.field_policy[4][9] = 4;
		WarcConstants.field_policy[2][9] = 4;
		WarcConstants.field_policy[3][9] = 4;
		WarcConstants.field_policy[5][9] = 4;
		WarcConstants.field_policy[6][9] = 4;
		WarcConstants.field_policy[1][9] = 3;
		WarcConstants.field_policy[7][9] = 3;
		WarcConstants.field_policy[8][9] = 3;
		WarcConstants.field_policy[4][6] = 4;
		WarcConstants.field_policy[2][6] = 4;
		WarcConstants.field_policy[3][6] = 4;
		WarcConstants.field_policy[5][6] = 4;
		WarcConstants.field_policy[6][6] = 4;
		WarcConstants.field_policy[1][6] = 3;
		WarcConstants.field_policy[7][6] = 3;
		WarcConstants.field_policy[8][6] = 3;
		WarcConstants.field_policy[5][10] = 4;
		WarcConstants.field_policy[7][10] = 4;
		WarcConstants.field_policy[6][10] = 4;
		WarcConstants.field_policy[1][10] = 3;
		WarcConstants.field_policy[4][10] = 3;
		WarcConstants.field_policy[2][10] = 3;
		WarcConstants.field_policy[3][10] = 3;
		WarcConstants.field_policy[8][10] = 3;
		WarcConstants.field_policy[4][11] = 2;
		WarcConstants.field_policy[2][11] = 2;
		WarcConstants.field_policy[3][11] = 2;
		WarcConstants.field_policy[7][11] = 2;
		WarcConstants.field_policy[8][11] = 2;
		WarcConstants.field_policy[6][11] = 2;
		WarcConstants.field_policy[5][11] = 4;
		WarcConstants.field_policy[1][11] = 3;
		for (int i = 1; i <= 8; ++i) {
			WarcConstants.field_policy[i][13] = 4;
			WarcConstants.field_policy[i][14] = 3;
			WarcConstants.field_policy[i][15] = 0;
			WarcConstants.field_policy[i][17] = 3;
			WarcConstants.field_policy[i][17] = 3;
		}
		WarcConstants.field_policy[1][13] = 5;
		WarcConstants.field_policy[1][14] = 4;
		WarcConstants.field_policy[6][15] = 1;
		WarcConstants.field_policy[8][17] = 1;
		WarcConstants.field_policy[8][18] = 1;
		for (int i = 1; i <= 8; ++i) {
			WarcConstants.field_policy[i][20] = 3;
			WarcConstants.field_policy[i][21] = 3;
		}
		WarcConstants.field_policy[6][20] = 4;
		WarcConstants.field_policy[6][21] = 4;
	}
}