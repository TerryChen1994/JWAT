package org.jwat.main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderUncompressed;
import org.jwat.warc.WarcRecord;

public class JWAT {

//	static String warcFile = "/users/chentairun/downloads/0312wb-00.warc";
	static String warcFile = "/Volumes/Sony_8GP/ClueWeb12_03/0312wb/0312wb-00.warc";
	// static String warcFile = "/home/nicl/Downloads/MYWARC.warc";

	public static void main(String[] args) {
		File file = new File(warcFile);
		try {
			InputStream in = new FileInputStream(file);

			int records = 0;
			
			ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 32);
			
			WarcReader reader = new WarcReaderUncompressed(pbin);
			WarcRecord record;
			
			long sT = System.currentTimeMillis();
			label1 : while ((record = reader.getNextRecord()) != null) {
				++records;
				System.out.println(records);
				
			}
			long eT = System.currentTimeMillis();
			long costT = (eT - sT) / 1000;
			System.out.println("--------------");
			System.out.println("Records: " + records);
			System.out.println("Cost Time: " + costT);
			in.close();
		
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	

}