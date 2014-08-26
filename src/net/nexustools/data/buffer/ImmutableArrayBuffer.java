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

/**
 *
 * @author katelyn
 */
public abstract class ImmutableArrayBuffer<T, TA, B, C, R> extends ArrayBuffer<T, TA, B, C, R> {
	
	protected abstract class ImmutableArrayIterator extends ArrayIterator {
		public ImmutableArrayIterator(int at) {
			super(at);
		}
		public void add(T... e) {
			throw new UnsupportedOperationException(getClass().getSimpleName() + ".add is Immutable");
		}
		public void remove(int previous, int next) {
			throw new UnsupportedOperationException(getClass().getSimpleName() + ".remove is Immutable");
		}
		@Override
		public void replace(int from, T... elements) {
			throw new UnsupportedOperationException(getClass().getSimpleName() + ".replace is Immutable");
		}
	}

	public ImmutableArrayBuffer(C typeClass, B buffer) {
		super(typeClass, buffer);
	}
	
	public void clear() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".clear is Immutable");
	}

	@Override
	public BufferIterator<T> bufferIterator() {
		return bufferIterator(0);
	}
	
	

//	public BufferIterator<T> bufferIterator(int at) {
//		at = at >= 0 ? at : length() + (at + 1);
//		if(at < 0 || at > length())
//			throw new IndexOutOfBoundsException();
//		return new ImmutableBufferIterator(at);
//	}
	
}
