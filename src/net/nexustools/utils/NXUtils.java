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
import net.nexustools.data.analyze.ClassDefinition;

/**
 *
 * @author katelyn
 */
public final class NXUtils {
	public static Runnable NOP = new Runnable() {
		public void run() {}
	};
	public static Runnable FOREVER = new Runnable() {
		public void run() {
			while(true)
				try {
					Thread.sleep(50000);
				} catch (InterruptedException ex) {}
		}
	};
	
	public static <T extends Throwable> T unwrapTarget(Class<T> target, Throwable throwable) {
		throwable = unwrapTarget(throwable);
		if(target.isAssignableFrom(throwable.getClass()))
			return target.cast(throwable);
		throw wrapRuntime(throwable);
	}
	public static Throwable unwrapTarget(Throwable throwable) {
		if(throwable instanceof InvocationTargetException)
			return unwrapTarget(((InvocationTargetException)throwable).getTargetException());
		if(throwable instanceof RuntimeTargetException)
			return unwrapTarget(((RuntimeTargetException)throwable).target());
		return throwable;
	}
	public static UnsupportedOperationException unwrapOperationException(Throwable throwable) {
		return unwrapTarget(UnsupportedOperationException.class, throwable);
	}
	public static IOException unwrapIOException(Throwable throwable) {
		return unwrapTarget(IOException.class, throwable);
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
	
	public static int remaining(long amount) {
		if(amount > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int)amount;
	}
	
	public static int remainingMin(long amount, long amount2) {
		return remaining(Math.min(amount, amount2));
	}

	public static String toString(Object target, Pair<String, Object>... vars) {
		StringBuilder builder = new StringBuilder();
		builder.append(ClassDefinition.shortNameFor(target.getClass()));
		if(vars.length > 0) {
			builder.append('{');
			boolean addComma = false;
			for(Pair<String, Object> var : vars) {
				if(addComma)
					builder.append(',');
				else
					addComma = true;
				builder.append(var.i);
				builder.append("=");
				builder.append(var.v);
			}
			builder.append('}');
		}
		return builder.toString();
	}

	public static void passException(Throwable targetException) throws RuntimeTargetException {
		throw wrapRuntime(targetException);
	}
	public static void passIOException(Throwable targetException) throws IOException {
		passException(IOException.class, targetException);
	}
	public static <T extends Throwable> void passException(Class<T> wrappedException, Throwable targetException) throws T {
		throw unwrapTarget(wrappedException, targetException);
	}
	public static <T extends Throwable> void throwException(Class<T> throwableClass, Object source, String method, String message, Throwable cause) throws T {
		StringBuilder error = new StringBuilder();
		error.append(source.getClass().getName());
		error.append('.');
		error.append(method);
		error.append(": ");
		error.append(message);
		
		try {
			if(cause == null)
				throw throwableClass.getConstructor(String.class).newInstance(error.toString());
			throw throwableClass.getConstructor(String.class, Throwable.class).newInstance(error.toString(), cause);
		} catch (InstantiationException ex) {
			throw wrapRuntime(ex);
		} catch (IllegalAccessException ex) {
			throw wrapRuntime(ex);
		} catch (IllegalArgumentException ex) {
			throw wrapRuntime(ex);
		} catch (InvocationTargetException ex) {
			throw wrapRuntime(ex);
		} catch (NoSuchMethodException ex) {
			throw wrapRuntime(ex);
		} catch (SecurityException ex) {
			throw wrapRuntime(ex);
		}
	}
	public static <T extends Throwable> void throwException(Class<T> throwableClass, Object source, String method, String message) throws T {
		throwException(throwableClass, source, method, message, null);
	}
	
}
