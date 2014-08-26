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

import java.util.NoSuchElementException;

/**
 *
 * @author katelyn
 */
public abstract class MutableArrayBuffer<T, TA, B, C, R> extends ArrayBuffer<T, TA, B, C, R> {
	
	protected class MutableIterator extends ArrayIterator {
		int moveOffset;
		public MutableIterator(int at) {
			super(at);
		}
		@Override
		public void add(T... elements) {
			throw new RuntimeException();
		}
		@Override
		public void replace(int from, T... elements) {
			throw new RuntimeException();
		}
		@Override
		public void remove(int offset, int count) {
			try {
				delete(pos+offset+moveOffset, count);
			} catch(java.lang.IllegalArgumentException ex) {
				throw new NoSuchElementException();
			}
		}
		public boolean hasNext() {
			return buffer != null && pos < size();
		}
		public boolean hasPrevious() {
			return buffer != null && pos > 0;
		}
		public T next() {
			moveOffset=-1;
			return get(pos++);
		}
		public T previous() {
			moveOffset=0;
			return get(--pos);
		}
	}

	protected int size;
	public MutableArrayBuffer(C typeClass, B buffer) {
		super(typeClass, buffer);
	}
	
	protected abstract TA create(int size);
	protected abstract TA convert(T[] from);
	protected abstract void release(TA buffer);
	protected abstract void setBuffer(TA buffer);
	public abstract void put(int pos, T value);
	
	@Override
	public BufferIterator<T> bufferIterator(int at) {
		return new MutableIterator(at);
	}

	@Override
	public final TA take() {
		try {
			return copy();
		} finally {
			clear();
		}
	}
	
	protected abstract int length();

	@Override
	protected final void deleteRight(int to) {
		size = to;
	}

	public final int size() {
		return size;
	}

	public final void clear() {
		buffer = null;
		size = 0;
	}
	
}
