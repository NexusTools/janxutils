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
	
	protected static final ThreadedRunQueue runQueue = new ThreadedRunQueue("CacheQueue");

	protected final int lifetime;
	protected final Runnable cacheClear = new Runnable() {
		public void run() {
			Logger.debug("Clearing cache", cache);
			cache.clear();
		}
	};
	protected final Prop<T> cache;
	public CacheReference(int lifetime, T value) {
		super(value);
		this.lifetime = lifetime;
		cache = new Prop(value);
		runQueue.push(new Runnable() {
			public void run() {
				schedule();
			}
		});
	}
	
	protected Task schedule() {
		return runQueue.schedule(cacheClear, lifetime);
	}
	
}
