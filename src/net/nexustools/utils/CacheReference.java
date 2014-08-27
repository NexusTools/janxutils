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

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;
import net.nexustools.concurrent.Prop;
import net.nexustools.runtime.ThreadedRunQueue;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class CacheReference<T> extends WeakReference<T> {
	
	private static final ThreadedRunQueue runQueue = new ThreadedRunQueue("CacheQueue");

	private final int lifetime;
	private final Runnable cacheClear = new Runnable() {
		public void run() {
			Logger.debug("Clearing cache", cache);
			cache.clear();
		}
	};
	private Task cacheTask;
	private final Prop<T> cache;
	private final Semaphore taskLock = new Semaphore(1);
	public CacheReference(int lifetime, T value) {
		super(value);
		this.lifetime = lifetime;
		cacheTask = runQueue.schedule(cacheClear, lifetime);
		cache = new Prop(value);
	}

	@Override
	public T get() {
		taskLock.acquireUninterruptibly();
		try {
			cacheTask.sync(new Runnable() {
				public void run() {
					cacheTask.cancel();

					if(!cache.isset()) {
						Logger.debug("Cache is empty, using reference");
						cache.set(CacheReference.super.get());
					}
					if(cache.isset())
						cacheTask = runQueue.schedule(cacheClear, lifetime);
				}
			});
			return cache.get();
		} finally {
			taskLock.release();
		}
		
	}
	
}
