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

package net.nexustools.concurrent;

import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author katelyn
 */
public interface ListAccessor<T> extends BaseAccessor, Iterable<T> {
	
	/**
	 * Similar to push, except checks if an instance already exists.
	 * 
	 * @param object
	 * @return true if added, false otherwise
	 */
	public boolean unique(T object);
	
	public T first();
	public T get(int at);
	public T last();
	
	public void push(T object);
	public void unshift(T object);
	public void insert(T object, int at);
	public void remove(T object);
	public T remove(int at);
	
	public int indexOf(T object);
	public int indexOf(T object, int from);
	public int lastIndexOf(T object, int from);
	public int lastIndexOf(T object);
	public boolean contains(T object);
	
	public ListIterator<T> listIterator();
	
	public int length();
	public List<T> copy();
	public List<T> take();
	
	public T shift();
	public T pop();
	
}
