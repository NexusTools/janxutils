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

import java.lang.reflect.InvocationTargetException;
import net.nexustools.concurrent.ReadWriteLock;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.DataAccessor.CacheLifetime;
import net.nexustools.data.buffer.basic.StrongTypeMap;

/**
 *
 * @author katelyn
 */
public class CacheMap<K,V, C extends CacheReference<V>> {
	
	private final Creator<V,K> creator;
	protected final CacheLifetime lifetime;
	private final ReadWriteLock<StrongTypeMap<K, C>> lock = new ReadWriteLock();
	private final StrongTypeMap<K, C> map = new StrongTypeMap();
	public CacheMap(Creator<V,K> creator) {
		this(creator, CacheLifetime.Medium);
	}
	public CacheMap(Creator<V,K> creator, CacheLifetime lifetime) {
		this.creator = creator;
		this.lifetime = lifetime;
	}
	
	public V get(final K key) {
		try {
			return lock.read(map, new SoftWriteReader<V, StrongTypeMap<K, C>>() {
				V value;
				@Override
				public boolean test(StrongTypeMap<K, C> against) {
					try {
						return (value = against.get(key).get()) == null;
					} catch(NullPointerException ex) {
						return true;
					}
				}
				@Override
				public V soft(StrongTypeMap<K, C> data) throws Throwable {
					return value;
				}
				@Override
				public V read(StrongTypeMap<K, C> data) throws Throwable {
					map.put(key, ref(value = creator.create(key)));
					return value;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	protected C ref(V value) {
		return (C)new CacheReference(lifetime.life, value);
	}
	
}
