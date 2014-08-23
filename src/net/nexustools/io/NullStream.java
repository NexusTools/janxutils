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
		public String toURL() {
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
	public String scheme() {
		return "null";
	}

	@Override
	public String path() {
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
	public boolean isHidden() {
		return true;
	}

	@Override
	public final long size() {
		return 0L;
	}
	
	@Override
	public String toURL() {
		return "null:";
	}
	
}
