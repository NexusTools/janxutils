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

package net.nexustools.concurrent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class PropMap<K,V> extends PropConcurrency<MapAccessor<K,V>> implements MapAccessor<K,V> {

	public static enum Type {
		HashMap,
		WeakHashMap,
		LinkedMap
	}
	
	private final Type type;
	private final Map<K,V> map;
	private final MapAccessor<K,V> directAccessor = new MapAccessor<K,V>() {
		public V get(K key) {
			return map.get(key);
		}
		public V take(K key) {
			return map.remove(key);
		}
		public void put(K key, V value) {
			map.put(key, value);
		}
		public boolean has(K key) {
			return map.containsKey(key);
		}
		public boolean isTrue() {
			return map.size() > 0;
		}
		public boolean isset() {
			return map.size() > 0;
		}
		public void clear() {
			map.clear();
		}
		public Iterator<Pair<K, V>> iterator() {
			final Iterator<Map.Entry<K,V>> mapIterator = map.entrySet().iterator();
			return new Iterator<Pair<K, V>>() {
				public boolean hasNext() {
					return mapIterator.hasNext();
				}
				public Pair<K, V> next() {
					Map.Entry<K, V> entry = mapIterator.next();
					return new Pair(entry.getKey(), entry.getValue());
				}
				public void remove() {
					mapIterator.remove();
				}
			};
		}
	};
	public PropMap(Type type) {
		switch(this.type = type) {
			case HashMap:
				map = new HashMap();
				break;
				
			case WeakHashMap:
				map = new WeakHashMap();
				break;
				
			default:
				throw new UnsupportedOperationException();
		}
	}
	public PropMap() {
		this(Type.HashMap);
	}

	@Override
	protected MapAccessor<K, V> directAccessor() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public boolean isTrue() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public boolean isset() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void clear() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Iterator<Pair<K, V>> iterator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	public V get(K key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public V take(K key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void put(K key, V value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public boolean has(K key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
