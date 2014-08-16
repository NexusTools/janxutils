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

import net.nexustools.concurrent.logic.Writer;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.WriteReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class PropMap<K,V> extends DefaultReadWriteConcurrency<MapAccessor<K,V>> implements MapAccessor<K,V>, Iterable<Pair<K,V>> {

	public static enum Type {
		HashMap,
		WeakHashMap,
		LinkedHashMap
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
		public Map<K, V> copy() {
			return new HashMap(map);
		}
		public void remove(K key) {
			map.remove(key);
		}
		public V replace(K key, V value) {
			return map.put(key, value);
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
				
			case LinkedHashMap:
				map = new LinkedHashMap();
				break;
				
			default:
				throw new UnsupportedOperationException();
		}
	}
	public PropMap() {
		this(Type.HashMap);
	}

	@Override
	public MapAccessor<K, V> directAccessor() {
		return directAccessor;
	}

	public boolean isTrue() {
		return read(new Reader<Boolean, MapAccessor<K, V>>() {
			@Override
			public Boolean read(MapAccessor<K, V> data) {
				return data.isTrue();
			}
		});
	}

	public boolean isset() {
		return read(new Reader<Boolean, MapAccessor<K, V>>() {
			@Override
			public Boolean read(MapAccessor<K, V> data) {
				return data.isset();
			}
		});
	}

	public void clear() {
		write(new Writer<MapAccessor<K, V>>() {
			@Override
			public void write(MapAccessor<K, V> data) {
				data.clear();
			}
		});
	}

	public Iterator<Pair<K, V>> iterator() {
		return new Iterator<Pair<K, V>>() {
			final Iterator<Map.Entry<K, V>> copy = copy().entrySet().iterator();
			public boolean hasNext() {
				return copy.hasNext();
			}
			public Pair<K, V> next() {
				Map.Entry<K, V> entry = copy.next();
				return new Pair(entry.getKey(), entry.getValue());
			}
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		};
	}
	
	public V get(final K key) {
		return read(new Reader<V, MapAccessor<K, V>>() {
			@Override
			public V read(MapAccessor<K, V> data) {
				return data.get(key);
			}
		});
	}

	public V take(final K key) {
		return read(new WriteReader<V, MapAccessor<K, V>>() {
			@Override
			public V read(MapAccessor<K, V> data) {
				return data.take(key);
			}
		});
	}

	public void put(final K key, final V value) {
		write(new Writer<MapAccessor<K, V>>() {
			@Override
			public void write(MapAccessor<K, V> data) {
				data.put(key, value);
			}
		});
	}
	
	public void remove(final K key) {
		write(new Writer<MapAccessor<K, V>>() {
			@Override
			public void write(MapAccessor<K, V> data) {
				data.remove(key);
			}
		});
	}

	public boolean has(final K key) {
		return read(new Reader<Boolean, MapAccessor<K, V>>() {
			@Override
			public Boolean read(MapAccessor<K, V> data) {
				return data.has(key);
			}
		});
	}
	
	public Map<K, V> copy() {
		return read(new Reader<Map<K, V>, MapAccessor<K, V>>() {
			@Override
			public Map<K, V> read(MapAccessor<K, V> data) {
				return data.copy();
			}
		});
	}
	
	
	public V replace(final K key, final V value) {
		return read(new WriteReader<V, MapAccessor<K, V>>() {
			@Override
			public V read(MapAccessor<K, V> data) {
				return data.replace(key, value);
			}
		});
	}
	
}
