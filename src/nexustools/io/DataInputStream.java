/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import nexustools.io.data.Adaptor;
import static nexustools.StringUtils.UTF8;
import static nexustools.StringUtils.UTF16;
import static nexustools.StringUtils.ASCII;
import nexustools.io.data.AdaptorException;

/**
 *
 * @author katelyn
 * 
 * Adds additional functionality to the standard DataInputStream
 */
public final class DataInputStream extends java.io.DataInputStream {

	private final InputStream inStream;
	public DataInputStream(InputStream in) {
		super(in);
		inStream = in;
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
		} catch (AdaptorException | ClassNotFoundException | UnsupportedOperationException ex) {
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
		
		byte[] stringBytes = new byte[size];
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
		builder.append(inStream.toString());
		builder.append(')');

		return builder.toString();
	}
	
}
