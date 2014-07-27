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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author katelyn
 * 
 * @param <K>
 * @param <V>
 */
abstract class AbstractResolvingMap<K,V> extends AbstractMap<K,V> {
	
	protected final static class Internal<K, V> {
		private final boolean readonly;
		private final Map<K, V> map;
		
		public Internal(Map<K, V> map, boolean readonly) {
			this.readonly = readonly;
			this.map = map;
		}
		
		public Set<K> keySet() {
			return map.keySet();
		}
		
		public Set<Map.Entry<K, V>> entrySet() {
			return map.entrySet();
		}
		
		public boolean containsKey(K key) {
			return map.containsKey(key);
		}
		
		public Iterator<Entry<K,V>> iterator() {
			final Iterator<Entry<K,V>> iterator = map.entrySet().iterator();
			if(readonly)
				return new Iterator<Entry<K,V>>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Entry<K, V> next() {
					return iterator.next();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Attempted to write readonly map.");
				}
			};
			return iterator;
		}
		
		public boolean isReadOnly() {
			return readonly;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return Objects.equals(this.map, ((Internal<?, ?>) obj).map);
		}

		@Override
		public int hashCode() {
			return this.map.hashCode();
		}
		
	}
	
	protected final ArrayList<Internal<K, V>> maps = new ArrayList();
	
	public final void attach(Map<K, V> map) {
		attach(map, true);
	}
	
	public final void attach(Map<K, V> map, boolean readonly) {
		attach0(new Internal(map, readonly));
	}
	
	public final boolean isAttached(Map<K, V> map) {
		for (Internal<K, V> internal : maps)
			if(internal.map.equals(map))
				return true;
		
		return false;
	}
	
	public final boolean detach(Map<K, V> map) {
		for (Iterator<Internal<K, V>> it = maps.iterator(); it.hasNext();) {
			Internal<K, V> internal = it.next();
			if(internal.map.equals(map)) {
				it.remove();
				return true;
			}
		}
		
		return false;
	}

	@Override
	public final V put(K key, V value) {
		Internal internal = lookupWritableForKey(key);
		if(internal != null)
			return (V) internal.map.put(key, value);
		throw new UnsupportedOperationException("No writable maps found.");
	}
	
	@Override
	public final V remove(Object key) {
		Internal internal = lookupWritableForKey((K) key);
		if(internal != null)
			return (V) internal.map.remove(key);
		throw new UnsupportedOperationException("No writable maps found.");
	}
	
	@Override
	public final boolean containsKey(Object key) {
		return lookupForKey((K) key) != null;
	}

	@Override
	public final void clear() {
		for(Internal<K, V> internal : maps) {
			if(internal.isReadOnly())
				continue;
			
			internal.map.clear();
		}
	}
	
	protected Internal<K,V> lookupWritableForKey(K key) {
		Internal firstWritable = null;
		for(Internal<K, V> internal : maps) {
			if(internal.isReadOnly())
				continue;
			
			if(internal.containsKey(key)) 
				return internal;
			
			if(firstWritable == null)
				firstWritable = internal;
		}
		return firstWritable;
	}
	
	protected Internal<K,V> lookupForKey(K key) {
		for(Internal<K, V> internal : maps) {
			if(internal.containsKey(key)) 
				return internal;
		}
		return null;
	}

	protected void attach0(Internal internal) {
		maps.add(internal);
	}

	protected void dettach0(Internal internal) {
		maps.remove(internal);
	}
	
}
