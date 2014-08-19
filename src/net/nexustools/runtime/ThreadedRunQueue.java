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
import net.nexustools.concurrent.logic.TestReader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.runtime.future.QueueFuture;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 */
public class ThreadedRunQueue<R extends Runnable> extends RunQueue<R, RunThread> {
	
	private final static int defaultThreadCount = Integer.valueOf(System.getProperty("threadqueuecount", String.valueOf(Runtime.getRuntime().availableProcessors())));
	
	private final int count;
	private final String name;
	private final PropList<RunThread> idleThreads;
	private final PropList<QueueFuture> tasks = new PropList();
	public ThreadedRunQueue(String name, float multiplier) {
		this(name, (int)(defaultThreadCount*multiplier));
	}
	public ThreadedRunQueue(String name, int threads) {
		count = threads;
		this.name = name + "Queue";
		if(threads < 1)
			threads = defaultThreadCount;
		ArrayList<RunThread> runThreads = new ArrayList();
		while(threads > 0) {
			RunThread runThread = new RunThread(this.name + '-' + threads, this);
			runThreads.add(runThread);
			threads --;
		}
		idleThreads = new PropList(runThreads);
	}
	public ThreadedRunQueue(String name) {
		this(name, -1);
	}
	@Override
	public QueueFuture nextFuture(final RunThread runThread) {
		return tasks.read(new WriteReader<QueueFuture, ListAccessor<QueueFuture>>() {
			@Override
			public QueueFuture read(final ListAccessor<QueueFuture> tasksData) {
				return idleThreads.read(new Reader<QueueFuture, ListAccessor<RunThread>>() {
					@Override
					public QueueFuture read(ListAccessor<RunThread> data) {
						Logger.gears(name, "Reading Future");
						if(tasksData.isTrue()) {
							idleThreads.remove(runThread);
							Logger.gears(name, "Found Future");
							return tasksData.shift();
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
	protected QueueFuture push(final QueueFuture future) {
		tasks.write(new Writer<ListAccessor<QueueFuture>>() {
			@Override
			public void write(final ListAccessor<QueueFuture> tasksData) {
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
