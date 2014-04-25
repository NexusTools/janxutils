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
public final class NullStream extends Stream {
	
	protected static final class SubNullStream extends SubStream {

		public SubNullStream() {
			super(instance, 0, 0);
		}

		@Override
		public SubStream createSubSectorStream(Range range) {
			return this;
		}

		@Override
		public String getURL() {
			return "null:";
		}
	
	}
	
	private static final NullStream instance = new NullStream();
	private static final SubNullStream subInstance = new SubNullStream();
	
	public static final NullStream getInstance() {
		return instance;
	}
	
	protected NullStream() {}

	@Override
	public String getScheme() {
		return "null";
	}

	@Override
	public String getPath() {
		return "";
	}

	@Override
	public final SubStream createSubSectorStream(SubStream.Range range) {
		return subInstance;
	}

	@Override
	public final int read(byte[] buffer, int pos, int size) throws IOException {
		throw new IOException("NullSectorStream cannot be read from...");
	}

	@Override
	public final void write(byte[] buffer, int pos, int size) throws IOException {
		throw new IOException("NullSectorStream cannot be written to...");
	}

	@Override
	public final boolean canWrite() {
		return false;
	}

	@Override
	public final void flush() throws IOException {
		throw new IOException("NullSectorStream cannot be flushed...");
	}

	@Override
	public final long pos() {
		return 0L;
	}

	@Override
	public final void seek(long pos) throws IOException {
		throw new IOException("NullSectorStream cannot seek...");
	}

	@Override
	public final long size() {
		return 0L;
	}
	
	@Override
	public String getURL() {
		return "null:";
	}
	
}
