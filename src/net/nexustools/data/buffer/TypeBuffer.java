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
import java.util.NoSuchElementException;
import net.nexustools.utils.Pair;

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
				return new ReferenceTypeBuffer<T, WeakReference<T>>(forType, Reference.Weak, elements);
				
			case Soft:
				return new ReferenceTypeBuffer<T, SoftReference<T>>(forType, Reference.Soft, elements);
		}
		
		throw new UnsupportedOperationException("Cannot created TypeBuffer for " + forType);
	}
	
	
	public static <K, V> TypeBuffer createPair(Pair<K, V>... elements) {
		return create(new Pair(), elements);
	}
	public static <K, V> TypeBuffer createPair(Pair<Class<K>, Class<V>> forType, Pair<K, V>... elements) {
		return create(forType, new Pair(Reference.Strong, Reference.Strong), elements);
	}
	public static <K, V> TypeBuffer createPair(Pair<Class<K>, Class<V>> forType, Pair<Reference, Reference> reference, Pair<K, V>... elements) {
		if(reference.i == Reference.Strong && reference.v == Reference.Strong)
			return new StrongPairBuffer<K, V>(forType, elements);
		
		throw new UnsupportedOperationException("Cannot created PairBuffer for " + forType);
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
		put[0] = value;
		write(0, put);
	}

	@Override
	public T get(int pos) {
		T[] get = create(1);
		if(read(0, get) < 1)
			throw new NoSuchElementException();
		return get[0];
	}

	@Override
	public final int length(T[] of) {
		return of.length;
	}
	
}
