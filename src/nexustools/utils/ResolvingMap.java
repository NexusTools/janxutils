/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * @author katelyn
 * 
 * @param <K> The key
 * @param <V> The value
 */
public final class ResolvingMap<K, V> implements Map<K, V> {
	
	protected static class Internal<K, V> {
		public final boolean readonly;
		public final Map<K, V> map;
		
		public Internal(Map<K, V> map, boolean readonly) {
			this.readonly = readonly;
			this.map = map;
		}
	}
	
	private final ArrayList<Internal<K, V>> maps = new ArrayList();
	
	public void attach(Map<K, V> map) {
		attach(map, true);
	}
	
	public void attach(Map<K, V> map, boolean readonly) {
		maps.add(new Internal(map, readonly));
	}
	
	public boolean isAttached(Map<K, V> map) {
		for (Internal<K, V> internal : maps)
			if(internal.map.equals(map))
				return true;
		
		return false;
	}
	
	public boolean detach(Map<K, V> map) {
		for (Iterator<Internal<K, V>> it = maps.iterator(); it.hasNext();) {
			Internal<K, V> internal = it.next();
			if(internal.map.equals(map)) {
				it.remove();
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int size() {
		return keySet().size();
	}

	@Override
	public boolean isEmpty() {
		return size() <= 0;
	}

	@Override
	public boolean containsKey(Object key) {
		for(Internal<K, V> internal : maps)
			if(internal.map.containsKey(key))
				return true;
		
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for(Internal<K, V> internal : maps)
			if(internal.map.containsValue(value))
				return true;
		
		return false;
	}

	@Override
	public V get(Object key) {
		for(Internal<K, V> internal : maps) {
			V value = internal.map.get(key);
			if(value != null)
				return value;
		}
		
		return null;
	}

	@Override
	public V put(K key, V value) {
		Map<K, V> firstWritableMap = null;
		for(Internal<K, V> internal : maps) {
			if(internal.readonly)
				continue;
			
			if(internal.map.containsKey(key)) {
				internal.map.put(key, value);
				return value;
			}
			
			if(firstWritableMap == null)
				firstWritableMap = internal.map;
		}
	
		if(firstWritableMap != null) {
			firstWritableMap.put(key, value);
			return value;
		}
		throw new UnsupportedOperationException("No writable maps are attached.");
	}

	@Override
	public V remove(Object key) {
		for(Internal<K, V> internal : maps) {
			if(internal.readonly)
				continue;
			
			if(internal.map.containsKey(key))
				return internal.map.remove(key);
		}
	
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for(Map.Entry<? extends K, ? extends V> entry : m.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public void clear() {
		for(Internal<K, V> internal : maps) {
			if(internal.readonly)
				continue;
			
			internal.map.clear();
		}
	}

	@Override
	public Set<K> keySet() {
		HashSet<K> keySet = new HashSet();
		for(Internal<K, V> internal : maps)
			for(K key : internal.map.keySet())
				if(!keySet.contains(key))
					keySet.add(key);
		return keySet;
	}

	@Override
	public Collection<V> values() {
		ArrayList<V> values = new ArrayList();
		for (Map.Entry<K, V> entry : entrySet()) {
			values.add(entry.getValue());
		}
		
		return values;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		HashSet<Entry<K, V>> hashSet = new HashSet();
		for(K key : keySet())
			hashSet.add(new AbstractMap.SimpleEntry(key, get(key)));
		return hashSet;
	}
	
}
