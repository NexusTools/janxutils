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

package net.nexustools.concurrent.logic;

import net.nexustools.data.accessor.MapAccessor;

/**
 *
 * @author katelyn
 */
public abstract class SoftMapReader<K, V> extends SoftWriteReader<V, MapAccessor<K, V>> {
	
	private V value;
	private final K key;
	public SoftMapReader(K key) {
		this.key = key;
	}

	@Override
	public final boolean test(MapAccessor<K, V> against) {
		return (value = against.get(key)) == null;
	}

	@Override
	public final V soft(MapAccessor<K, V> data) {
		return value;
	}

	@Override
	public final V read(MapAccessor<K, V> data) {
		data.put(key, value = create(key));
		return value;
	}
	
	protected abstract V create(K key);
	
}
