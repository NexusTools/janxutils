/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author katelyn
 * 
 * @param <T>
 */
public class WeakArrayList<T> implements Collection<T> {
	
	private final ArrayList<WeakReference<T>> collection = new ArrayList();

	@Override
	public int size() {
		int size = 0;
		Iterator it = iterator();
		while(it.hasNext())
			size ++;
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size() > 0;
	}

	@Override
	public boolean contains(Object o) {
		Iterator it = iterator();
		while(it.hasNext())
			if(it.next().equals(o))
				return true;
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<WeakReference<T>> it = collection.iterator();
		return new Iterator<T>() {
			
			private T nextObject;

			@Override
			public boolean hasNext() {
				while(it.hasNext()) {
					nextObject = it.next().get();
					if(nextObject == null)
						it.remove();
					else
						return true;
				}
				return false;
			}

			@Override
			public T next() {
				return nextObject;
			}

			@Override
			public void remove() {
				it.remove();
			}
			
		};
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean add(T e) {
		return collection.add(new WeakReference(e));
	}

	@Override
	public boolean remove(Object o) {
		Iterator it = iterator();
		while(it.hasNext())
			if(it.next().equals(o)) {
				it.remove();
				return true;
			}
		return false;
	}

	@Override
	public boolean containsAll(Collection c) {
		for(Object o : c)
			if(!contains((T)o))
				return false;
		return true;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean changed = false;
		for(Object o : c)
			if(add((T)o))
				changed = true;
		return changed;
	}

	@Override
	public boolean removeAll(Collection c) {
		boolean changed = false;
		for(Object o : c)
			if(remove((T)o))
				changed = true;
		return changed;
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void clear() {
		collection.clear();
	}
	
}
