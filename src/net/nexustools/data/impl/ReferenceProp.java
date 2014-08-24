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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.utils.Creator;

/**
 *
 * @author katelyn
 */
public class ReferenceProp<T> extends AbstractProp<T> {
	
	protected final Reference type;
	protected java.lang.ref.Reference<T> storage;
	protected final Creator<java.lang.ref.Reference<T>, T> creator;
	public ReferenceProp(Class<T> typeClass, Reference type) {
		super(typeClass);
		this.type = type;
		switch(type) {
			case Soft:
				creator = new Creator<java.lang.ref.Reference<T>, T>() {
					public java.lang.ref.Reference<T> create(T using) {
						return new SoftReference(using);
					}
				};
				break;
				
			case Weak:
				creator = new Creator<java.lang.ref.Reference<T>, T>() {
					public java.lang.ref.Reference<T> create(T using) {
						return new WeakReference(using);
					}
				};
				break;
				
			default:
				creator = null;
				throw new UnsupportedOperationException();
		}
	}
	public ReferenceProp(T object, Class<T> typeClass, Reference type) {
		this(typeClass, Reference.Soft);
		set(object);
	}
	public ReferenceProp(T object, Reference type) {
		this((Class<T>)null, type);
		set(object);
	}
	public ReferenceProp(Reference type) {
		this((Class<T>)null, type);
	}
	public ReferenceProp(T object) {
		this((Class<T>)null, Reference.Soft);
		set(object);
	}
	public ReferenceProp() {
		this((Class<T>)null, Reference.Soft);
	}

	public final T get() {
		return storage == null ? null : storage.get();
	}

	public final void set(T value) {
		if(value == null)
			storage = null;
		else
			storage = creator.create(value);
	}

	public final Reference refType() {
		return type;
	}
	
}
