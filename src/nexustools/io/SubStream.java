/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;

/**
 *
 * @author katelyn
 */
public class SubStream extends Stream {
	
	public static final class Range {
		
		public final long start;
		public final long end;
		
		protected Range(long start, long end) {
			this.start = start;
			this.end = end;
		}

		protected Range() {
			this(0, Long.MAX_VALUE);
		}
		
		public boolean isSubRange() {
			return start > 0 || end < Long.MAX_VALUE;
		}

		private long size() {
			return end-start;
		}
		
	}
	
	public static Range getRange() {
		return new Range();
	}
	
	public static Range getRange(long start, long end) {
		return new Range(start, end);
	}
	
	private long pos = 0;
	private final Stream stream;
	private final Range range;
	
	public SubStream(Stream stream, Range range) {
		this.stream = stream;
		this.range = range;
	}

	SubStream(Stream stream, long start, long end) {
		this(stream, new Range(start, end));
	}

	SubStream(Stream stream) {
		this(stream, new Range());
	}

	@Override
	public void seek(long pos) throws IOException {
		stream.seek(Math.min(pos, size()) + range.start);
	}

	@Override
	public long pos() {
		return pos;
	}

	@Override
	public long size() throws IOException {
		return Math.min(stream.size() - range.start, range.size());
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
		stream.write(buffer, offset, (int) (Math.min(range.end, offset+len)-offset));
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
		
		return "substream:" + range.start + "-" + range.end + "@" + stream.getURL();
	}
	
	@Override
	public String toString() {
		if(!range.isSubRange())
			return stream.toString();
		
		return super.toString();
	}
	
}
