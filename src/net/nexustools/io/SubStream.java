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
import java.io.OutputStream;
import java.net.URI;

/**
 * A Stream that uses other Streams,
 * you can provide a Range, or use it
 * to read from the same Stream at multiple
 * positions without losing track.
 * 
 * @author katelyn
 */
public class SubStream extends Stream {
	
	public static interface Range {
		
		public boolean isSubRange();
		
		public long start();
		public long end();
		
		public long size();
		
	}
	
	private static final class SetRange implements Range {
		
		private final long start;
		private final long end;
		
		protected SetRange(long start, long end) {
			this.start = start;
			this.end = end;
		}

		protected SetRange() {
			this(0, Long.MAX_VALUE);
		}
		
		@Override
		public boolean isSubRange() {
			return start > 0 || end < Long.MAX_VALUE;
		}

		@Override
		public long size() {
			return end-start;
		}

		@Override
		public long start() {
			return start;
		}

		@Override
		public long end() {
			return end;
		}
		
	}
	
	private static final Range fullRange = new Range() {

		@Override
		public boolean isSubRange() {
			return false;
		}

		@Override
		public long size() {
			return Long.MAX_VALUE;
		}

		@Override
		public long start() {
			return 0;
		}

		@Override
		public long end() {
			return Long.MAX_VALUE;
		}
		
	};
	
	public static Range getFullRange() {
		return fullRange;
	}
	
	public static Range createRange(long start, long end) {
		return new SetRange(start, end);
	}
	
	private long pos = 0;
	private final Stream stream;
	private final Range range;
	
	public SubStream(Stream stream, Range range) {
		this.stream = stream;
		this.range = range;
	}

	public SubStream(Stream stream, long start, long end) {
		this(stream, new SetRange(start, end));
	}

	public SubStream(Stream stream) {
		this(stream, getFullRange());
	}

	@Override
	public long size() throws IOException {
		return Math.min(stream.size() - range.start(), range.size());
	}
	
	@Override
	public String mimeType() {
		return stream.mimeType();
	}


	@Override
	public boolean canWrite() {
		return stream.canWrite();
	}

	@Override
	public boolean canRead() {
		return stream.canRead();
	}

	@Override
	public URI toURI() {
		if(!range.isSubRange())
			return stream.toURI();
		
		return super.toURI();
	}
	
	@Override
	public String toString() {
		if(!range.isSubRange())
			return stream.toString();
		
		return super.toString();
	}
	
	/**
	 * Returns the Effective Stream.
	 * 
	 * If the range for this SubStream is the full range of the underlying Stream,
	 * the underlying Stream is returned, otherwise this Stream is returned.
	 * 
	 * @return
	 */
	@Override
	public Stream effectiveStream() {
		if(!range.isSubRange())
			return this.stream.effectiveStream();
		return super.effectiveStream();
	}

	@Override
	public String scheme() {
		return "substream";
	}

	@Override
	public String path() {
		return range.start() + "-" + range.end() + "@" + stream.toURL();
	}

	@Override
	public boolean isDirectory() {
		return stream.isDirectory();
	}

	@Override
	public Iterable<String> children() throws IOException {
		return stream.children();
	}

	@Override
	public boolean isHidden() throws IOException {
		return stream.isHidden();
	}

	@Override
	public long lastModified() throws IOException {
		return stream.lastModified();
	}

	/**
	 * Returns the Stream this SubStream is a range of.
	 * 
	 * @return
	 */
	public Stream getUnderlyingString() {
		return stream;
	}

	public Range getRange() {
		return range;
	}

	@Override
	public InputStream createInputStream(long pos) throws IOException {
		if(range.isSubRange())
			throw new UnsupportedOperationException();
		return stream.createInputStream(pos);
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		if(range.isSubRange())
			throw new UnsupportedOperationException();
		return stream.createOutputStream(pos);
	}
	
}
