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

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public abstract class GenericTypeBuffer<T, C, R> extends TypeBuffer<T, T, C, R> {

	public GenericTypeBuffer(C typeClass, T... elements) {
		super(typeClass, elements);
	}

	public T[] copy() {
		if(buffer == null)
			return null;
		return Arrays.copyOf(buffer, size());
	}

	@Override
	public void put(int pos, T value) {
		T[] put = create(1);
		put[0] = value;
		write(pos, put, 1);
	}

	@Override
	public T[] storage() {
		return buffer;
	}

	@Override
	public int readImpl(int pos, T[] to, int off, int len) {
		int read = Math.min(size-pos, len);
		if(read < 1)
			return -1;
		arraycopy(buffer, pos, to, off, len);
		return read;
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
			T[] newBuffer = create(newLength);
			int copy = Math.min(pos, size);
			if(copy > 0)
				arraycopy(buffer, 0, newBuffer, 0, copy);
			setBuffer(newBuffer);
		}
		if(len > 0)
			arraycopy(from, off, buffer, pos, len);
		size = newSize;
	}

	@Override
	protected void deleteRange(final int keepLeft, final int keepRight, final int gap) {
		int newSize = keepLeft + keepRight;
		if(keepRight <= gap)
			arraycopy(buffer, keepLeft+gap, buffer, keepLeft, gap);
		else {
			int end = length() - keepRight;
			if(end > size) {
				arraycopy(buffer, keepLeft+gap, buffer, end, gap);
				arraycopy(buffer, end, buffer, keepLeft, keepRight);
			} else {
				T[] newBuffer = create(newSize);
				if(keepLeft > 0)
					arraycopy(buffer, 0, newBuffer, 0, keepLeft);
				arraycopy(buffer, keepLeft+gap, newBuffer, keepLeft, keepRight);
				setBuffer(newBuffer);
			}
		}
		size = newSize;
	}

	@Override
	protected int length() {
		return buffer == null ? 0 : buffer.length;
	}

	public void sort(Comparator<T> sortMethod) throws UnsupportedOperationException {
		if(buffer != null)
			Arrays.sort(buffer, sortMethod);
	}

	@Override
	protected void release(T[] buffer) {}

	@Override
	public T get(int off) {
		try {
			assert(off >= 0 && off < size);
			return buffer[off];
		} catch(ArrayIndexOutOfBoundsException ex) {
			throw new NoSuchElementException();
		}
	}
	
}
