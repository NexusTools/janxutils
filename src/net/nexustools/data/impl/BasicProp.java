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

import net.nexustools.data.accessor.DataAccessor.Reference;

/**
 *
 * @author katelyn
 */
public class BasicProp<T> extends AbstractProp<T> {
	
	
	protected T storage;
	public BasicProp(Class<T> typeClass, T object) {
		super(typeClass);
		storage = object;
	}
	public BasicProp(T object) {
		storage = object;
	}

	public final T get() {
		return storage;
	}

	public final void set(T value) {
		storage = value;
	}

	public final Reference refType() {
		return Reference.Strong;
	}
	
}
