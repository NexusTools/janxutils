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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import net.nexustools.data.accessor.DataAccessor.Reference;

/**
 *
 * @author katelyn
 */
public class ReferenceTypeBuffer<T, W extends java.lang.ref.Reference<T>> extends WrappedTypeBuffer<T, W, Class<T>, Reference> {
	
	protected static <T, W extends java.lang.ref.Reference<T>> W[] wrap(Reference type, T... elements) {
		ArrayList<W> list = new ArrayList();
			
		switch(type) {
			case Weak:
				for(T element : elements)
					list.add((W)new WeakReference(element));
				return (W[]) list.toArray(new WeakReference[list.size()]);
				
			case Soft:
				for(T element : elements)
					list.add((W)new SoftReference(element));
				return (W[]) list.toArray(new SoftReference[list.size()]);
				
			default:
				throw new UnsupportedOperationException("ReferenceBuffers can only contain Weak or Strong referenced items.");
		}
	}

	final Reference type;
	public ReferenceTypeBuffer(Class<T> typeClass, Reference type, T... elements) {
		super(typeClass, (W[])wrap(type, elements));
		this.type = type;
	}
	public ReferenceTypeBuffer(Class<T> typeClass, T... elements) {
		this(typeClass, Reference.Weak, elements);
	}

	@Override
	protected T[] create(int size) {
		return (T[])Array.newInstance(typeClass, size);
	}

	@Override
	protected W wrap(T object) {
		switch(type) {
			case Soft:
				return (W)new SoftReference<T>(object);
				
			case Weak:
				return (W)new WeakReference<T>(object);
		}
		throw new RuntimeException();
	}

	@Override
	protected T unwrap(W object) {
		return object.get();
	}

	@Override
	protected W[] createwrap(int size) {
		switch(type) {
			case Soft:
				return (W[])new SoftReference[size];
				
			case Weak:
				return (W[])new WeakReference[size];
		}
		throw new RuntimeException();
	}

	public void sort(Comparator<T> sortMethod) throws UnsupportedOperationException {
		T[] buffer = unwrap(this.buffer);
		Arrays.sort(buffer, sortMethod);
		this.buffer = wrap(buffer);
	}

	@Override
	protected void releasewrap(W[] wrap) {}
	
	public Reference refType() {
		return type;
	}

	@Override
	protected int length() {
		return buffer.length;
	}
	
}
