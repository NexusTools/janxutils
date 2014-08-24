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

import java.lang.reflect.InvocationTargetException;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.WriteReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.utils.NXUtils;
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
	
	public static Map create(Type type) {
		switch(type) {
			case HashMap:
				return new HashMap();
				
			case WeakHashMap:
				return new WeakHashMap();
				
			case LinkedHashMap:
				return new LinkedHashMap();
				
			default:
				throw new UnsupportedOperationException();
		}
	}
	
	private Map<K,V> map;
	private final Type type;
	private final MapAccessor<K,V> directAccessor = new MapAccessor<K,V>() {
		public V get(K key) {
			return map.get(key);
		}
		public V get(K key, V def) {
			V val = map.get(key);
			if(val == null)
				val = def;
			return val;
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
		public Map<K, V> take() {
			try {
				return map;
			} finally {
				map = create(type);
			}
		}
		public void putAll(Iterable<Pair<K, V>> iterable) {
			for(Pair<K,V> entry : iterable)
				map.put(entry.i, entry.v);
		}
		public void putAll(Map<K, V> iterable) {
			map.putAll(iterable);
		}
	};
	public PropMap(Type type) {
		map = create(this.type = type);
	}
	public PropMap() {
		this(Type.HashMap);
	}

	@Override
	public MapAccessor<K, V> directAccessor() {
		return directAccessor;
	}

	public boolean isTrue() {
		try {
			return read(new Reader<Boolean, MapAccessor<K, V>>() {
				@Override
				public Boolean read(MapAccessor<K, V> data) {
					return data.isTrue();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public void clear() {
		try {
			write(new Writer<MapAccessor<K, V>>() {
				@Override
				public void write(MapAccessor<K, V> data) {
					data.clear();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
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
		try {
			return read(new Reader<V, MapAccessor<K, V>>() {
				@Override
				public V read(MapAccessor<K, V> data) {
					return data.get(key);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public V get(final K key, final V def) {
		try {
			return read(new Reader<V, MapAccessor<K, V>>() {
				@Override
				public V read(MapAccessor<K, V> data) {
					return data.get(key, def);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public void putAll(final Iterable<Pair<K, V>> iterable) {
		try {
			write(new Writer<MapAccessor<K, V>>() {
				@Override
				public void write(MapAccessor<K, V> data) {
					data.putAll(iterable);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public void putAll(final Map<K, V> map) {
		try {
			write(new Writer<MapAccessor<K, V>>() {
				@Override
				public void write(MapAccessor<K, V> data) {
					data.putAll(map);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public V take(final K key) {
		try {
			return read(new WriteReader<V, MapAccessor<K, V>>() {
				@Override
				public V read(MapAccessor<K, V> data) {
					return data.take(key);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public void put(final K key, final V value) {
		try {
			write(new Writer<MapAccessor<K, V>>() {
				@Override
				public void write(MapAccessor<K, V> data) {
					data.put(key, value);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
	public void remove(final K key) {
		try {
			write(new Writer<MapAccessor<K, V>>() {
				@Override
				public void write(MapAccessor<K, V> data) {
					data.remove(key);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public boolean has(final K key) {
		try {
			return read(new Reader<Boolean, MapAccessor<K, V>>() {
				@Override
				public Boolean read(MapAccessor<K, V> data) {
					return data.has(key);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
	public Map<K, V> copy() {
		try {
			return read(new Reader<Map<K, V>, MapAccessor<K, V>>() {
				@Override
				public Map<K, V> read(MapAccessor<K, V> data) {
					return data.copy();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
	
	public V replace(final K key, final V value) {
		try {
			return read(new WriteReader<V, MapAccessor<K, V>>() {
				@Override
				public V read(MapAccessor<K, V> data) {
					return data.replace(key, value);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
	public Map<K, V> take() {
		try {
			return read(new WriteReader<Map<K, V>, MapAccessor<K, V>>() {
				@Override
				public Map<K, V> read(MapAccessor<K, V> data) {
					return data.take();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
}
