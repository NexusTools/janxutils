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

package net.nexustools.data.buffer;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.annote.ThreadUnsafe;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
@ThreadUnsafe
public abstract class TypeMap<K, V, B extends TypeBuffer<Pair<K,V>, ?, Pair<Class<K>, Class<V>>, Pair<Reference, Reference>>> implements MapAccessor<K, V> {
	
	protected final TypeBuffer<Pair<K,V>, ?, Pair<Class<K>, Class<V>>, Pair<Reference, Reference>> buffer;
	protected TypeMap(TypeBuffer<Pair<K,V>, ?, Pair<Class<K>, Class<V>>, Pair<Reference, Reference>> buffer) {
		this.buffer = buffer;
	}
	protected TypeMap(Pair<Class<K>, Class<V>> typeClass, Pair<Reference, Reference> reference, Pair<K, V>... elements) {
		buffer = TypeBuffer.createPair(typeClass, reference, elements);
	}
	protected TypeMap(Pair<Reference, Reference> reference, Pair<K, V>... elements) {
		this(new Pair(), reference, elements);
	}

	public V get(K key) {
		return get(key, null);
	}

	public V get(K key, V def) {
		for(Pair<K, V> entry : this)
			if(entry.i.hashCode() == key.hashCode())
				return entry.v;
		
		return def;
	}

	public void remove(K key) {
		Iterator<Pair<K, V>> it = iterator();
		while(it.hasNext())
			if(it.next().i.hashCode() == key.hashCode()) {
				it.remove();
				break;
			}
	}

	public void put(K key, V value) {
		ListIterator<Pair<K, V>> it = (ListIterator<Pair<K, V>>)iterator();
		while(it.hasNext())
			if(it.next().i.hashCode() == key.hashCode()) {
				it.set(new Pair(key, value));
				return;
			}
		it.add(new Pair(key, value));
	}

	public void putAll(Iterable<Pair<K, V>> iterable) {
		for(Pair<K, V> entry : iterable)
			put(entry.i, entry.v);
	}

	public void putAll(Map<K, V> map) {
		for(Map.Entry<K, V> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	public V replace(K key, V value) {
		ListIterator<Pair<K, V>> it = (ListIterator<Pair<K, V>>)iterator();
		while(it.hasNext()) {
			Pair<K, V> entry = it.next();
			if(entry.i.equals(key)) {
				if(entry.v.hashCode() == value.hashCode())
					return value;
				else {
					it.set(new Pair(key, value));
					return entry.v;
				}
			}
		}
		it.add(new Pair(key, value));
		return value;
	}

	public boolean has(K key) {
		ListIterator<Pair<K, V>> it = (ListIterator<Pair<K, V>>)iterator();
		while(it.hasNext())
			if(it.next().i.hashCode() == key.hashCode())
				return true;
		return false;
	}

	public Map<K, V> copy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public V take(K key) {
		ListIterator<Pair<K, V>> it = (ListIterator<Pair<K, V>>)iterator();
		while(it.hasNext()) {
			Pair<K, V> entry = it.next();
			if(entry.i.hashCode() == key.hashCode())
				try {
					return entry.v;
				} finally {
					it.remove();
				}
		}
		return null;
	}

	public Map<K, V> take() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Iterator<Pair<K, V>> iterator() {
		return buffer.bufferIterator();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + buffer + ")";
	}
	
}
