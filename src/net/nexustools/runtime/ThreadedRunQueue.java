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
import net.nexustools.concurrent.TestReader;
import net.nexustools.concurrent.WriteReader;
import net.nexustools.concurrent.Writer;
import net.nexustools.runtime.future.QueueFuture;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 */
public class ThreadedRunQueue<R extends Runnable> extends RunQueue<R, RunThread> {
	
	private final String name;
	private final PropList<RunThread> idleThreads;
	//private final PropList<RunThread> knownThreads;
	private final PropList<QueueFuture> tasks = new PropList();
	public ThreadedRunQueue(String name, int threads) {
		this.name = name;
		if(threads < 1)
			threads = Runtime.getRuntime().availableProcessors();
		ArrayList<RunThread> runThreads = new ArrayList();
		while(threads > 0) {
			RunThread runThread = new RunThread(name + '-' + threads, this);
			runThreads.add(runThread);
			threads --;
		}
		//knownThreads = new PropList(runThreads);
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
	public QueueFuture nextFuture(final RunThread runThread) {
		return tasks.read(new WriteReader<QueueFuture, ListAccessor<QueueFuture>>() {
			@Override
			public QueueFuture read(ListAccessor<QueueFuture> data) {
				if(data.isTrue()) {
					idleThreads.remove(runThread);
					return data.shift();
				}
				
				Logger.debug("No New Futures");
				idleThreads.push(runThread);
				return null;
			}
		});
	}

	@Override
	protected QueueFuture push(final QueueFuture future) {
		tasks.write(new Writer<ListAccessor<QueueFuture>>() {
			@Override
			public void write(ListAccessor<QueueFuture> data) {
				data.push(future);
				idleThreads.read(new TestReader<ListAccessor<RunThread>>() {
					@Override
					public Boolean read(ListAccessor<RunThread> data) {
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
