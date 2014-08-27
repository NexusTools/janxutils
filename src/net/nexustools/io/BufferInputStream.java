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
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class BufferInputStream extends EfficientInputStream {
	
	private byte[][] buffers;
	public BufferInputStream(int... bufferLengths) {
		buffers = new byte[bufferLengths.length][];
		for(int i=0; i<bufferLengths.length; i++)
			buffers[i] = StreamUtils.nextBuffer(bufferLengths[i]);
	}

	@Override
	public void close() throws IOException {
		for(byte[] buffer : buffers)
			StreamUtils.releaseBuffer(buffer);
		buffers = null;
	}

	@Override
	public final int read(byte[] b, int off, int len) throws IOException {
		return read(b, off, len, buffers);
	}

	public abstract int read(byte[] b, int off, int len, byte[]... buffers) throws IOException;

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(buffers != null) {
			Logger.warn("BufferInputStream.close not called, buffers being released by system.");
			close();
		}
	}
	
}
