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

import java.lang.reflect.Array;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class StrongPairBuffer<K, V> extends GenericTypeBuffer<Pair<K, V>, Pair<Class<K>, Class<V>>, Pair<Reference, Reference>> {

	public static final Pair<Reference, Reference> Ref = new Pair(Reference.Strong, Reference.Strong);
	
	public StrongPairBuffer(Pair<Class<K>, Class<V>> typeClass, Pair<K, V>... elements) {
		super(typeClass, elements);
	}

	@Override
	protected void setBuffer(Pair<K, V>[] buffer) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Pair<Reference, Reference> refType() {
		return Ref;
	}

	@Override
	protected Pair<K, V>[] create(int size) {
		return new Pair[size];
	}
	
}
