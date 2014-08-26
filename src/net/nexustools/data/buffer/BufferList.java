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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import net.nexustools.data.accessor.ListAccessor;

/**
 *
 * @author katelyn
 */
public abstract class BufferList<T, C, R, B extends ArrayBuffer<T, T[], ?, C, R>> implements ListAccessor<T> {
	
	protected final B buffer;
	protected BufferList(B typeBuffer) {
		buffer = typeBuffer;
	}

	public T first() {
		return buffer.bufferIterator().next();
	}

	public T get(int at) {
		return buffer.bufferIterator(at).next();
	}

	public T last() {
		return buffer.bufferIterator(-1).previous();
	}

	public int indexOf(T object) {
		return indexOf(object, 0);
	}

	public int indexOf(T object, int from) {
		Iterator<T> it = listIterator(from);
		while(it.hasNext()) {
			if(object.equals(it.next()))
				return from;
			from ++;
		}
		return -1;
	}

	public int lastIndexOf(T object, int from) {
		ListIterator<T> it = listIterator(from);
		while(it.hasPrevious()) {
			if(object.equals(it.previous()))
				return from;
			from ++;
		}
		return -1;
	}

	public int lastIndexOf(T object) {
		return lastIndexOf(object, -1);
	}

	public boolean contains(T object) {
		return indexOf(object) > -1;
	}

	public ListIterator<T> listIterator(int at) {
		return buffer.bufferIterator(at);
	}

	public ListIterator<T> listIterator() {
		return buffer.bufferIterator();
	}

	public List<T> toList() {
		return null;
	}

	public int length() {
		return buffer.size();
	}

	public boolean isTrue() {
		return buffer.isTrue();
	}

	public Iterator<T> iterator() {
		return buffer.bufferIterator();
	}

	public void sort(Comparator<T> sortMethod) {
		buffer.sort(sortMethod);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + buffer + ")";
	}
	
}
