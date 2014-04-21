/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A expanding Stream that stores its contents in memory.
 * 
 * While writing, this Stream expands its internal buffer
 * by a multiple of 2 each time its about to become full.
 * 
 * @author katelyn
 */
public class MemoryStream extends Stream {
	
	private int pos = 0;
	private int size = 0;
	private byte[] buffer;
	
	private static int sizeForStream(InputStream in) {
		try {
			return in.available();
		} catch(IOException ex) {
			return 64;
		}
	}
	
	public MemoryStream(InputStream inStream, short max) throws IOException {
		this(sizeForStream(inStream));
		StreamUtils.copy(inStream, createOutputStream(), max);
	}
	
	public MemoryStream(InputStream inStream) throws IOException {
		this(inStream, StreamUtils.DefaultMemoryMax);
	}
	
	public MemoryStream(byte[] buf, int pos, int len) throws IOException {
		this(len - pos);
		write(buf, pos, len);
	}
	
	public MemoryStream(byte[] buf) throws IOException {
		this(buf, 0, buf.length);
	}
	
	public MemoryStream(int size) {
		buffer = new byte[Math.max(8, size)];
	}
	
	public MemoryStream() {
		this(64);
	}
	
	public final byte[] toByteArray() {
		byte[] copy = new byte[size];
		System.arraycopy(buffer, 0, copy, 0, size);
		return copy;
	}
	
	protected final static int nearestPow(int size) {
		if(size < 8)
			return 8;
		
		return (int) Math.pow(2, Math.ceil(Math.log(size)/Math.log(2)));
	}
	
	public void expand(int newSize) {
		if(newSize <= size)
			throw new RuntimeException("The requested size is not expanding");
		
		byte[] newArray = new byte[newSize];
		System.arraycopy(buffer, 0, newArray, 0, size);
		buffer = newArray;
	}

	@Override
	public final int read(byte[] buf, int offset, int len) throws IOException {
		len = (int) Math.min(remaining(), len);
		if(len < 1)
			return -1;
		
		System.arraycopy(buffer, this.pos, buf, offset, len);
		pos += len;
		return len;
	}

	@Override
	public final void write(byte[] buf, int offset, int len) throws IOException {
		if(len >= buffer.length - this.pos)
			expand(nearestPow(buffer.length + len));
		
		System.arraycopy(buf, offset, buffer, pos, len);
		size += len;
		pos += len;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public void flush() throws IOException {}

	@Override
	public long pos() {
		return pos;
	}

	@Override
	public void seek(long pos) throws IOException {
		if(pos > size)
			throw new IOException("Seek out of range");
		
		this.pos = (int)Math.min(Integer.MAX_VALUE, pos);
	}

	@Override
	public long size() throws IOException {
		return size;
	}

	@Override
	public String getURL() {
		return "memory:" + buffer.length + "@" + buffer;
	}

	public boolean isEmpty() {
		return size <= 0;
	}
	
}
