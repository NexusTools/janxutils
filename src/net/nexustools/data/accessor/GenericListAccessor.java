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

package net.nexustools.data.accessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class GenericListAccessor<I> implements ListAccessor<I> {
	
	private ArrayList<I> list;
	protected GenericListAccessor(ArrayList<I> items) {
		list = items;
	}
	public GenericListAccessor(Collection<I> items) {
		list = new ArrayList(items);
	}
	public GenericListAccessor() {
		list = new ArrayList();
	}

	public void push(I object) {
		list.add(object);
	}
	public void unshift(I object) {
		insert(object, 0);
	}
	public void insert(I object, int at) {
		list.add(at, object);
	}
	public void remove(I object) {
		list.remove(object);
	}
	public I remove(int at) {
		return list.remove(at);
	}
	public int indexOf(I object) {
		return indexOf(object, 0);
	}
	public int indexOf(I object, int from) {
		ListIterator<I> listIterator = list.listIterator(from);
		try {
			while(true) {
				int index = listIterator.nextIndex();
				if(listIterator.next().hashCode() == object.hashCode())
					return index;
			}
		} catch(NoSuchElementException ex) {
			return -1;
		}
	}
	public int lastIndexOf(I object, int from) {
		ListIterator<I> listIterator = list.listIterator(from);
		try {
			while(true) {
				int index = listIterator.previousIndex();
				if(index == -1 || listIterator.previous().hashCode() == object.hashCode())
					return index;
			}
		} catch(NoSuchElementException ex) {
			return -1;
		}
	}
	public int lastIndexOf(I object) {
		return lastIndexOf(object, list.size());
	}
	public int length() {
		return list.size();
	}
	public I shift() {
		try {
			return list.remove(0);
		} catch(IndexOutOfBoundsException ex) {
			return null;
		}
	}
	public I pop() {
		try {
			return list.remove(list.size()-1);
		} catch(IndexOutOfBoundsException ex) {
			return null;
		}
	}
	public boolean isTrue() {
		return list.size() > 0;
	}
	public boolean isset() {
		return true;
	}
	public void clear() {
		list.clear();
	}
	public Iterator<I> iterator() {
		return list.iterator();
	}
	public boolean unique(I object) {
		if(contains(object))
			return false;

		list.add(object);
		return true;
	}
	public boolean contains(I object) {
		return list.contains(object);
	}
	public I first() {
		return list.get(0);
	}
	public I get(int at) {
		return list.get(at);
	}
	public I last() {
		return list.get(list.size()-1);
	}

	public ListIterator<I> listIterator() {
		return list.listIterator();
	}

	public void pushAll(Iterable<I> objects) {
		if(objects instanceof List) {
			list.addAll((List)objects);
			return;
		}
		for(I obj : objects)
			push(obj);
	}

	public void unshiftAll(Iterable<I> objects) {
		List<I> current = list;
		list = new ArrayList<I>();
		if(objects instanceof List)
			list.addAll((List)objects);
		else
			for(I obj : objects)
				unshift(obj);
		list.addAll(current);
	}

	public ListIterator<I> listIterator(int where) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ArrayList<I> toList() {
		return list;
	}

	public ListAccessor<I> immutable() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<I> immutableCopy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<I> immutableCopy(Testable<I> shouldCopy) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void sort(Comparator<I> sortMethod) {
		Collections.sort(list, sortMethod);
	}

	public ListAccessor<I> take(Testable<I> shouldTake) {
		try {
			ArrayList taken = new ArrayList();
			ListIterator<I> it = list.listIterator();
			while(it.hasNext()) {
				I next = it.next();
				if(shouldTake.test(next)) {
					taken.add(next);
					it.remove();
				}
			}
			return new GenericListAccessor(taken);
		} finally {
			list = new ArrayList();
		}
	}

	public ListAccessor<I> copy(Testable<I> shouldCopy) {
		try {
			ArrayList copy = new ArrayList();
			for(I value : list)
				if(shouldCopy.test(value))
					copy.add(value);
			return new GenericListAccessor(copy);
		} finally {
			list = new ArrayList();
		}
	}

	public ListAccessor<I> copy() {
		return new GenericListAccessor(new ArrayList(list));
	}

	public I[] toArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public ListAccessor<I> take() {
		try {
			return new GenericListAccessor(list);
		} finally {
			list = new ArrayList();
		}
	}

	public Object[] toObjectArray() {
		return list.toArray();
	}
	
}
