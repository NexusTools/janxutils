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


/**
 *
 * @author katelyn
 */
public abstract class WrappedTypeBuffer<T, W, C, R> extends TypeBuffer<T, W, C, R> {
	
	public WrappedTypeBuffer(C typeClass, W... elements) {
		super(typeClass, elements);
	}
	
	protected abstract W wrap(T object);
	protected abstract T unwrap(W object);
	protected abstract W[] createwrap(int size);
	protected abstract void releasewrap(W[] wrap);
	
	protected W[] wrap(T... objects) {
		W[] buffer = createwrap(objects.length);
		try {
			for(int i=0; i<objects.length; i++)
				buffer[i] = wrap(objects[i]);
			return buffer;
		} finally {
			releasewrap(buffer);
		}
	}
	
	protected T[] unwrap(W... objects) {
		T[] buffer = create(objects.length);
		try {
			for(int i=0; i<objects.length; i++)
				buffer[i] = unwrap(objects[i]);
			return buffer;
		} finally {
			release(buffer);
		}
	}

	@Override
	public T get(int pos) {
		return bufferIterator(pos).next();
	}

	@Override
	protected void release(T[] buffer) {}

	@Override
	protected void setBuffer(T[] buffer) {
		this.buffer = wrap(buffer);
	}

	@Override
	public T[] copy() {
		return unwrap(buffer);
	}

	@Override
	public W[] storage() {
		return buffer;
	}

	@Override
	public int readImpl(int pos, T[] to, int off, int len) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void writeImpl(int pos, T[] from, int off, int len) {
		
	}

	@Override
	protected void deleteRange(int pos, int count, int gap) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
