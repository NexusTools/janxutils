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

import java.util.concurrent.Semaphore;
import net.nexustools.data.accessor.DataAccessor.CacheLifetime;
import net.nexustools.data.buffer.basic.StrongTypeMap;

/**
 *
 * @author katelyn
 */
public class CacheMap<K,V, C extends CacheReference<V>> {
	
	private final Creator<V,K> creator;
	protected final CacheLifetime lifetime;
	private final Semaphore getLock = new Semaphore(1);
	private final StrongTypeMap<K, C> map = new StrongTypeMap();
	public CacheMap(Creator<V,K> creator) {
		this(creator, CacheLifetime.Medium);
	}
	public CacheMap(Creator<V,K> creator, CacheLifetime lifetime) {
		this.creator = creator;
		this.lifetime = lifetime;
	}
	
	public V get(K key) {
		getLock.acquireUninterruptibly();
		try {
			C ref = map.get(key);
			
			V value;
			if(ref == null || (value = ref.get()) == null)
				map.put(key, ref(value = creator.create(key)));
			return value;
		} finally {
			getLock.release();
		}
	}
	
	protected C ref(V value) {
		return (C)new CacheReference(lifetime.life, value);
	}
	
}
