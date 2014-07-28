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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import net.nexustools.concurrent.Accessor;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.ReadWriteLock;

/**
 *
 * @author katelyn
 */
public abstract class RunQueue<R extends Runnable, F extends QueueFuture<R>, T extends RunThread> extends Accessor<List<R>> {
	
	private static ThreadLocal<RunQueue> currentRunQueue = new ThreadLocal();
	public static RunQueue current() {
		RunQueue cRunThread = currentRunQueue.get();
		return cRunThread == null ? null : cRunThread;
	}
	
	public void makeCurrent() {
		currentRunQueue.set(this);
	}

	public static void pushCurrent(Runnable runnable) {
		current().push(runnable);
	}
	
	private final String name;
	private static RunQueueScheduler schedulerThread = new RunQueueScheduler();
	private static final HashMap<Class<? extends Runnable>, HashMap<String, WeakReference<QueueFuture<Runnable>>>> uniqueMap = new HashMap();
	private static final ReadWriteLock uniqueMapLock = new ReadWriteLock();
	protected RunQueue(String name) {
		if(name == null)
			name = getClass().getSimpleName();
		this.name = name;
	}
	protected RunQueue() {
		this(null);
	}
	
	public final String name() {
		return name;
	}
	
	protected F wrap(R runnable, QueueFuture.State state) {
		return (F)new QueueFuture<R>(state, runnable);
	}
	
	public final F push(R runnable) {
		F future = wrap(runnable, QueueFuture.State.Scheduled);
		push(future);
		return future;
	}
	public final F schedule(R runnable, long when) {
		F future = wrap(runnable, QueueFuture.State.Scheduled);
		schedulerThread.schedule(future, when);
		return future;
	}
	public abstract F nextFuture(T runThread);
	protected void registerUnique(F future, final String unique) {
		final Class<? extends Runnable> futureClass = future.get().getClass();
		uniqueMapLock.act(new ReadWriteLock.UpgradeWriter() {
			@Override
			public void perform(ReadWriteLock lock) {
				HashMap<String, WeakReference<QueueFuture<Runnable>>> classMap = uniqueMap.get(futureClass);
				if(classMap == null) {
					classMap = new HashMap();
					uniqueMap.put(futureClass, classMap);
				}
				WeakReference<QueueFuture<Runnable>> ref = classMap.get(unique);
				QueueFuture<Runnable> cFuture = ref.get();
				if(cFuture != null)
					cFuture.cancel();
			}
		});
	}
	protected abstract void push(F future);
	
}
