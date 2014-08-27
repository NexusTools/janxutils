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

package net.nexustools.data.buffer.basic;

import net.nexustools.data.buffer.StrongPairBuffer;
import net.nexustools.data.buffer.TypeBuffer;
import net.nexustools.data.buffer.TypeMap;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class StrongTypeMap<K, V> extends TypeMap<K, V, StrongPairBuffer<K, V>> {

	public StrongTypeMap(Pair<Class<K>, Class<V>> typeClass, Pair<K, V>... elements) {
		super(TypeBuffer.createPair(typeClass, StrongPairBuffer.Ref, elements));
	}
	public StrongTypeMap(Pair<K, V>... elements) {
		this(new Pair(), elements);
	}
	
	public boolean isTrue() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void clear() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
