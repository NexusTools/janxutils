/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author katelyn
 */
public class ResolvingMap<K, V> extends HashMap<K, V> {
	
	private final ArrayList<HashMap<K, V>> attachedMaps = new ArrayList();
	
	public void attach(HashMap<K, V> map, boolean readonly) {
		if(attachedMaps.contains(map))
			return; // Already contains this map
		
		attachedMaps.add(map);
	}
	
	public void detach(HashMap<K, V> map, boolean readonly) {
		if(!attachedMaps.contains(map))
			return; // Already contains this map
		
		attachedMaps.remove(map);
	}

	@Override
	public V get(Object key) {
		ListIterator it = attachedMaps.listIterator();
		while(it.hasPrevious()) {
			HashMap<K, V> map = (HashMap<K, V>) it.previous();
			for (Map.Entry<K,V> entry : map.entrySet()) {
				if(entry.getKey().equals(key))
					return entry.getValue();
			}
		}
		
		return super.get(key);
	}
	
}
