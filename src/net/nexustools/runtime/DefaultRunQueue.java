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

import net.nexustools.concurrent.BaseAccessor;
import net.nexustools.concurrent.BaseActor;
import net.nexustools.concurrent.BaseReader;
import net.nexustools.concurrent.BaseWriter;
import net.nexustools.concurrent.FakeLock;
import net.nexustools.concurrent.IfReader;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.ReadWriteLock;
import net.nexustools.concurrent.Reader;
import net.nexustools.concurrent.TestWriteReader;
import net.nexustools.concurrent.Writer;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 */
public class DefaultRunQueue<R extends Runnable, F extends QueueFuture> extends RunQueue<R, F, RunThread> {

	private static DefaultRunQueue instance = new DefaultRunQueue();
	public static DefaultRunQueue instance() {
		return instance;
	}
	
	private final String name;
	private final ReadWriteLock lock = new ReadWriteLock();
	private final PropList<RunThread> activeThreads = new PropList();
	private final PropList<RunThread> idleThreads = new PropList();
	private final PropList<F> tasks = new PropList();
	public DefaultRunQueue(String name, int threads) {
		this.name = name;
		if(threads < 1)
			threads = Runtime.getRuntime().availableProcessors();
		while(threads > 0) {
			RunThread runThread = new RunThread(name + "[Worker" + threads + "]", this);
			activeThreads.push(runThread);
			idleThreads.push(runThread);
			threads --;
		}
	}
	public DefaultRunQueue(String name) {
		this(name, -1);
	}
	public DefaultRunQueue(int threads) {
		this(null, threads);
	}
	public DefaultRunQueue() {
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
				
				idleThreads.push(runThread);
				return null;
			}
		});
	}

	@Override
	protected F push(final F future) {
		tasks.write(new Writer<ListAccessor<F>>() {
			@Override
			public void write(ListAccessor<F> data) {
				if(tasks.unique(future))
					idleThreads.read(new TestWriteReader<ListAccessor<RunThread>>() {
						@Override
						public Boolean read(ListAccessor<RunThread> data) {
							data.pop().notifyTasksAvailable();
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

	public void write(BaseAccessor data, BaseWriter actor) {
		lock.write(data, actor);
	}

	public Object read(BaseAccessor data, BaseReader reader) {
		return lock.read(data, reader);
	}

	public void act(final BaseActor actor) {
		push((R)new Runnable() {
			public void run() {
				actor.perform(FakeLock.instance);
			}
		});
	}

}
