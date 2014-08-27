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

import java.util.concurrent.Semaphore;
import net.nexustools.runtime.logic.Task;

/**
 *
 * @author katelyn
 */
public class RefreshingCacheReference<T> extends CacheReference<T> {
	
	private Task cacheTask;
	private final Semaphore getLock = new Semaphore(1);
	public RefreshingCacheReference(int lifetime, T value) {
		super(lifetime, value);
	}

	@Override
	protected Task schedule() {
		return cacheTask = super.schedule();
	}

	@Override
	public T get() {
		getLock.acquireUninterruptibly();
		try {
			cacheTask.sync(new Runnable() {
				public void run() {
					cacheTask.cancel();

					if(!cache.isset())
						cache.set(RefreshingCacheReference.super.get());
					if(cache.isset())
						schedule();
				}
			});
			return cache.get();
		} finally {
			getLock.release();
		}
		
	}
	
}
