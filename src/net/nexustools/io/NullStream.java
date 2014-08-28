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
import java.nio.channels.ByteChannel;

/**
 *
 * @author katelyn
 */
public final class NullStream extends Stream {
	
	private static final NullStream instance = new NullStream();
	
	public static final NullStream instance() {
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
	public final boolean canWrite() {
		return true;
	}

	@Override
	public boolean canRead() {
		return false;
	}

	@Override
	public final long size() {
		return Long.MAX_VALUE;
	}
	
	@Override
	public String toURL() {
		return "null:";
	}

	@Override
	public InputStream createInputStream(long pos) throws IOException {
		return EfficientInputStream.Null;
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		throw new IOException("NullStream cannot be read from.");
	}

	@Override
	public ByteChannel createChannel(Object... args) throws UnsupportedOperationException, IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
