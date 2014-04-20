/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A convenience class 
 * 
 * @author katelyn
 */
public class StreamUtils {
	
	public static final short DefaultMemoryMax = 8192;
	public static final short DefaultCopySize = DefaultMemoryMax;
	
	public static long copy(InputStream inStream, OutputStream outStream) throws IOException {
		return copy(inStream, outStream, DefaultMemoryMax);
	}
	
	public static long copy(InputStream inStream, OutputStream outStream, long max) throws IOException {
		return copy(inStream, outStream, max, DefaultCopySize);
	}

	public static long copy(InputStream inStream, OutputStream outStream, long max, short bufferSize) throws IOException {
		int copied;
		long read = 0;
		byte[] buf = new byte[bufferSize];
		while((copied = inStream.read(buf)) != -1) {
			max -= copied;
			if(max < 0)
				throw new IOException("Reached copy limit");
			outStream.write(buf, 0, copied);
			read += copied;
		}
		
		return read;
	}
	
}
