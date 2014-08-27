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
public class RefreshingCache<V> extends Cache<V, RefreshingCacheReference<V>> {
	
	public RefreshingCache(Creator<V,Void> creator) {
		this(creator, CacheLifetime.Medium);
	}
	public RefreshingCache(Creator<V,Void> creator, CacheLifetime lifetime) {
		super(creator, lifetime);
	}
	
	@Override
	protected RefreshingCacheReference<V> ref(V value) {
		return new RefreshingCacheReference(lifetime.life, value);
	}
	
}
