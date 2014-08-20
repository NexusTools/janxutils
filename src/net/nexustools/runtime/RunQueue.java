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

import net.nexustools.runtime.logic.BackpeddlingRunTask;
import net.nexustools.runtime.logic.ForwardingRunTask;
import net.nexustools.runtime.logic.Task;
import net.nexustools.runtime.logic.RunTask;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <T>
 */
public abstract class RunQueue<R extends Runnable, T> {
	
    public static enum Placement {
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
	
	protected RunQueue() {}
	public abstract String name();
	protected Task wrap(R runnable, Task.State state, Placement placement) {
		switch(placement) {
			case NormalPlacement:
				return new RunTask(runnable, state);
				
			case ReplaceExisting:
				return new ForwardingRunTask(runnable, state);
				
			case WaitOnIdleThread:
			case NormalPlacementAndCancelExisting:
				return new BackpeddlingRunTask(runnable, state);
				
			default:
				throw new UnsupportedOperationException();
		}
	}
	public final Task push(R runnable, Placement placement) {
		Task future = wrap(runnable, Task.State.WaitingInQueue, placement);
		if(future.isDone())
			return null;
		return push(future);
	}
	public final Task scheduleRepeating(R runnable, int delay, int repeat, int accuracy, Placement placement) {
		Task future = wrap(runnable, Task.State.Scheduled, placement);
		if(future.isDone())
			return null;
		RunQueueScheduler.scheduleRepeating(future, delay, repeat, accuracy, this);
		return future;
	}
	public final Task scheduleRepeating(R runnable, int repeat, Placement placement) {
		return scheduleRepeating(runnable, 0, repeat, 5, placement);
	}
	public final Task scheduleRepeating(R runnable, int delay, int repeat) {
		return scheduleRepeating(runnable, delay, repeat, 5, Placement.NormalPlacement);
	}
	public final Task scheduleRepeating(R runnable, int repeat) {
		return scheduleRepeating(runnable, 0, repeat, 5, Placement.NormalPlacement);
	}
	public final Task schedule(R runnable, int when, int accuracy, Placement placement) {
		Task future = wrap(runnable, Task.State.Scheduled, placement);
		if(future.isDone())
			return null;
		RunQueueScheduler.schedule(future, when, accuracy, this);
		return future;
	}
	public final Task schedule(R runnable, int when) {
		return schedule(runnable, when, 5, Placement.NormalPlacement);
	}
	public final Task push(R runnable) {
		return push(runnable, Placement.ReplaceExisting);
	}
	public abstract Task nextFuture(T requestSource);
	public abstract Task push(Task future);
	
	public abstract int countThreads();
	
}
