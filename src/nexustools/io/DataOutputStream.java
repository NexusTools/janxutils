/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import static nexustools.StringUtils.UTF8;
import static nexustools.StringUtils.UTF16;
import static nexustools.StringUtils.ASCII;
import nexustools.io.data.Adaptor;
import nexustools.io.data.AdaptorException;

/**
 *
 * @author katelyn
 */
public class DataOutputStream extends java.io.DataOutputStream {

	private final OutputStream outStream;
	public DataOutputStream(OutputStream out) {
		super(out);
		outStream = out;
	}

	public void writeObject(Object object, Class<?> type) throws IOException {
		try {
			Adaptor.resolveAdaptor(type).write(object, this);
		} catch (AdaptorException ex) {
			throw new IOException(ex);
		}
	}

	public void writeObject(Object object) throws IOException {
		writeObject(object, object.getClass());
	}

	public void writeMutableObject(Object object) throws IOException {
		try {
			Adaptor.resolveAndWriteMutable(object, this);
		} catch (UnsupportedOperationException | AdaptorException ex) {
			throw new IOException(ex);
		}
	}
	
	public void writeString(String string, Charset charset) throws IOException {
		if(string == null) {
			writeShort(0);
			return;
		}
		
		byte[] stringBytes = string.getBytes(charset);
		writeShort(stringBytes.length);
		write(stringBytes);
	}
	
	public void writeASCII(String string) throws IOException {
		writeString(string, ASCII);
	}
	
	public void writeUTF16(String string) throws IOException {
		writeString(string, UTF16);
	}
	
	public void writeUTF8(String string) throws IOException {
		writeString(string, UTF8);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append('(');
		builder.append(outStream.toString());
		builder.append(')');

		return builder.toString();
	}
	
}
