/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

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
