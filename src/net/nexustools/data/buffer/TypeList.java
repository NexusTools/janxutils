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
import java.util.ListIterator;
import java.util.NoSuchElementException;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class TypeList<T, C, R> extends BufferList<T, C, R, TypeBuffer<T, ?, C, R>> {
	
	protected final Class<T> typeClass;
	protected final Reference reference;
	public TypeList(Class<T> typeClass, Reference reference, T... elements) {
		super(TypeBuffer.create(typeClass, reference, elements));
		this.typeClass = typeClass;
		this.reference = reference;
	}
	
	public Reference referenceType() {
		return reference;
	}

	public boolean unique(T object) {
		ListIterator<T> it = listIterator();
		while(it.hasNext()) {
			if(object.equals(it.next()))
				return false;
		}
		it.add(object);
		return true;
	}

	public void pushAll(Iterable<T> objects) {
		for(T object : objects)
			pushAll(object);
	}

	public void unshiftAll(Iterable<T> objects) {
		for(T object : objects)
			unshiftAll(object);
	}

	public void push(final T object) {
		pushAll(object);
	}

	public void unshift(T object) {
		unshiftAll(object);
	}

	public void pushAll(T... objects) {
		buffer.write(-1, objects);
	}

	public void unshiftAll(T...objects) {
		buffer.bufferIterator().add(objects);
	}

	public void insert(T object, int at) {
		buffer.bufferIterator(at).add(object);
	}

	public void remove(T object) {
		ListIterator<T> it = listIterator();
		while(it.hasNext()) {
			if(object.equals(it.next()))
				it.remove();
		}
	}

	public T remove(int at) {
		BufferIterator<T> it = buffer.bufferIterator(at);
		try {
			return it.next();
		} finally {
			it.remove();
		}
	}

	public T shift() {
		try {
			return remove(0);
		} catch(NoSuchElementException ex) {
			return null;
		}
	}

	public T pop() {
		try {
			return remove(-1);
		} catch(NoSuchElementException ex) {
			return null;
		}
	}

	public void clear() {
		buffer.clear();
	}

	@Override
	public List<T> toList() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ListAccessor<T> take(Testable<T> shouldTake) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> copy(Testable<T> shouldCopy) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> copy() {
		return new TypeList<T, C, R>(typeClass, reference, buffer.copy());
	}

	public T[] toArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Object[] toObjectArray() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<T> take() {
		return new TypeList(typeClass, reference, buffer.take());
	}
	
}
