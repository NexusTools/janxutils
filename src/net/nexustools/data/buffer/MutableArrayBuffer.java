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
import net.nexustools.io.StreamUtils;

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
		public void insert(T... elements) {
			int rem = size - pos - elements.length;
			if(rem > 0) {
				TA right = create(rem);
				int pos = size - rem;
				int read = read(pos, right);
				write(this.pos, convert(elements));
				write(pos, right, 0, read);
			} else
				write(pos, convert(elements));
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
		public void set(T e) {
			put(pos+moveOffset, e);
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
		size = 0;
	}
	
	public void delete(int pos, int count) {
		if(pos < 0)
			pos = size + (pos+1);
		if(pos < 0 || pos+count > size)
			throw new IllegalArgumentException(pos + ", " + count);
		
		int right = size - (pos+count);
		if(right < 1)
			deleteRight(pos);
		else
			deleteRange(pos, right, count);
	}
	
}
