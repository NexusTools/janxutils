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

/**
 * A Stream which caches an InputStream and provides
 * read/write functionality through an underlying FileStream.
 * 
 * @author katelyn
 */
public class CachingStream extends Stream {
	
	private final Stream localCache;
	private SubStream writeStream;
	private InputStream source;
	
	private String mimetype;
	private long size;
	
	protected static class CacheInfo {
		
		public long size;
		public String mimetype;
		public final Stream cacheStream;
		public final InputStream inStream;
		public CacheInfo(Stream cacheStream, InputStream source) {
			this.cacheStream = cacheStream;
			this.inStream = source;
		}
		public CacheInfo(Stream cacheStream) {
			this.cacheStream = cacheStream;
			this.inStream = null;
		}
		public CacheInfo(InputStream source) {
			this(null, source);
		}

		@Override
		public String toString() {
			return "CacheInfo[mimetype=" + mimetype + ",size=" + size + ",cacheStream=" + cacheStream+ ",inputStream=" + inStream + "]";
		}
		
	}
	
	protected CachingStream(CacheInfo set) throws IOException {
		this(set.cacheStream, set.inStream);
		this.mimetype = set.mimetype;
		this.size = set.size;
	}
	
	public CachingStream(Stream cacheStream, InputStream source) throws IOException {
		if(cacheStream == null)
			cacheStream = new TemporaryFileStream();
		if(source == null)
			this.writeStream = null;
		else
			this.writeStream = cacheStream.createSubSectorStream();
		this.localCache = cacheStream.createSubSectorStream();
		this.source = source;
		this.size = 0;
	}
	
	public CachingStream(String cachePath, InputStream source) throws IOException {
		this(cachePath != null ? new FileStream(cachePath, source != null) : null, source);
	}
	
	public CachingStream(InputStream source) throws IOException {
		this((FileStream)null, source);
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
		if(size > 0)
			return size;
		
		if(writeStream != null)
			return writeStream.pos() + source.available();
		
		return localCache.size();
	}

	@Override
	public String getMimeType() {
		if(mimetype != null)
			return mimetype;
		
		return super.getMimeType();
	}

	@Override
	public long pos() throws IOException {
		return localCache.pos();
	}
	
	@Override
	public String getScheme() {
		return localCache.getScheme();
	}

	@Override
	public String getPath() {
		return localCache.getPath();
	}

	@Override
	public String getURL() {
		return localCache.getURL();
	}
	
}
