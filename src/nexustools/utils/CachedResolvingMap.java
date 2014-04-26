/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author katelyn
 * 
 * @param <K>
 * @param <V>
 */
public final class CachedResolvingMap<K,V> extends AbstractResolvingMap<K,V> {
	
	private static class CacheEntry<K,V> implements Entry<K,V> {
		private final K key;
		public final Entry<K,V> entry;
		public final Internal<K,V> internal;
		public CacheEntry(Internal<K,V> internal, Entry<K,V> entry) {
			this.key = entry.getKey();
			this.internal = internal;
			this.entry = entry;
		}
		@Override
		public K getKey() {
			return entry.getKey();
		}
		@Override
		public V getValue() {
			return entry.getValue();
		}
		@Override
		public V setValue(V value) {
			return entry.setValue(value);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return Objects.equals(this.key, ((CacheEntry<?, ?>) obj).key);
		}
		@Override
		public int hashCode() {
			int hash = 7;
			hash = 59 * hash + Objects.hashCode(this.key);
			return hash;
		}
	}
	
	private final HashMap<K, Internal> readMaps = new HashMap();
	private final HashMap<K, Internal> writeMaps = new HashMap();
	private Set<Entry<K, V>> cachedEntrySet = null;
	private Set<K> cachedKeySet = null;

	@Override
	protected void attach0(Internal internal) {
		cleanCache();
	}

	@Override
	protected void dettach0(Internal internal) {
		HashSet<K> removedKeys = new HashSet();
		{
			Entry<K,Internal> entry;
			Iterator<Entry<K,Internal>> entries = readMaps.entrySet().iterator();
			while(entries.hasNext()) {
				entry = entries.next();
				if(entry.getValue().equals(internal)) {
					removedKeys.add(entry.getKey());
					entries.remove();
				}
			}
			entries = writeMaps.entrySet().iterator();
			while(entries.hasNext()) {
				entry = entries.next();
				if(entry.getValue().equals(internal)) {
					removedKeys.add(entry.getKey());
					entries.remove();
				}
			}
		}
		if(!removedKeys.isEmpty()) {
			if(cachedEntrySet != null) {
				Set<Entry<K, V>> newEntrySet = new HashSet();
				Iterator<Entry<K,V>> entries = cachedEntrySet.iterator();
				while(entries.hasNext()) {
					CacheEntry<K,V> cacheEntry = (CacheEntry<K,V>)entries.next();
					if(!cacheEntry.internal.equals(internal))
						newEntrySet.add(cacheEntry);
				}
				cachedEntrySet = newEntrySet;
			}
			if(cachedKeySet != null) {
				HashSet<K> newKeySet = new HashSet();
				Iterator<K> entries = cachedKeySet.iterator();
				while(entries.hasNext()) {
					K key = entries.next();
					if(!removedKeys.contains(key))
						newKeySet.add(key);
				}
				cachedKeySet = newKeySet;
			}
		}
	}

	@Override
	protected Internal<K, V> lookupWritableForKey(K key) {
		Internal<K,V> internal = writeMaps.get(key);
		if(internal == null) {
			internal = super.lookupForKey(key);
			writeMaps.put(key, internal);
		}
		return internal;
	}

	@Override
	protected Internal<K, V> lookupForKey(K key) {
		Internal<K,V> internal = readMaps.get(key);
		if(internal == null) {
			internal = super.lookupForKey(key);
			readMaps.put(key, internal);
		}
		return internal;
	}
	
	public void cleanCache() {
		readMaps.clear();
		writeMaps.clear();
		cachedEntrySet = null;
		cachedKeySet = null;
	}

	@Override
	public Set<K> keySet() {
		if(cachedKeySet == null) {
			cachedKeySet = new HashSet();
			for(Internal<K, V> internal : maps)
				for(K key : internal.keySet())
					cachedKeySet.add(key); // Allow deeper keys to override
		}
		return cachedKeySet;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		if(cachedEntrySet == null) {
			cachedEntrySet = new HashSet();
			for(Internal<K, V> internal : maps)
				for(Entry<K,V> entry : internal.entrySet())
					cachedEntrySet.add(new CacheEntry(internal, entry));
		}
		return cachedEntrySet;
	}
	
}
