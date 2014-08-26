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
		System.out.println("Writing " + len + "bytes");
		int newSize = pos+len;
		int newLength = NXUtils.nearestPow(newSize);
		if(newLength < 8)
			newLength = 8;
		else if(newLength < length())
			newLength = Math.min(length(), newLength*newLength);
		if(newLength != length()) {
			System.out.println((newLength > length() ? "Expand" : "Shrink") + "ing from " + length() + ":" + size + " to " + newLength + ":" + newSize);
			
			T[] newBuffer = create(newLength);
			int copy = Math.min(pos, size);
			if(copy > 0) {
				System.out.println("Copying " + copy+"bytes of old data");
				arraycopy(buffer, 0, newBuffer, 0, copy);
			}
			setBuffer(newBuffer);
		}
		if(len > 0) {
			System.out.println("Writing " + len+"bytes of new data");
			arraycopy(from, off, buffer, pos, len);
		}
		size = newSize;
	}

	@Override
	protected int length() {
		return buffer == null ? 0 : buffer.length;
	}

	public void sort(Comparator<T> sortMethod) throws UnsupportedOperationException {
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
