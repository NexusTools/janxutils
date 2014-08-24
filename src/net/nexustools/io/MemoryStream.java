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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.StringUtils;
import net.nexustools.utils.WeakArrayList;

/**
 * A expanding Stream that stores its contents in memory.
 * 
 * While writing, this Stream expands its internal buffer
 * by a multiple of 2 each time its about to become full.
 * 
 * @author katelyn
 */
public class MemoryStream extends Stream {
	
	private final Prop<byte[]> buffer = new Prop();
	private final Prop<Long> lastModified = new Prop(0);
	private final Prop<WeakArrayList> weakArrayList = new Prop(new WeakArrayList());
	public MemoryStream(final InputStream inStream, final short max) throws IOException {
		write(new StreamWriter<OutputStream>() {
			public void write(OutputStream outStream) throws IOException {
				try {
					StreamUtils.copy(inStream, outStream, max);
				} catch(EOFException ex) {}
			}
		});
	}
	
	public MemoryStream(InputStream inStream) throws IOException {
		this(inStream, StreamUtils.DefaultMemoryMax);
	}
	
	public MemoryStream(String source, Charset charset) throws IOException {
		this(source.getBytes(charset));
	}
	
	public MemoryStream(String source) throws IOException {
		this(source, StringUtils.UTF8);
	}
	
	public MemoryStream(byte[] buf, int pos, int len) throws IOException {
		this(len - pos);
	}
	
	public MemoryStream(byte[] buf) throws IOException {
		this(buf, 0, buf.length);
	}
	
	public MemoryStream(int size) {
		buffer.set(new byte[Math.max(8, size)]);
	}
	
	public MemoryStream() {
		this(64);
	}
	
	public final byte[] toByteArray() {
		byte[] buff = buffer.get();
		return Arrays.copyOf(buff, buff.length);
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
	public long size() throws IOException {
		try {
			return buffer.read(new Reader<Long, PropAccessor<byte[]>>() {
				@Override
				public Long read(PropAccessor<byte[]> data) {
					return data.isset() ? data.get().length : 0L;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
	}

	@Override
	public long lastModified() {
		return lastModified.get();
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
		byte[] buff;
		try {
			buff = weakArrayList.read(new Reader<byte[], PropAccessor<WeakArrayList>>() {
				@Override
				public byte[] read(PropAccessor<WeakArrayList> data) {
					for(Object stream : data.get())
						try {
							((ByteArrayOutputStream)stream).flush();
						} catch (Throwable t) {}
					return buffer.get();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
		if(buff == null)
			throw new IOException("");
		return new ByteArrayInputStream(buff);
	}
	
	protected void flushFrom(final ByteArrayOutputStream outStream) throws IOException {
		try {
			buffer.write(new Writer<PropAccessor<byte[]>>() {
				@Override
				public void write(PropAccessor<byte[]> data) {
					if(!data.isset() || data.get().length != outStream.size())
						data.set(outStream.toByteArray());
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		if(pos > 0)
			throw new IOException("Not handled yet");
		
		try {
			return weakArrayList.read(new Reader<OutputStream, PropAccessor<WeakArrayList>>() {
				@Override
				public OutputStream read(PropAccessor<WeakArrayList> data) {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream() {
						boolean open = true;
						
						@Override
						public void flush() throws IOException {
							if(open)
								flushFrom(this);
						}
						@Override
						public void close() throws IOException {
							if(open) {
								final ByteArrayOutputStream myself = this;
								try {
									weakArrayList.write(new Writer<PropAccessor<WeakArrayList>>() {
										@Override
										public void write(PropAccessor<WeakArrayList> data) throws IOException {
											data.get().remove(myself);
											flushFrom(myself);
										}
									});
								} catch (InvocationTargetException ex) {
									throw NXUtils.unwrapIOException(ex);
								}
								open = false;
							}
							super.close();
						}
						@Override
						protected void finalize() throws Throwable {
							close();
							super.finalize();
						}
					};
					data.get().add(outStream);
					return outStream;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
				
	}

	@Override
	public String path() {
		return "";
	}
	
}
