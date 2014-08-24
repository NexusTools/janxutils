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

import java.util.ListIterator;

/**
 *
 * @author katelyn
 */
public interface IterableAccessor<T, C, R, S extends IterableAccessor<T, C, R, S>> extends DataAccessor<T, C, R>, Iterable<T> {
	
	public static interface Iterator<T> {
		public void iterate(ListIterator<T> it);
	}
	
	public void iterate(Iterator<T> iterator);
	public void iterate(Iterator<T> iterator, int at);
	public S sorted(Comparable<T> sortMethod);
	
	public int length();
	public T[] copy();
	public T[] take();
	
}
