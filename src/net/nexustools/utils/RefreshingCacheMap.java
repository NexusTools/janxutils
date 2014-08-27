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

import net.nexustools.data.accessor.DataAccessor.CacheLifetime;

/**
 *
 * @author katelyn
 */
public class RefreshingCacheMap<K,V> extends CacheMap<K, V, RefreshingCacheReference<V>> {
	
	public RefreshingCacheMap(Creator<V,K> creator) {
		this(creator, CacheLifetime.Medium);
	}
	public RefreshingCacheMap(Creator<V,K> creator, CacheLifetime lifetime) {
		super(creator, lifetime);
	}
	
	protected RefreshingCacheReference<V> ref(V value) {
		return new RefreshingCacheReference(lifetime.life, value);
	}
	
}
