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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;
import net.nexustools.tasks.Task;
import net.nexustools.tasks.Task.State;

/**
 *
 * @author katelyn
 */
public class RefreshingCacheReference<T> extends CacheReference<T> {
	
	private Task cacheTask;
	private boolean markedToClear = false;
	private boolean silentRefresh = false;
	private final Semaphore taskLock = new Semaphore(1);
	public RefreshingCacheReference(int lifetime, T value) {
		super(lifetime, value);
	}
	
	@Override
	protected void schedule() {
		taskLock.acquireUninterruptibly();
		try {
			cacheTask = runQueue.schedule(new Runnable() {
				public void run() {
					if(!markedToClear) {
						if(silentRefresh)
							silentRefresh = false;
						else
							markedToClear = true;
						scheduleLater();
					} else
						cache.clear();
				}
			}, lifetime/2);
		} finally {
			taskLock.release();
		}
	}

	@Override
	public T get() {
		if(!markedToClear) {
			silentRefresh = true;
			return super.get();
		}
		
		taskLock.acquireUninterruptibly();
		try {
			cacheTask.sync(new Handler<State>() {
				public void handle(State state) {
					if(state == State.Enqueued ||
							state == State.Scheduled)
						cacheTask.cancel();

					if(!cache.isset())
						cache.set(RefreshingCacheReference.super.get());
					if(cache.isset()) {
						markedToClear = false;
						schedule();
					}
				}
			});
			return cache.get();
		} finally {
			taskLock.release();
		}
		
	}
	
}
