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

import net.nexustools.runtime.future.BackpeddlingQueueFuture;
import net.nexustools.runtime.future.ForwardingQueueFuture;
import net.nexustools.runtime.future.QueueFuture;
import net.nexustools.runtime.future.RunnableQueueFuture;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <T>
 */
public abstract class RunQueue<R extends Runnable, T> {
	
    public static enum QueuePlacement {
        NormalPlacement,
        ReplaceExisting,
        NormalPlacementAndCancelExisting,
		WaitOnIdleThread
    }
	
	private static final ThreadLocal<RunQueue> currentRunQueue = new ThreadLocal();
	public static RunQueue current() {
		RunQueue cRunThread = currentRunQueue.get();
		return cRunThread == null ? DefaultRunQueue.instance() : cRunThread;
	}
	
	public void makeCurrent() {
		currentRunQueue.set(this);
	}

	public static void pushCurrent(Runnable runnable) {
		current().push(runnable);
	}
	
	private static RunQueueScheduler schedulerThread = new RunQueueScheduler();
	protected RunQueue() {}
	public abstract String name();
	protected QueueFuture wrap(R runnable, QueueFuture.State state, QueuePlacement placement) {
		switch(placement) {
			case NormalPlacement:
				return new RunnableQueueFuture(runnable, state);
				
			case ReplaceExisting:
				return new ForwardingQueueFuture(runnable, state);
				
			case WaitOnIdleThread:
			case NormalPlacementAndCancelExisting:
				return new BackpeddlingQueueFuture(runnable, state);
				
			default:
				throw new UnsupportedOperationException();
		}
	}
	public final QueueFuture push(R runnable, QueuePlacement placement) {
		QueueFuture future = wrap(runnable, QueueFuture.State.WaitingInQueue, placement);
		if(future.isDone())
			return null;
		return push(future);
	}
	public final QueueFuture schedule(R runnable, long when, QueuePlacement placement) {
		QueueFuture future = wrap(runnable, QueueFuture.State.Scheduled, placement);
		if(future.isDone())
			return null;
		schedulerThread.schedule(future, when);
		return future;
	}
	public final QueueFuture push(R runnable) {
		return push(runnable, QueuePlacement.ReplaceExisting);
	}
	public final QueueFuture schedule(R runnable, long when) {
		return schedule(runnable, when, QueuePlacement.NormalPlacementAndCancelExisting);
	}
	public abstract QueueFuture nextFuture(T requestSource);
	protected abstract QueueFuture push(QueueFuture future);
	
	public abstract int countThreads();
	
}
