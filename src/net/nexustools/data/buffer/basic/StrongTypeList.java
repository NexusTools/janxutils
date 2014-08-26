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

package net.nexustools.data.buffer.basic;

import net.nexustools.data.accessor.DataAccessor;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.data.buffer.TypeList;

/**
 *
 * @author katelyn
 */
public class StrongTypeList<T> extends TypeList<T, Class<T>, DataAccessor.Reference>  {

	public StrongTypeList(T... elements) {
		this((Class<T>)elements.getClass().getComponentType(), elements);
	}
	public StrongTypeList(Class<T> typeClass, T... elements) {
		super(typeClass, Reference.Strong, elements);
	}
	
}
