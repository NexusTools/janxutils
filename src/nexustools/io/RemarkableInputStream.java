/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author katelyn
 * 
 * This class name is a pun
 * its purpose is to allow marking and resetting
 * with input streams that normally don't support it
 * 
 */
public class RemarkableInputStream extends EfficientInputStream {
	
	private final InputStream inStream;
	private ByteArrayInputStream byteInStream;
	private MemoryStream byteOutStream;
	
	public RemarkableInputStream(InputStream inStream) {
		this.inStream = inStream;
	}

	@Override
	public synchronized void mark(int readlimit) {
		byteOutStream = new MemoryStream();
	}

	@Override
	public synchronized void reset() throws IOException {
		if(!byteOutStream.isEmpty())
			byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
		byteOutStream = null;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int markOff = off;
		int markLen = len;
		int readBytes = 0;
		if(byteInStream != null) {
			readBytes += byteInStream.read(b, markOff, markLen);
			if(readBytes > 0) {
				markOff += readBytes;
				markLen -= readBytes;
			} else {
				readBytes = 0;
				byteInStream = null;
			}
		}

		if(markLen > 0)
			readBytes += inStream.read(b, markOff, markLen);
		
		if(byteOutStream != null && readBytes > 0)
			byteOutStream.write(b, off, readBytes);
		
		return readBytes;
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
