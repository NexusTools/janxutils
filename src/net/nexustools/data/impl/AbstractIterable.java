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

package net.nexustools.data.impl;

import java.util.ListIterator;
import net.nexustools.data.accessor.IterableAccessor;
import net.nexustools.data.buffer.BufferIterator;

/**
 *
 * @author katelyn
 */
public abstract class AbstractIterable<T, C, R, S extends IterableAccessor<T, C, R, S>> implements IterableAccessor<T, C, R, S> {
	
	protected final C typeClass;
	public AbstractIterable() {
		this(null);
	}
	public AbstractIterable(C typeClass) {
		this.typeClass = typeClass;
	}
	protected abstract BufferIterator<T> listIterator(int at);

	public C type() {
		return typeClass;
	}

	public void iterate(Iterator<T> iterator) {
		iterate(iterator, 0);
	}

	public void iterate(Iterator<T> iterator, int at) {
		iterator.iterate(listIterator(at));
	}

	public S sorted(Comparable<T> sortMethod) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public java.util.Iterator<T> iterator() {
		return listIterator(0);
	}
	
}
