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

import net.nexustools.concurrent.ConcurrentStage;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 * @param <T>
 */
public abstract class RunQueue<R extends Runnable, F extends QueueFuture, T extends RunThread> implements ConcurrentStage {
	
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
	protected F wrap(R runnable, QueueFuture.State state) {
		return (F)QueueFuture.wrap(state, runnable);
	}
	public final F push(R runnable) {
		F future = wrap(runnable, QueueFuture.State.WaitingInQueue);
		return push(future);
	}
	public final F schedule(R runnable, long when) {
		F future = wrap(runnable, QueueFuture.State.Scheduled);
		schedulerThread.schedule(future, when);
		return future;
	}
	public abstract F nextFuture(T runThread);
	protected abstract F push(F future);
	
}
