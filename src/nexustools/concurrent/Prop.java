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

package nexustools.concurrent;

import java.util.Objects;

/**
 *
 * @author katelyn
 */
public class Prop<T> extends Accessor<T> {
	
	private T value;
	protected final ConcurrentList<Watcher> watchers = new ConcurrentList();
	public Prop(T val) {
		value = val;
	}
//	public Prop(Default d) {
//		super(d);
//	}
	public Prop() {}

	@Override
	public int hashCode() {
		if(value == null)
			return 0;
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		try {
			if(obj == null) {
				return value == null;
			}
			if(value == null)
				return false;
			if(obj instanceof Prop) {
				return Objects.equals(this.value, ((Prop)obj).get());
			} else if(value.getClass().isAssignableFrom(obj.getClass())) {
				return Objects.equals(this.value, value);
			}
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	public void watch() {
		
	}
	
	/**
	 * Sets the value of this property.
	 * This method is assured to be blocking.
	 * 
	 * @param val
	 * @return 
	 */
	public T set(T val) {
		try {
			if(!lock.tryFastUpgrade()) {
				lock.upgrade();
				//if(!def.isnull(value))
				//	return value;
			}
			return value = val;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Gets the value of this property, using a provided
	 * Default to create a default property if needed.
	 * This method is assured to be blocking.
	 * 
	 * @param def The default implementation
	 * @return 
	 */
//	public T get(Default<T> def) {
//		try {
//			lock.lock();
//			if(def.isnull(value)) {
//				if(!lock.tryFastUpgrade()) {
//					lock.upgrade();
//					if(!def.isnull(value))
//						return value;
//				}
//				
//				return value = def.create();
//			}
//			return value;
//		} finally {
//			lock.unlock();
//		}
//	}
	
	/**
	 * Gets the value of this property, might use a
	 * Default provided to the constructor.
	 * This method is assured to be blocking.
	 * 
	 * @return 
	 */
	public T get() {
		try {
			lock.lock();
//			if(def != null) {
//				if(!lock.tryFastUpgrade()) {
//					lock.upgrade();
//					if(def == null)
//						return value;
//				}
//				
//				Default<T> d = def;
//				def = null;
//				lock.downgrade();
//				return get(d);
//			}
			
			return value;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Checks if this property has a value set, or if its null.
	 * This method is assured to be blocking.
	 * 
	 * @return 
	 */
	@Override
	public boolean isset() {
		return get() != null;
	}

	/**
	 * Clears the contents of this property.
	 * This method is assured to be blocking.
	 */
	public void clear() {
		set(null);
	}

	@Override
	public T internal() {
		return value;
	}

	@Override
	public void init(T object) {
		value = object;
	}
	
}
