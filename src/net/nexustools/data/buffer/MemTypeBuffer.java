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

package net.nexustools.data.buffer;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ListIterator;
import net.nexustools.data.accessor.DataAccessor;
import net.nexustools.data.impl.AbstractIterable;

/**
 *
 * @author katelyn
 */
public class MemTypeBuffer<T> extends AbstractIterable<T, Class<T>, DataAccessor.Reference, TypeBuffer<T>> implements TypeBuffer<T> {
	
	protected int size;
	protected T[] buffer;
	public MemTypeBuffer(Class<T> typeClass) {
		super(typeClass);
	}
	
	protected T[] create(int count) {
		return (T[]) Array.newInstance(typeClass, count);
	}
	
	public void clear() {
		size = 0;
	}

	public BufferIterator<T> bufferIterator(final int where) {
		return new BufferIterator<T>() {
			int pos = where >= 0 ? where : size + where + 1;
			public boolean hasNext() {
				return buffer != null && pos < size;
			}
			public T next() {
				return buffer[pos++];
			}
			public boolean hasPrevious() {
				return pos > 0;
			}
			public T previous() {
				return buffer[--pos];
			}
			public int nextIndex() {
				return pos;
			}
			public int previousIndex() {
				return pos-1;
			}
			public void remove() {
				remove(1);
			}
			public void remove(int count) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
			public void set(T e) {
				set((T[])e);
			}
			public void set(T... e) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
			public void add(T e) {
				add((T[])e);
			}
			public void add(T... e) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}

		};
	}

	@Override
	protected BufferIterator<T> listIterator(int at) {
		return bufferIterator(0);
	}

	public BufferIterator<T> bufferIterator() {
		return bufferIterator(0);
	}

	public int length() {
		return size;
	}

	public T[] copy() {
		if(buffer == null)
			return null;
		return Arrays.copyOf(buffer, size);
	}

	public T[] take() {
		try {
			return buffer;
		} finally {
			buffer = null;
			size = 0;
		}
	}

	public boolean isTrue() {
		return size > 0;
	}

	public Reference refType() {
		return Reference.Strong;
	}
	
}
