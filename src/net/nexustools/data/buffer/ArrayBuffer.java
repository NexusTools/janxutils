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

import net.nexustools.data.accessor.IterableAccessor;

/**
 *
 * @author katelyn
 */
public abstract class ArrayBuffer<T, TA, B, C, R> implements IterableAccessor<T, C, R> {
	
	protected abstract class ArrayIterator extends BufferIterator<T> {
		protected int pos;
		public ArrayIterator(int at) {
			pos = at;
		}
		public int nextIndex() {
			return pos;
		}
		public int previousIndex() {
			return pos-1;
		}
	}
	
	
	protected B buffer;
	protected final C typeClass;
	public ArrayBuffer(C typeClass, B buffer) {
		this.buffer = buffer;
		this.typeClass = typeClass;
	}
	
	public abstract TA copy();
	public abstract TA take();
	public abstract B storage();
	
	public C type() {
		return typeClass;
	}
	
	public abstract T get(int pos);
	protected abstract int length(TA of);
	protected abstract int readImpl(int pos, TA to, int off, int len);
	protected abstract void writeImpl(int pos, TA from, int off, int len);
	
	public int read(int pos, TA from, int off, int len) {
		if(pos < 0) {
			System.out.println("Inverted Access: " + pos + ": " + (pos + 1) + ", " + size());
			pos = size() + (pos + 1);
		}
		return readImpl(pos, from, 0, len);
	}
	public void write(int pos, TA from, int off, int len) {
		if(pos < 0) {
			System.out.println("Inverted Access: " + pos + ": " + (pos + 1) + ", " + size());
			pos = size() + (pos + 1);
		}
		writeImpl(pos, from, 0, len);
	}
	
	public int read(int pos, TA from, int len) {
		return read(pos, from, 0, len);
	}
	public void write(int pos, TA from, int len) {
		write(pos, from, 0, len);
	}
	
	public int read(int pos, TA from) {
		return read(pos, from, length(from));
	}
	public void write(int pos, TA from) {
		write(pos, from, length(from));
	}
	
	protected abstract void arraycopy(TA from, int fromOff, TA to, int toOff, int len);
	
	public abstract BufferIterator<T> bufferIterator(int at);
	
	public BufferIterator<T> bufferIterator() {
		return bufferIterator(0);
	}
	public java.util.Iterator<T> iterator() {
		return bufferIterator();
	}
	public void iterate(IterableAccessor.Iterator<T> iterator) {
		iterator.iterate(bufferIterator());
	}
	public void iterate(IterableAccessor.Iterator<T> iterator, int at) {
		iterator.iterate(bufferIterator(at));
	}

	public boolean isTrue() {
		return size() > 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		boolean first = true;
		for(T object : this) {
			if(first)
				first = false;
			else
				builder.append(',');
			builder.append(object);
		}
		builder.append(']');
		
		return builder.toString();
	}
	
}
