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
public class LongArrayBuffer extends PrimitiveArrayBuffer<Long, long[]> {
	private static final long[] EMPTY = new long[0];
	
	private static final PropMap<Integer, CacheTypeList<long[]>> cache = new PropMap();
	
	public static long[] nextBuffer(int size) {
		if(size < 1)
			size = DefaultBufferSize;
		
		final Integer desiredSize = size;
		try {
			return cache.read(new WriteReader<long[], MapAccessor<Integer, CacheTypeList<long[]>>>() {
				@Override
				public long[] read(MapAccessor<Integer, CacheTypeList<long[]>> data) throws Throwable {
					long[] next;
					try {
						next = data.get(desiredSize).shift();
						if(next == null)
							throw new NullPointerException();
					} catch(NullPointerException ex) {
						Logger.performance("Allocating", "long[" + desiredSize + "]");
						next = new long[desiredSize];
					}
					return next;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public static void releaseBuffer(final long[] buffer) {
		if(buffer == null)
			throw new NullPointerException();
		
		final Integer desiredSize = buffer.length;
		try {
			cache.write(new Writer<MapAccessor<Integer, CacheTypeList<long[]>>>() {
				@Override
				public void write(MapAccessor<Integer, CacheTypeList<long[]>> data) throws Throwable {
					CacheTypeList<long[]> cache = data.get(desiredSize);
					if(cache == null)
						data.put(desiredSize, cache = new CacheTypeList<long[]>());
					cache.push(buffer);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public LongArrayBuffer() {
		this((long[])null);
	}
	public LongArrayBuffer(int size) {
		this(nextBuffer(size));
	}
	public LongArrayBuffer(long[] buffer) {
		super(Long.class, buffer);
	}

	@Override
	public long[] copy() {
		if(buffer == null)
			return EMPTY;
		return Arrays.copyOf(buffer, size);
	}

	@Override
	protected void release(long[] buffer) {
		assert(buffer.length <= Short.MAX_VALUE);
		releaseBuffer(buffer);
	}

	@Override
	protected long[] create(int size) {
		return nextBuffer(size);
	}

	@Override
	protected void convert(Long[] from, long[] to) {
		int pos = 0;
		for(int i=0; i<from.length; i++)
			to[i] = from[i];
	}

	@Override
	protected void arraycopy(long[] from, int fromOff, long[] to, int toOff, int len) {
		System.arraycopy(from, fromOff, to, toOff, len);
	}

	@Override
	public Long get(int pos) {
		return buffer[pos];
	}

	@Override
	public void put(int pos, Long value) {
		write(pos, new long[]{value}, 1);
	}

	@Override
	public int length(long[] of) {
		return of.length;
	}
	
}
