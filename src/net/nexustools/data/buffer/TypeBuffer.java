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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 *
 * @author katelyn
 */
public abstract class TypeBuffer<T, TA, C, R> extends MutableArrayBuffer<T, T[], TA[], C, R> {
	
	public static <T> TypeBuffer create(T... elements) {
		return create((Class<T>)elements.getClass(), Reference.Strong, elements);
	}
	public static <T> TypeBuffer create(Class<T> forType, T... elements) {
		return create(forType, Reference.Strong, elements);
	}
	public static <T> TypeBuffer create(Class<T> forType, Reference reference, T... elements) {
		switch(reference) {
			case Strong:
				return new StrongTypeBuffer<T>(forType, elements);
				
			case Weak:
				return new ReferenceTypeBuffer<T, WeakReference<T>>(forType, Reference.Soft, elements);
				
			case Soft:
				return new ReferenceTypeBuffer<T, SoftReference<T>>(forType, Reference.Soft, elements);
		}
		
		throw new UnsupportedOperationException("Cannot created TypeBuffer for " + forType);
	}
	
	public TypeBuffer(C typeClass, TA... elements) {
		super(typeClass, elements);
		size = elements == null ? 0 : elements.length;
	}

	@Override
	protected final T[] convert(T[] from) {
		return from;
	}

	@Override
	protected final void arraycopy(T[] from, int fromOff, T[] to, int toOff, int len) {
		System.arraycopy(from, fromOff, to, toOff, len);
	}

	@Override
	public void put(int pos, T value) {
		T[] put = create(1);
		write(0, put);
	}

	@Override
	public final int length(T[] of) {
		return of.length;
	}
	
}
