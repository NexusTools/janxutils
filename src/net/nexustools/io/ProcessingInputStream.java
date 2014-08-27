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

/**
 *
 * @author katelyn
 */
public abstract class ProcessingInputStream extends BufferInputStream {
	
	public final InputStream underlying;
	public ProcessingInputStream(InputStream underlying, int... bufferLengths) {
		super(bufferLengths);
		this.underlying = underlying;
	}

	@Override
	public void close() throws IOException {
		super.close();
		underlying.close();
	}

	@Override
	public final int read(byte[] b, int off, int len, byte[]... buffers) throws IOException {
		return read(b, off, len, underlying, buffers);
	}

	public abstract int read(byte[] b, int off, int len, InputStream underlying, byte[]... buffers) throws IOException;
	
}
