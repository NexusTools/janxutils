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

import java.util.Arrays;
import java.lang.reflect.InvocationTargetException;
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
public class ShortArrayBuffer extends PrimitiveArrayBuffer<Short, short[]> {
	private static final short[] EMPTY = new short[0];
	
	private static final PropMap<Integer, CacheTypeList<short[]>> cache = new PropMap();
	
	public static short[] nextBuffer(int size) {
		if(size < 1)
			size = DefaultBufferSize;
		
		final Integer desiredSize = size;
		return cache.read(new WriteReader<short[], MapAccessor<Integer, CacheTypeList<short[]>>>() {
			@Override
			public short[] read(MapAccessor<Integer, CacheTypeList<short[]>> data) {
				short[] next;
				try {
					next = data.get(desiredSize).shift();
					if(next == null)
						throw new NullPointerException();
				} catch(NullPointerException ex) {
					Logger.performance("Allocating", "short[" + desiredSize + "]");
					next = new short[desiredSize];
				}
				return next;
			}
		});
	}
	public static void releaseBuffer(final short[] buffer) {
		if(buffer == null)
			throw new NullPointerException();
		
		final Integer desiredSize = buffer.length;
		cache.write(new Writer<MapAccessor<Integer, CacheTypeList<short[]>>>() {
			@Override
			public void write(MapAccessor<Integer, CacheTypeList<short[]>> data) {
				CacheTypeList<short[]> cache = data.get(desiredSize);
				if(cache == null)
					data.put(desiredSize, cache = new CacheTypeList<short[]>());
				cache.push(buffer);
			}
		});
	}
	
	public ShortArrayBuffer() {
		this((short[])null);
	}
	public ShortArrayBuffer(int size) {
		this(nextBuffer(size));
	}
	public ShortArrayBuffer(short[] buffer) {
		super(Short.class, buffer);
	}

	@Override
	public short[] copy() {
		if(buffer == null)
			return EMPTY;
		return Arrays.copyOf(buffer, size);
	}

	@Override
	protected void release(short[] buffer) {
		assert(buffer.length <= Short.MAX_VALUE);
		releaseBuffer(buffer);
	}

	@Override
	protected short[] create(int size) {
		return nextBuffer(size);
	}

	@Override
	protected void convert(Short[] from, short[] to) {
		int pos = 0;
		for(int i=0; i<from.length; i++)
			to[i] = from[i];
	}

	@Override
	protected void arraycopy(short[] from, int fromOff, short[] to, int toOff, int len) {
		System.arraycopy(from, fromOff, to, toOff, len);
	}

	@Override
	public Short get(int pos) {
		return buffer[pos];
	}

	@Override
	public void put(int pos, Short value) {
		write(pos, new short[]{value}, 1);
	}

	@Override
	public int length(short[] of) {
		return of.length;
	}
	
}
