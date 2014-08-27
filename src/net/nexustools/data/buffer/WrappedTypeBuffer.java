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
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.utils.NXUtils;


/**
 *
 * @author katelyn
 */
public abstract class WrappedTypeBuffer<T, W, C, R> extends TypeBuffer<T, W, C, R> {
	
	public class WrappedTypeIterator extends MutableIterator {

		T current;
		public WrappedTypeIterator(int at) {
			super(at);
		}

		public boolean hasNext() {
			if(buffer != null) {
				int dead = 0;
				int offset = pos;
				try {
					while(offset < size) {
						current = unwrap(buffer[offset++]);
						if(current != null)
							return true;
						
						dead ++;
					}
				} finally {
					if(dead > 0)
						remove(dead);
				}
			}
			
			return false;
		}

		public T next() {
			if(current == null && !hasNext())
				throw new NoSuchElementException();
			
			try {
				return current;
			} finally {
				current = null;
				pos++;
			}
		}

		public boolean hasPrevious() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		public T previous() {
			if(current == null && !hasPrevious())
				throw new NoSuchElementException();
			
			try {
				return current;
			} finally {
				current = null;
				pos--;
			}
		}
		
	}
	
	public WrappedTypeBuffer(C typeClass, W... elements) {
		super(typeClass, elements);
	}
	
	protected abstract W wrap(T object);
	protected abstract T unwrap(W object);
	protected abstract W[] createwrap(int size);
	protected abstract void releasewrap(W[] wrap);
	
	protected W[] wrap(T... objects) {
		return wrap(objects, 0, objects.length);
	}
	protected W[] wrap(T[] objects, int offset, int count) {
		W[] wrap = createwrap(count);
		try {
			for(int i=0; i<count; i++)
				wrap[i] = wrap(objects[i+offset]);
			return wrap;
		} finally {
			releasewrap(wrap);
		}
	}

	@Override
	protected void release(T[] buffer) {}

	@Override
	protected void setBuffer(T[] buffer) {
		this.buffer = wrap(buffer);
	}

	@Override
	public T[] copy() {
		StrongTypeList<T> copyBuffer = new StrongTypeList<T>();
		for(T object : this)
			copyBuffer.push(object);
		return copyBuffer.buffer.take();
	}

	@Override
	public W[] storage() {
		return buffer;
	}

	@Override
	public T get(int pos) {
		return bufferIterator(pos).next();
	}

	@Override
	public int readImpl(int pos, T[] to, int off, int len) {
		/*int read = 0;
		BufferIterator<T> it = bufferIterator(pos);
		
		return read;*/
		throw new RuntimeException();
	}
	
	

	@Override
	public void writeImpl(int pos, T[] from, int off, int len) {
		int newSize = pos+len;
		int newLength = NXUtils.nearestPow(newSize);
		if(newLength < 8)
			newLength = 8;
		else if(newLength < length())
			newLength = Math.min(length(), newLength*newLength);
		if(newLength != length()) {
			W[] newBuffer = createwrap(newLength);
			int copy = Math.min(pos, size);
			if(copy > 0)
				System.arraycopy(buffer, 0, newBuffer, 0, copy);
			buffer = newBuffer;
		}
		if(len > 0)
			System.arraycopy(wrap(from, off, len), off, buffer, pos, len);
		size = newSize;
	}

	@Override
	protected void deleteRange(int keepLeft, int gap, int keepRight) {
		int newSize = keepLeft + keepRight;
		if(keepRight <= gap)
			System.arraycopy(buffer, keepLeft+gap, buffer, keepLeft, gap);
		else {
			int end = length() - keepRight;
			if(end > size) {
				System.arraycopy(buffer, keepLeft+gap, buffer, end, gap);
				System.arraycopy(buffer, end, buffer, keepLeft, keepRight);
			} else {
				T[] newBuffer = create(newSize);
				if(keepLeft > 0)
					System.arraycopy(buffer, 0, newBuffer, 0, keepLeft);
				System.arraycopy(buffer, keepLeft+gap, newBuffer, keepLeft, keepRight);
				setBuffer(newBuffer);
			}
		}
		size = newSize;
	}

	@Override
	public BufferIterator<T> bufferIterator(int at) {
		return new WrappedTypeIterator(at);
	}
	
}
