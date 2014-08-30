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
import java.lang.reflect.InvocationTargetException;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.buffer.basic.CacheTypeList;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Handler;
import net.nexustools.utils.StringUtils;
import net.nexustools.utils.log.Logger;

/**
 * A convenience class 
 * 
 * @author katelyn
 */
public class StreamUtils {
	
	public static final short DefaultBufferSize = Short.valueOf(System.getProperty("stream.buffersize", "8192"));
	
	public static void copy(InputStream inStream, OutputStream outStream) throws IOException {
		copy(inStream, outStream, DefaultBufferSize);
	}
	
	public static void copy(InputStream inStream, OutputStream outStream, long amount) throws IOException {
		copy(inStream, outStream, DefaultBufferSize, amount);
	}

	public static void copy(final InputStream inStream, final OutputStream outStream, short bufferSize, final long amount) throws IOException {
		copy(inStream, outStream, new byte[bufferSize], amount);
	}

	public static void copy(InputStream inStream, OutputStream outStream, byte[] buffer, long amount) throws IOException {
		int copied;
		while((copied = inStream.read(buffer, 0, NXUtils.remainingMin(amount, buffer.length))) > 0) {
			amount -= copied;
			outStream.write(buffer, 0, copied);
			if(amount < 0)
				break;
		}
		if(amount > 0)
			throw new EOFException("Stream ended with " + amount + "bytes left");
	}
	
}
