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

import java.util.Map;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public interface MapAccessor<K,V> extends BaseAccessor, Iterable<Pair<K,V>> {
	
	public V get(K key);
	public V get(K key, V def);
	
	public void remove(K key);
	public void put(K key, V value);
	public void putAll(Iterable<Pair<K,V>> iterable);
	public void putAll(Map<K,V> iterable);
	public V replace(K key, V value);
	public boolean has(K key);
	public Map<K, V> copy();
	
	public V take(K key);
	public Map<K,V> take();
	
}
