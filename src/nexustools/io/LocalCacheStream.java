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
public class LocalCacheStream extends Stream {
	
	private final Stream localCache;
	private SubStream writeStream;
	private InputStream source;
	
	public LocalCacheStream(FileStream localCache, InputStream source) {
		if(source == null) {
			this.localCache = localCache;
			this.writeStream = null;
		} else {
			this.localCache = localCache.createSubSectorStream();
			this.writeStream = localCache.createSubSectorStream();
		}
		this.source = source;
	}
	
	public LocalCacheStream(String cachePath, InputStream source) throws IOException {
		this(new FileStream(cachePath, source != null), source);
	}
	
	public LocalCacheStream(InputStream source) throws IOException {
		this(new TemporaryFileStream(), source);
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException {
		if(writeStream != null) {
			int readAheadLen = len - (int)Math.min(Integer.MAX_VALUE, writeStream.pos() - localCache.pos());
			if(readAheadLen > 0)
				readAhead(readAheadLen);
		}
		int read = localCache.read(buffer, off, len);
		return read;
	}

	@Override
	public void write(byte[] buffer, int off, int len) throws IOException {
		throw new IOException("Cannot write to LocalCacheStream");
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public void flush() throws IOException {
		throw new IOException("Cannot flush LocalCacheStream");
	}
	
	protected void readAhead(long len) throws IOException {
		int read = 0;
		byte[] buf = new byte[StreamUtils.DefaultCopySize];
		while(len > 0 && (read = source.read(buf, 0, (int)Math.min(buf.length, len))) > 0) {
			writeStream.write(buf, 0, read);
			len -= read;
		}
		if(read < 0) {
			source = null;
			writeStream.flush();
			writeStream = null;
		}
	}

	@Override
	public void seek(long pos) throws IOException {
		if(writeStream != null)
			if(pos > writeStream.pos())
				readAhead(pos - writeStream.pos());
		localCache.seek(pos);
	}
	
	@Override
	public long size() throws IOException {
		if(writeStream != null)
			return writeStream.pos() + source.available();
		
		return localCache.size();
	}

	@Override
	public long pos() {
		return localCache.pos();
	}

	@Override
	public String getURL() {
		return localCache.getURL();
	}
	
}
