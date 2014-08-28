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
public class IntArrayBuffer extends PrimitiveArrayBuffer<Integer, int[]> {
	private static final int[] EMPTY = new int[0];
	
	private static final PropMap<Integer, CacheTypeList<int[]>> cache = new PropMap();
	
	public static int[] nextBuffer(int size) {
		if(size < 1)
			size = DefaultBufferSize;
		
		final Integer desiredSize = size;
		try {
			return cache.read(new WriteReader<int[], MapAccessor<Integer, CacheTypeList<int[]>>>() {
				@Override
				public int[] read(MapAccessor<Integer, CacheTypeList<int[]>> data) throws Throwable {
					int[] next;
					try {
						next = data.get(desiredSize).shift();
						if(next == null)
							throw new NullPointerException();
					} catch(NullPointerException ex) {
						Logger.performance("Allocating", "int[" + desiredSize + "]");
						next = new int[desiredSize];
					}
					return next;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public static void releaseBuffer(final int[] buffer) {
		if(buffer == null)
			throw new NullPointerException();
		
		final Integer desiredSize = buffer.length;
		try {
			cache.write(new Writer<MapAccessor<Integer, CacheTypeList<int[]>>>() {
				@Override
				public void write(MapAccessor<Integer, CacheTypeList<int[]>> data) throws Throwable {
					CacheTypeList<int[]> cache = data.get(desiredSize);
					if(cache == null)
						data.put(desiredSize, cache = new CacheTypeList<int[]>());
					cache.push(buffer);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public IntArrayBuffer() {
		this((int[])null);
	}
	public IntArrayBuffer(int size) {
		this(nextBuffer(size));
	}
	public IntArrayBuffer(int[] buffer) {
		super(Integer.class, buffer);
	}

	@Override
	public int[] copy() {
		if(buffer == null)
			return EMPTY;
		return Arrays.copyOf(buffer, size);
	}

	@Override
	protected void release(int[] buffer) {
		assert(buffer.length <= Integer.MAX_VALUE);
		releaseBuffer(buffer);
	}

	@Override
	protected int[] create(int size) {
		return nextBuffer(size);
	}

	@Override
	protected void convert(Integer[] from, int[] to) {
		int pos = 0;
		for(int i=0; i<from.length; i++)
			to[i] = from[i];
	}

	@Override
	protected void arraycopy(int[] from, int fromOff, int[] to, int toOff, int len) {
		System.arraycopy(from, fromOff, to, toOff, len);
	}

	@Override
	public Integer get(int pos) {
		return buffer[pos];
	}

	@Override
	public void put(int pos, Integer value) {
		write(pos, new int[]{value}, 1);
	}

	@Override
	public int length(int[] of) {
		return of.length;
	}
	
}
