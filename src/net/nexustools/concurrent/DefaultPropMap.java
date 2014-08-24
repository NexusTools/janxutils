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

import net.nexustools.data.accessor.MapAccessor;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.utils.ClassUtils;
import net.nexustools.utils.Creator;
import net.nexustools.utils.Pair;

/**
 *
 * @author kate
 */
public class DefaultPropMap<K, V> extends PropMap<K, V> {
	
	final Creator<V, K> creator;
	final MapAccessor<K, V> accessor = new MapAccessor<K, V>() {
		final MapAccessor<K, V> accessor = DefaultPropMap.super.directAccessor();
		public V get(final K key) {
			V val = accessor.get(key);
			if(val == null)
				val = lock.read(accessor, new SoftWriteReader<V, MapAccessor<K, V>>() {
					@Override
					public boolean test(MapAccessor<K, V> against) {
						return !against.has(key);
					}
					@Override
					public V soft(MapAccessor<K, V> data) {
						return data.get(key);
					}
					@Override
					public V read(MapAccessor<K, V> data) {
						V val = creator.create(key);
						data.put(key, val);
						return val;
					}
				});
			return val;
		}
		public V get(K key, V def) {
			return accessor.get(key, def);
		}
		public void remove(K key) {
			accessor.remove(key);
		}
		public void put(K key, V value) {
			accessor.put(key, value);
		}
		public void putAll(Iterable<Pair<K, V>> iterable) {
			accessor.putAll(iterable);
		}
		public void putAll(Map<K, V> iterable) {
			accessor.putAll(iterable);
		}
		public V replace(K key, V value) {
			return accessor.replace(key, value);
		}
		public boolean has(K key) {
			return accessor.has(key);
		}
		public Map<K, V> copy() {
			return accessor.copy();
		}
		public V take(K key) {
			return accessor.take(key);
		}
		public Map<K, V> take() {
			return accessor.take();
		}
		public boolean isTrue() {
			return accessor.isTrue();
		}
		public void clear() {
			accessor.clear();
		}
		public Iterator<Pair<K, V>> iterator() {
			return accessor.iterator();
		}
	};
	public DefaultPropMap(final Class<? extends K> keyClass, final Class<? extends V> valueClass, Type type) throws NoSuchMethodException {
		this(ClassUtils.creator(valueClass, keyClass), type);
	}
	public DefaultPropMap(Creator<V, K> creator, Type type) {
		super(type);
		this.creator = creator;
	}
	public DefaultPropMap(Creator<V, K> creator) {
		this(creator, Type.HashMap);
	}

	@Override
	public MapAccessor<K, V> directAccessor() {
		return accessor;
	}
	
}
