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
import net.nexustools.concurrent.Reader;
import net.nexustools.concurrent.TestWriteReader;
import net.nexustools.concurrent.Writer;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 */
public abstract class ThreadedRunQueue<R extends Runnable, F extends QueueFuture> extends RunQueue<R, F, RunThread> {
	
	private final String name;
	private final PropList<RunThread> activeThreads;
	private final PropList<RunThread> idleThreads;
	private final PropList<F> tasks = new PropList();
	public ThreadedRunQueue(String name, int threads) {
		this.name = name;
		if(threads < 1)
			threads = Runtime.getRuntime().availableProcessors();
		ArrayList<RunThread> runThreads = new ArrayList();
		while(threads > 0) {
			RunThread runThread = new RunThread(name + "[Worker" + threads + "]", this);
			runThreads.add(runThread);
			threads --;
		}
		activeThreads = new PropList(runThreads);
		idleThreads = new PropList(runThreads);
	}
	public ThreadedRunQueue(String name) {
		this(name, -1);
	}
	public ThreadedRunQueue(int threads) {
		this(null, threads);
	}
	public ThreadedRunQueue() {
		this(null, -1);
	}
	@Override
	public F nextFuture(final RunThread runThread) {
		return tasks.read(new Reader<F, ListAccessor<F>>() {
			@Override
			public F read(ListAccessor<F> data) {
				if(data.isTrue()) {
					idleThreads.remove(runThread);
					return data.shift();
				}
				
				System.out.println("No New Futures");
				idleThreads.push(runThread);
				return null;
			}
		});
	}

	@Override
	protected F push(final F future) {
		System.out.println("Pushing Future into Pool");
		tasks.write(new Writer<ListAccessor<F>>() {
			@Override
			public void write(ListAccessor<F> data) {
				data.push(future);
				idleThreads.read(new TestWriteReader<ListAccessor<RunThread>>() {
					@Override
					public Boolean read(ListAccessor<RunThread> data) {
						System.out.println("Notifying Idle Thread of New Task");
						data.last().notifyTasksAvailable();
						return true;
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

}
