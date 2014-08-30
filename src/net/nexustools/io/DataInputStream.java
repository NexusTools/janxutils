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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.data.adaptor.Adaptor;
import static net.nexustools.utils.StringUtils.UTF8;
import static net.nexustools.utils.StringUtils.UTF16;
import static net.nexustools.utils.StringUtils.ASCII;
import net.nexustools.data.adaptor.AdaptorException;
import net.nexustools.utils.Handler;

/**
 *
 * @author katelyn
 * 
 * Adds additional functionality to the standard DataInputStream
 */
public class DataInputStream extends java.io.DataInputStream {

	public DataInputStream(InputStream in) {
		super(in);
	}
	public DataInputStream(Stream stream) throws IOException {
		this(stream.createInputStream());
	}
	public DataInputStream(String uri) throws URISyntaxException, URISyntaxException, IOException {
		this(Stream.open(uri));
	}
	
	/**
	 * Uses a compatible adaptor to restore the object
	 * 
	 * @param object Object to restore
	 * @param type The type of class to try and read
	 * @throws IOException
	 */
	public final void readObject(Object object, Class<?> type) throws IOException {
		try {
			Adaptor.resolveAdaptor(type).read(object, this);
		} catch (AdaptorException ex) {
			throw new IOException(ex);
		}
	}
	
	/**
	 * Uses a compatible adaptor to restore the object
	 * 
	 * @param object Object to restore
	 * @throws IOException
	 */
	public final void readObject(Object object) throws IOException {
		readObject(object, object.getClass());
	}

	/**
	 * Attempts to create and restore an object from the stream
	 * 
	 * @return A new instance of an Object
	 * @throws IOException
	 */
	public final Object readMutableObject() throws IOException {
		try {
			return Adaptor.resolveAndReadMutable(this);
		} catch (AdaptorException ex) {
			throw new IOException(ex);
		} catch (ClassNotFoundException ex) {
			throw new IOException(ex);
		} catch (UnsupportedOperationException ex) {
			throw new IOException(ex);
		}
	}
	
	/**
	 * Reads a string using a given character set
	 * 
	 * @param charset
	 * @return String or null
	 * @throws IOException
	 */
	public String readString(Charset charset) throws IOException {
		short size = readShort();
		if(size < 1)
			return null;
		
		byte[] stringBytes = new byte[StreamUtils.DefaultBufferSize];
		readFully(stringBytes);
		return new String(stringBytes, charset);
	}
	
	/**
	 * Reads a string using the ASCII character set
	 * 
	 * @return String or null
	 * @throws IOException
	 */
	public String readASCII() throws IOException {
		return readString(ASCII);
	}
	
	/**
	 * Reads a string using the UTF-16 character set
	 * 
	 * @return String or null
	 * @throws IOException
	 */
	public String readUTF16() throws IOException {
		return readString(UTF16);
	}
	
	/**
	 * Reads a string using the UTF-8 character set
	 * 
	 * @return String or null
	 * @throws IOException
	 */
	public String readUTF8() throws IOException {
		return readString(UTF8);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append('(');
		builder.append(in.toString());
		builder.append(')');

		return builder.toString();
	}
	
}
