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
	public static IOException unwrapIOException(Throwable throwable) {
		throwable = unwrapTarget(throwable);
		if(throwable instanceof IOException)
			return (IOException)throwable;
		throw wrapRuntime(throwable);
	}

	public static RuntimeTargetException wrapRuntime(Throwable throwable) {
		throwable = unwrapTarget(throwable);
		if(throwable instanceof RuntimeException)
			throw (RuntimeException)throwable;
		if(throwable instanceof RuntimeTargetException)
			return (RuntimeTargetException)throwable;
		return new RuntimeTargetException(throwable);
	}
	public static InvocationTargetException wrapInvocation(Throwable throwable) {
		if(throwable instanceof InvocationTargetException)
			return (InvocationTargetException)throwable;
		return new InvocationTargetException(unwrapTarget(throwable));
	}
	
	public final static int nearestPow(int size) {
		if(size < 8)
			return 8;
		
		return (int) Math.pow(2, Math.ceil(Math.log(size)/Math.log(2)));
	}
	
}
