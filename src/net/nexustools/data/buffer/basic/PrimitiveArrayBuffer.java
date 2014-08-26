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

import java.util.Comparator;
import net.nexustools.data.accessor.DataAccessor;
import net.nexustools.data.buffer.MutableArrayBuffer;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public abstract class PrimitiveArrayBuffer<T, B> extends MutableArrayBuffer<T, B, B, Class<T>, DataAccessor.Reference> {

	public PrimitiveArrayBuffer(Class<T> typeClass, B buffer) {
		super(typeClass, buffer);
	}

	@Override
	protected final B convert(T[] from) {
		B buffer = create(from.length);
		try {
			convert(from, buffer);
		} catch(Throwable t) {
			release(buffer);
			throw NXUtils.unwrapRuntime(t);
		}
		return buffer;
	}

	@Override
	public final void writeImpl(int pos, B from, int off, int len) {
		int newSize = pos+len;
		int newLength = NXUtils.nearestPow(newSize);
		if(newLength < 256)
			newLength = 256;
		else if(newLength < length())
			newLength = Math.min(length(), newLength*newLength);
		if(newLength != length()) {
			//System.out.println((newLength > length() ? "Expand" : "Shrink") + "ing from " + length() + ":" + size + " to " + newLength + ":" + newSize);
			
			B newBuffer = create(newLength);
			int copy = Math.min(pos, size);
			//System.out.println("Copying " + copy + "bytes of old data");
			if(copy > 0)
				arraycopy(buffer, 0, newBuffer, 0, copy);
			setBuffer(newBuffer);
		}
		//System.out.println("Writing " + len+"bytes of new data");
		if(len > 0)
			arraycopy(from, off, buffer, pos, len);
		size = newSize;
	}

	@Override
	public final int readImpl(int pos, B to, int off, int len) {
		int read = Math.min(size-pos, len);
		if(read < 1)
			return -1;
		arraycopy(buffer, pos, to, off, len);
		return read;
	}
	
	protected abstract void convert(T[] from, B to);

	@Override
	protected final void setBuffer(B buffer) {
		this.buffer = buffer;
	}
	
	protected int length() {
		if(buffer == null)
			return 0;
		return length(buffer);
	}

	public void sort(Comparator<T> sortMethod) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Sorting with a sortMethod is not supported by " + getClass().getSimpleName());
	}

	@Override
	public B storage() {
		return buffer;
	}

	public final Reference refType() {
		return Reference.Strong;
	}
	
}
