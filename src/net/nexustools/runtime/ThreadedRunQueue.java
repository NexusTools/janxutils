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
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 * @param <R>
 */
public class ThreadedRunQueue<R extends Runnable> extends RunQueue<R, RunThread> {
	
	public static enum Delegator {
		/**
		 * First come first serve.
		 * Delegates tasks based on the order they were entered into the queue.
		 */
		FCFS,
		
		/**
		 * Fair, keeps track of the CPU time used by each unique runnable.
		 * Prioritizes instances that use less CPU over those that use more.
		 */
		Fair
	}
	
	public static FutureDelegator create(Delegator type) {
		switch(type) {
			case FCFS:
				return new FCFSTaskDelegator();
				
			case Fair:
				return new FairTaskDelegator();
		}
		
		throw new UnsupportedOperationException();
	}
	
	private final static int defaultThreadCount = Integer.valueOf(System.getProperty("threadqueuecount", String.valueOf(Runtime.getRuntime().availableProcessors())));
	
	private final int count;
	private final String name;
	private final FutureDelegator taskDelegator;
	private final PropList<RunThread> idleThreads;
	private final PropList<Task> tasks = new PropList();
	public ThreadedRunQueue(String name, float multiplier) {
		this(name, Delegator.Fair, (int)(defaultThreadCount*multiplier));
	}
	public ThreadedRunQueue(String name, int threads) {
		this(name, Delegator.Fair, threads);
	}
	public ThreadedRunQueue(String name) {
		this(name, Delegator.Fair, -1);
	}
	public ThreadedRunQueue(String name, Delegator delegator, float multiplier) {
		this(name, delegator, (int)(defaultThreadCount*multiplier));
	}
	public ThreadedRunQueue(String name, Delegator delegator, int threads) {
		this(name, create(delegator), threads);
	}
	public ThreadedRunQueue(String name, Delegator delegator) {
		this(name, delegator, -1);
	}
	public ThreadedRunQueue(String name, FutureDelegator delegator, float multiplier) {
		this(name, delegator, (int)(defaultThreadCount*multiplier));
	}
	public ThreadedRunQueue(String name, FutureDelegator delegator, int threads) {
		count = threads;
		taskDelegator = delegator;
		this.name = name + "Queue";
		if(threads < 1)
			threads = defaultThreadCount;
		Logger.gears("Creating", threads, "RunThreads for", this.name);
		ArrayList<RunThread> runThreads = new ArrayList();
		while(threads > 0) {
			RunThread runThread = new RunThread(this.name + '-' + threads, this);
			runThreads.add(runThread);
			threads --;
		}
		idleThreads = new PropList(runThreads);
	}
	public ThreadedRunQueue(String name, FutureDelegator delegator) {
		this(name, delegator, -1);
	}
	@Override
	public Task nextFuture(final RunThread runThread) {
		return tasks.read(new WriteReader<Task, ListAccessor<Task>>() {
			@Override
			public Task read(final ListAccessor<Task> tasksData) {
				return idleThreads.read(new Reader<Task, ListAccessor<RunThread>>() {
					@Override
					public Task read(ListAccessor<RunThread> data) {
						Logger.gears(name, "Reading Future");
						Task nextFuture = taskDelegator.nextTask(tasksData);
						if(nextFuture != null) {
							idleThreads.remove(runThread);
							Logger.gears(name, "Found Future");
							return nextFuture;
						}
						
						idleThreads.unique(runThread);
						Logger.gears(name, "No New Futures");
						return null;
					}
				});
			}
		});
	}

	@Override
	public Task push(final Task future) {
		tasks.write(new Writer<ListAccessor<Task>>() {
			@Override
			public void write(final ListAccessor<Task> tasksData) {
				idleThreads.read(new Reader<Boolean, ListAccessor<RunThread>>() {
					@Override
					public Boolean read(ListAccessor<RunThread> data) {
						tasksData.push(future);
						if(data.isTrue()) {
							Logger.gears("Notifying Idle RunThread");
							data.pop().notifyTasksAvailable();
							return true;
						}
					
						Logger.gears("No idle RunThreads to Notify");
						return false;
					}
				});
			}
		});
		return future;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int countThreads() {
		return count;
	}

}
