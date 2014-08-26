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

import java.util.List;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class ImmutableTypeList<T, C, R> extends BufferList<T, C, R, TypeBuffer<T, ?, C, R>> {
	
	public ImmutableTypeList(Class<T> typeClass, Reference reference, T... elements) {
		super(TypeBuffer.create(typeClass, reference, elements));
	}

	public boolean unique(T object) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".unique is Immutable");
	}

	public void pushAll(Iterable<T> object) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".pushAll is Immutable");
	}

	public void unshiftAll(Iterable<T> object) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".unshiftAll is Immutable");
	}

	public void push(T object) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".push is Immutable");
	}

	public void unshift(T object) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".unshift is Immutable");
	}

	public void insert(T object, int at) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".insert is Immutable");
	}

	public void remove(T object) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".remove is Immutable");
	}

	public T remove(int at) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".remove is Immutable");
	}

	public T shift() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".shift is Immutable");
	}

	public T pop() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".pop is Immutable");
	}

	public void clear() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".clear is Immutable");
	}

	public List<T> toList() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void sort(Comparable<T> sortMethod) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> take(Testable<T> shouldTake) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> copy(Testable<T> shouldCopy) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> copy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public T[] toArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Object[] toObjectArray() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> take() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
