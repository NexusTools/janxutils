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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import net.nexustools.data.buffer.basic.ByteArrayBuffer;
import static net.nexustools.io.StreamUtils.DefaultBufferSize;
import net.nexustools.utils.StringUtils;

/**
 * A expanding Stream that stores its contents in memory.
 * 
 * While writing, this Stream expands its internal buffer
 * by a multiple of 2 each time its about to become full.
 * 
 * @author katelyn
 */
public class MemoryStream extends Stream {
	
	private final ByteArrayBuffer buffer;
	public MemoryStream(final InputStream inStream, final short max) throws IOException {
		this();
		write(new StreamWriter<OutputStream>() {
			public void write(OutputStream outStream) throws IOException {
				try {
					StreamUtils.copy(inStream, outStream, max);
				} catch(EOFException ex) {}
			}
		});
	}
	
	public MemoryStream(InputStream inStream) throws IOException {
		this(inStream, DefaultBufferSize);
	}
	
	public MemoryStream(String source, Charset charset) throws IOException {
		this(source.getBytes(charset));
	}
	
	public MemoryStream(String source) throws IOException {
		this(source, StringUtils.UTF8);
	}
	
	public MemoryStream(byte[] buf, int pos, int len) throws IOException {
		this(len - pos);
		buffer.writeImpl(0, buf, pos, len);
	}
	
	public MemoryStream(byte[] buf) throws IOException {
		buffer = new ByteArrayBuffer(buf);
	}
	
	public MemoryStream(int size) {
		buffer = new ByteArrayBuffer(size);
	}
	
	public MemoryStream() {
		buffer = new ByteArrayBuffer();
	}
	
	public final byte[] toByteArray() {
		return buffer.copy();
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean canRead() {
		return buffer != null;
	}

	@Override
	public long size() {
		return buffer.size();
	}

	@Override
	public long lastModified() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Modification times not tracked on memory streams");
	}

	@Override
	public String scheme() {
		return "memory:";
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public InputStream createInputStream(long pos) throws IOException {
		return buffer.createInputStream((short)pos);
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		return buffer.createOutputStream((int)pos);
	}

	@Override
	public String path() {
		return "";
	}

	@Override
	public ByteChannel createChannel(Object... args) throws UnsupportedOperationException, IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
