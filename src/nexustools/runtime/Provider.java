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

package nexustools.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nexustools.runtime.Provider.Provides;

/**
 *
 * @author katelyn
 */
public class Provider<T extends Provides> implements Iterable<T> {
	
	public static interface Provides {}
	
	private List<T> loaded;
	public Provider() {}
	
	public T get() {
		if(loaded == null) {
			Iterator<T> it = iterator();
			if(it.hasNext())
				it.next();
		}
		return loaded.isEmpty() ? null : loaded.get(0);
	}
	
	@Override
	public Iterator<T> iterator() {
		if(loaded == null) // Not started yet
			loaded = new ArrayList();
		if(loaded instanceof ArrayList) // Not finished yet
			return new Iterator<T>() {
				@Override
				public boolean hasNext() {
					throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
				}
				@Override
				public T next() {
					throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
				}
		};
		return loaded.iterator();
	}
	
}
