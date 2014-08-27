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
import net.nexustools.data.accessor.DataAccessor.CacheLifetime;
import net.nexustools.data.buffer.TypeList;

/**
 *
 * @author katelyn
 */
public class CacheTypeList<T> extends TypeList<T, Class<T>, DataAccessor.CacheLifetime>  {

	public CacheTypeList(T... elements) {
		this((Class<T>)elements.getClass().getComponentType(), elements);
	}
	public CacheTypeList(CacheLifetime type, T... elements) {
		super((Class<T>)elements.getClass().getComponentType(), type, elements);
	}
	public CacheTypeList(Class<T> typeClass, T... elements) {
		this(typeClass, CacheLifetime.Medium, elements);
	}
	public CacheTypeList(Class<T> typeClass, CacheLifetime type, T... elements) {
		super(typeClass, type, elements);
	}
	
}
