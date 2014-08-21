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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author katelyn
 */
public class ClassUtils {
	
	public static <M extends Map.Entry<? extends Class<?>, ?>> M bestMatch(Class<?> target, Set<M> entrySet) {
		M bestMatch = null;
		for(M entry : entrySet) {
			Class<?> test = entry.getKey();
			if(test.isAssignableFrom(target) && (bestMatch == null || bestMatch.getKey().isAssignableFrom(test)))
				bestMatch = entry;
		}
		return bestMatch;
	}
	
	public static <C, U> Creator<C, U> creator(Class<? extends C> create, Class<? extends U> using) {
		try {
			final Constructor<? extends C> constructor = create.getConstructor(using);
			return new Creator<C, U>() {
				public C create(U using) {
					try {
						return constructor.newInstance(using);
					} catch (InstantiationException ex) {
						throw new RuntimeException(ex);
					} catch (IllegalAccessException ex) {
						throw new RuntimeException(ex);
					} catch (IllegalArgumentException ex) {
						throw new RuntimeException(ex);
					} catch (InvocationTargetException ex) {
						throw new RuntimeException(ex);
					}
				}
			};
		} catch (NoSuchMethodException ex) {
		} catch (SecurityException ex) {
		}
		
		try {
			final Constructor<? extends C> constructor = create.getConstructor();
			return new Creator<C, U>() {
				public C create(U using) {
					try {
						return constructor.newInstance();
					} catch (InstantiationException ex) {
						throw new RuntimeException(ex);
					} catch (IllegalAccessException ex) {
						throw new RuntimeException(ex);
					} catch (IllegalArgumentException ex) {
						throw new RuntimeException(ex);
					} catch (InvocationTargetException ex) {
						throw new RuntimeException(ex);
					}
				}
			};
		} catch (NoSuchMethodException ex) {
			throw new UnsupportedOperationException("No compatible constructors.");
		} catch (SecurityException ex) {
			throw new UnsupportedOperationException("No compatible constructors.");
		}
	}
	public static Creator<?, ?> creator(Class<?> create) {
		return creator(create, Void.class);
	}
	
}
