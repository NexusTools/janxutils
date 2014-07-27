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

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * 
 * @author katelyn
 * 
 * @param <K> The key
 * @param <V> The value
 */
public final class ResolvingMap<K, V> extends AbstractResolvingMap<K, V> {

	@Override
	public Set<K> keySet() {
		HashSet<K> keySet = new HashSet();
		for(Internal<K, V> internal : maps)
			for(K key : internal.keySet())
				if(!keySet.contains(key))
					keySet.add(key);
		return keySet;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		HashSet<Entry<K, V>> hashSet = new HashSet();
		for(K key : keySet())
			hashSet.add(new AbstractMap.SimpleEntry(key, get(key)));
		return hashSet;
	}

}
