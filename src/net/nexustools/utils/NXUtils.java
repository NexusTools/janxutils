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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author katelyn
 */
public final class NXUtils {
	
	public static Throwable unwrapTarget(Throwable throwable) {
		if(throwable instanceof InvocationTargetException)
			return unwrapTarget(((InvocationTargetException)throwable).getTargetException());
		if(throwable instanceof RuntimeTargetException)
			return unwrapTarget(((RuntimeTargetException)throwable).target());
		return throwable;
	}
	public static RuntimeException unwrapRuntime(Throwable throwable) {
		throwable = unwrapTarget(throwable);
		if(throwable instanceof RuntimeException)
			return (RuntimeException)throwable;
		return new RuntimeException(throwable);
	}
	public static IOException unwrapIOException(Throwable throwable) {
		throwable = unwrapTarget(throwable);
		if(throwable instanceof IOException)
			return (IOException)throwable;
		if(throwable instanceof RuntimeException)
			throw (RuntimeException)throwable;
		return new IOException(throwable);
	}

	public static RuntimeTargetException wrapRuntime(Throwable throwable) {
		throwable = unwrapTarget(throwable);
		if(throwable instanceof RuntimeTargetException)
			throw (RuntimeTargetException)throwable;
		throw new RuntimeTargetException(throwable);
	}
	
}
