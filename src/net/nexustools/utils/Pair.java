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

/**
 *
 * @author katelyn
 */
public class Pair<I, V> {
	
	public I i;
	public V v;
	
	public Pair(I i, V v) {
		this.i = i;
		this.v = v;
	}
	
	public Pair() {}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 13 * hash + this.i.hashCode();
		hash = 13 * hash + this.v.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pair<?, ?> other = (Pair<?, ?>) obj;
		if (this.i != other.i && (this.i == null || !this.i.equals(other.i))) {
			return false;
		}
		if (this.v != other.v && (this.v == null || !this.v.equals(other.v))) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Pair(" + i + ", " + v + ")";
	}
	
}
