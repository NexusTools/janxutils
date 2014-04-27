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
