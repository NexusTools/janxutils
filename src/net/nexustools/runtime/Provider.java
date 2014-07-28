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

package net.nexustools.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author katelyn
 */
public class Provider<T> implements Iterable<T> {
	
	private static final HashMap<String, Provider> providers = new HashMap();
	
	static interface ImplProvidee<I> {
		public I create();
	}
	
	public static <I> I createImpl(String searchPath) {
		Provider<ImplProvidee<I>> provider = instance(searchPath);
		return provider.get().create();
	}

	private static <I> Provider<I> instance(String searchPath) {
		Provider<I> provider;
		synchronized(providers) {
			provider = providers.get(searchPath);
			if(provider == null) {
				provider = new Provider(searchPath);
				providers.put(searchPath, provider);
			}
		}
		return provider;
	}
	
	private final ArrayList<T> loaded = new ArrayList();
	protected Provider(String searchPath) {}
	
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
		if(loaded instanceof ArrayList) // Not finished yet
			return new Iterator<T>() {
				
				@Override
				public boolean hasNext() {
					return false;
				}
				@Override
				public T next() {
					return null;
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
		};
		return loaded.iterator();
	}
	
}
