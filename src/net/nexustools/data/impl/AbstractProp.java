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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public abstract class AbstractProp<T> implements PropAccessor<T> {
	
	public static <T> boolean isTrueHeiristic(T value) {
		if(value == null)
			return false;
		
		if(value instanceof String)
			return ((String)value).length() > 0;
		if(value instanceof Boolean)
			return ((Boolean)value);
		if(value instanceof Double)
			return ((Double)value) != 0;
		if(value instanceof Float)
			return ((Float)value) != 0;
		if(value instanceof Byte)
			return ((Byte)value) != 0;
		if(value instanceof Short)
			return ((Short)value) != 0;
		if(value instanceof Integer)
			return ((Integer)value) != 0;
		if(value instanceof Long)
			return ((Long)value) != 0;
		if(value instanceof Collection)
			return ((Collection)value).size() > 0;

		return true;
	}
	
	private final Class<T> typeClass;
	public AbstractProp(Class<T> typeClass) {
		this.typeClass = typeClass;
	}
	public AbstractProp(String typeClass) throws ClassNotFoundException {
		this((Class<T>)Class.forName(typeClass));
	}
	public AbstractProp() {
		this((Class<T>)null);
	}

	public T take() {
		try {
			return get();
		} finally {
			set(null);
		}
	}

	public void set(T value, Testable<T> condition) {
		try {
			if(condition.test(get()))
				set(value);
		} catch (Throwable ex) {
			if(ex instanceof RuntimeException)
				throw (RuntimeException)ex;
			throw new RuntimeException(ex);
		}
	}

	public boolean update(T value) {
		if(get() != value) {
			set(value);
			return true;
		} else
			return false;
	}

	public boolean isTrue() {
		return isTrueHeiristic(get());
	}

	public Class<T> type() {
		return typeClass;
	}

	public boolean isset() {
		return get() != null;
	}

	public void clear() {
		set(null);
	}
	
}
