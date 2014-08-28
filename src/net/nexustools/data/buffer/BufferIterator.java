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

import java.util.ListIterator;

/**
 *
 * @author katelyn
 */
public abstract class BufferIterator<T> implements ListIterator<T> {
	
	public abstract void insert(T... e);
	public abstract void remove(int offset, int count);

	public void remove() {
		remove(1);
	}
	public void remove(int count) {
		assert(count > 0);
		remove(0, count);
	}
	public void add(T e) {
		insert(e);
	}
	
}
