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

package net.nexustools.utils;

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author katelyn
 * 
 * @param <T>
 */
public class WeakArrayList<T> extends AbstractCollection<T> {
	
	private final ArrayList<WeakReference<T>> collection = new ArrayList();
	
	public WeakArrayList() {}

	@Override
	public boolean add(T e) {
		return collection.add(new WeakReference(e));
	}

	@Override
	public boolean remove(Object o) {
		Iterator it = iterator();
		while(it.hasNext())
			if(it.next().hashCode() == o.hashCode()) {
				it.remove();
				return true;
			}
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
	public int size() {
		int size = 0;
		Iterator it = iterator();
		while(it.hasNext())
			size ++;
		return size;
	}

}
