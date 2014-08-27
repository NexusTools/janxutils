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
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.DataAccessor.CacheLifetime;
import net.nexustools.data.accessor.PropAccessor;

/**
 *
 * @author katelyn
 */
public class Cache<V, C extends CacheReference<V>> {
	
	private final Creator<V, Void> creator;
	protected final CacheLifetime lifetime;
	private final Prop<C> cache = new Prop();
	public Cache(Creator<V,Void> creator) {
		this(creator, CacheLifetime.Medium);
	}
	public Cache(Creator<V,Void> creator, CacheLifetime lifetime) {
		this.creator = creator;
		this.lifetime = lifetime;
	}
	
	public V get() {
		try {
			return cache.read(new SoftWriteReader<V, PropAccessor<C>>() {
				V value;
				@Override
				public boolean test(PropAccessor<C> against) {
					try {
						return (value = against.get().get()) == null;
					} catch(NullPointerException ex) {
						return true;
					}
				}
				@Override
				public V soft(PropAccessor<C> data) throws Throwable {
					return value;
				}
				@Override
				public V read(PropAccessor<C> data) throws Throwable {
					value = creator.create(null);
					data.set(ref(value));
					return value;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public void clear() {
		cache.clear();
	}
	
	protected C ref(V value) {
		return (C)new CacheReference(lifetime.life, value);
	}
	
}
