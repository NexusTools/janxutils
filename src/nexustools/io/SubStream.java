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
		
		public long getStart();
		public long getEnd();
		
		public long getSize();
		
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
		public long getSize() {
			return end-start;
		}

		@Override
		public long getStart() {
			return start;
		}

		@Override
		public long getEnd() {
			return end;
		}
		
	}
	
	private static final Range fullRange = new Range() {

		@Override
		public boolean isSubRange() {
			return false;
		}

		@Override
		public long getSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public long getStart() {
			return 0;
		}

		@Override
		public long getEnd() {
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
	public void seek(long pos) throws IOException {
		stream.seek(Math.min(pos, size()) + range.getStart());
	}

	@Override
	public long pos() {
		return pos;
	}

	@Override
	public long size() throws IOException {
		return Math.min(stream.size() - range.getStart(), range.getSize());
	}
	
	@Override
	public String getMimeType() {
		return stream.getMimeType();
	}

	@Override
	public int read(byte[] buffer, int offset, int len) throws IOException {
		stream.seek(pos);
		len = stream.read(buffer, offset, len);
		pos = stream.pos();
		return len;
	}

	@Override
	public void write(byte[] buffer, int offset, int len) throws IOException {
		stream.seek(pos);
		stream.write(buffer, offset, (int) (Math.min(range.getEnd(), offset+len)-offset));
		pos = stream.pos();
	}

	@Override
	public boolean canWrite() {
		return stream.canWrite();
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	@Override
	public SubStream createSubSectorStream(Range range) {
		if(!range.isSubRange())
			return stream.createSubSectorStream(range);
		return super.createSubSectorStream(range); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Override
	public String getURL() {
		if(!range.isSubRange())
			return stream.getURL();
		
		return super.getURL();
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
	public Stream getEffectiveStream() {
		if(!range.isSubRange())
			return this.stream.getEffectiveStream();
		return super.getEffectiveStream();
	}

	@Override
	public String getScheme() {
		return "substream";
	}

	@Override
	public String getPath() {
		return range.getStart() + "-" + range.getEnd() + "@" + stream.getURL();
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
	
}
