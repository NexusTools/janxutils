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
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.MapAccessor;
import static net.nexustools.io.StreamUtils.DefaultBufferSize;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class CharArrayBuffer extends AppendablePrimitiveBuffer<Character, char[]> {
	private static final char[] EMPTY = new char[0];
	
	private static final PropMap<Integer, CacheTypeList<char[]>> cache = new PropMap();
	
	public static char[] nextBuffer(int size) {
		if(size < 1)
			size = DefaultBufferSize;
		
		final Integer desiredSize = size;
		try {
			return cache.read(new WriteReader<char[], MapAccessor<Integer, CacheTypeList<char[]>>>() {
				@Override
				public char[] read(MapAccessor<Integer, CacheTypeList<char[]>> data) throws Throwable {
					char[] next;
					try {
						next = data.get(desiredSize).shift();
						if(next == null)
							throw new NullPointerException();
					} catch(NullPointerException ex) {
						Logger.performance("Allocating", "char[" + desiredSize + "]");
						next = new char[desiredSize];
					}
					return next;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public static void releaseBuffer(final char[] buffer) {
		if(buffer == null)
			throw new NullPointerException();
		
		final Integer desiredSize = buffer.length;
		try {
			cache.write(new Writer<MapAccessor<Integer, CacheTypeList<char[]>>>() {
				@Override
				public void write(MapAccessor<Integer, CacheTypeList<char[]>> data) throws Throwable {
					CacheTypeList<char[]> cache = data.get(desiredSize);
					if(cache == null)
						data.put(desiredSize, cache = new CacheTypeList<char[]>());
					cache.push(buffer);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public CharArrayBuffer() {
		this((char[])null);
	}
	public CharArrayBuffer(int size) {
		this(nextBuffer(size));
	}
	public CharArrayBuffer(char[] buffer) {
		super(Character.class, buffer);
	}
	
	public Appendable createAppendable() {
		return new Appendable() {
			int pos = 0;
			public Appendable append(CharSequence cs) throws IOException {
				return append(cs, 0, cs.length());
			}
			public Appendable append(CharSequence cs, int offset, int len) throws IOException {
				char[] buff = nextBuffer(len);
				for(int i=0; i<len--; i++)
					buff[i] = cs.charAt(i + offset++);
				write(pos, buff);
				pos += len;
				
				return this;
			}
			public Appendable append(char c) throws IOException {
				write(pos, new char[]{c});
				return this;
			}
		};
	}

	@Override
	public char[] copy() {
		if(buffer == null)
			return EMPTY;
		return Arrays.copyOf(buffer, size);
	}

	@Override
	protected void release(char[] buffer) {
		assert(buffer.length <= Short.MAX_VALUE);
		releaseBuffer(buffer);
	}

	@Override
	protected char[] create(int size) {
		return nextBuffer(size);
	}

	@Override
	protected void convert(Character[] from, char[] to) {
		int pos = 0;
		for(int i=0; i<from.length; i++)
			to[i] = from[i];
	}

	@Override
	protected void arraycopy(char[] from, int fromOff, char[] to, int toOff, int len) {
		System.arraycopy(from, fromOff, to, toOff, len);
	}

	@Override
	public Character get(int pos) {
		return buffer[pos];
	}

	@Override
	public void put(int pos, Character value) {
		write(pos, new char[]{value}, 1);
	}

	@Override
	public int length(char[] of) {
		return of.length;
	}

	public byte[] toByteArray(Charset charset) {
		return toString().getBytes(charset);
	}

	@Override
	public String toString() {
		return new String(storage(), 0, size);
	}

	@Override
	public void write(int pos, CharSequence cs, int offset, int len) {
		int newSize = prepWrite(pos, len);
		for(int i=0; i<len; i++)
			buffer[pos+i] = cs.charAt(offset+i);
		size = newSize;
	}

	@Override
	public void put(int pos, char ch) {
		write(pos, new char[]{ch}, 1);
	}
	
}
