/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author katelyn
 */
public class InputAdaptorStream extends Stream {
	
	private long pos;
	private final InputStream inStream;
	
	public InputAdaptorStream(final InputStream inStream) throws IOException {
		if(!inStream.markSupported())
			this.inStream = new RemarkableInputStream(inStream);
		else
			this.inStream = inStream;
		
		mark();
	}
	
	private void mark() {
		inStream.mark(Short.MAX_VALUE);
	}

	@Override
	public int read(byte[] buffer, int offet, int size) throws IOException {
		int len = inStream.read(buffer, offet, size);
		if(len > 0)
			this.pos += len;
		return len;
	}

	@Override
	public void write(byte[] buffer, int pos, int size) throws IOException {
		throw new IOException("InputAdaptorStream does not support writing...");
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public void flush() throws IOException {
		throw new IOException("InputAdaptorStream cannot be flushed...");
	}

	@Override
	public long pos() {
		return pos;
	}

	@Override
	public final void seek(long pos) throws IOException {
		if(this.pos == pos)
			return;
		
		inStream.reset();
		mark();
		
		inStream.skip(pos);
		this.pos = pos;
	}

	@Override
	public final long size() throws IOException {
		return pos + inStream.available();
	}

	@Override
	public final long remaining() throws IOException {
		return inStream.available();
	}

	@Override
	public String getURL() {
		return "input:" + inStream.toString();
	}
	
}
