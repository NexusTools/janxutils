/*
 * janxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * janxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with janxutils.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.nexustools.io;

import java.io.EOFException;
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
	
	public static void copy(InputStream inStream, OutputStream outStream) throws IOException {
		copy(inStream, outStream, DefaultMemoryMax);
	}
	
	public static void copy(InputStream inStream, OutputStream outStream, int max) throws IOException {
		copy(inStream, outStream, max, DefaultCopySize);
	}

	public static void copy(InputStream inStream, OutputStream outStream, int amount, short bufferSize) throws IOException {
		int copied;
		byte[] buf = new byte[bufferSize];
		while((copied = inStream.read(buf, 0, (int) Math.min(amount, bufferSize))) > 0) {
			amount -= copied;
			outStream.write(buf, 0, copied);
			if(amount < 0)
				break;
		}
		if(amount > 0)
			throw new EOFException("Stream ended with " + amount + "bytes left");
	}
	
}
