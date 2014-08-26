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

import java.lang.reflect.Array;
import java.util.Arrays;
import net.nexustools.data.accessor.DataAccessor.Reference;

/**
 *
 * @author katelyn
 */
public class StrongTypeBuffer<T> extends GenericTypeBuffer<T, Class<T>, Reference> {

	public StrongTypeBuffer(Class<T> typeClass, T... elements) {
		super(typeClass, elements);
	}

	@Override
	protected T[] create(int size) {
		return (T[])Array.newInstance(typeClass, size);
	}

	public Reference refType() {
		return Reference.Strong;
	}

	@Override
	protected void setBuffer(T[] buffer) {
		this.buffer = buffer;
	}
	
}
