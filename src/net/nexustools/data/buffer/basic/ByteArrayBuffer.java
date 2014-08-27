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

package net.nexustools.data.buffer.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import net.nexustools.io.EfficientInputStream;
import net.nexustools.io.EfficientOutputStream;
import net.nexustools.io.StreamUtils;

/**
 *
 * @author katelyn
 */
public class ByteArrayBuffer extends PrimitiveArrayBuffer<Byte, byte[]> {
	
	public ByteArrayBuffer() {
		this((byte[])null);
	}
	public ByteArrayBuffer(int size) {
		this(StreamUtils.nextBuffer(size));
	}
	public ByteArrayBuffer(byte[] buffer) {
		super(Byte.class, buffer);
	}
	
	public OutputStream createOutputStream(final int at) {
		return new EfficientOutputStream() {
			int pos = at;
			{
				if(pos < 0)
					pos = size + (pos + 1);
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				ByteArrayBuffer.this.writeImpl(pos, b, off, len);
				pos += len;
			}
		};
	}
	
	public InputStream createInputStream(final int at) {
		return new EfficientInputStream() {
			int pos = at;
			{
				if(pos < 0)
					pos = size + (pos + 1);
			}
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				int read = ByteArrayBuffer.this.readImpl(pos, b, off, len);
				pos += read;
				return read;
			}
		};
	}

	@Override
	public byte[] copy() {
		return Arrays.copyOf(buffer, size);
	}

	@Override
	protected void release(byte[] buffer) {
		assert(buffer.length <= Short.MAX_VALUE);
		StreamUtils.releaseBuffer(buffer);
	}

	@Override
	protected byte[] create(int size) {
		return StreamUtils.nextBuffer(size);
	}

	@Override
	protected void convert(Byte[] from, byte[] to) {
		int pos = 0;
		for(int i=0; i<from.length; i++)
			to[i] = from[i];
	}

	@Override
	protected void arraycopy(byte[] from, int fromOff, byte[] to, int toOff, int len) {
		System.arraycopy(from, fromOff, to, toOff, len);
	}

	@Override
	public Byte get(int pos) {
		return buffer[pos];
	}

	@Override
	public void put(int pos, Byte value) {
		write(pos, new byte[]{value}, 1);
	}

	@Override
	public int length(byte[] of) {
		return of.length;
	}
	
}
