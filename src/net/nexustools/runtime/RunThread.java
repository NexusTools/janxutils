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

import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.logic.IfReader;
import net.nexustools.concurrent.logic.SoftWriter;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.runtime.RunQueueScheduler.StopRepeating;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 * @param <Q>
 */
public class RunThread<R extends Runnable, Q extends RunQueue<R, RunThread>> {

	public static enum Priority {
		Low,
		Normal,
		High,
		
		Maximum
	}

	class NativeRunThread extends Thread {

		private Task future;
		private boolean killNext;

		{
			switch (priority.get()) {
				case Low:
					setPriority(0);
					break;
				case Normal:
					setPriority(3);
					break;
				case High:
					setPriority(6);
					break;
				case Maximum:
					setPriority(10);
					break;
			}
			Logger.debug(name, "Spawned");
			setDaemon(true);
			setName(name);
			start();
		}

		RunQueue cQueue;
		@Override
		public void run() {
			Logger.gears(name, "Entered");
			// Make the queue this thread was created for current.
			queue.read(new IfReader<Void, PropAccessor<Q>>() {
				@Override
				public Void read(PropAccessor<Q> data) {
					data.get().makeCurrent();
					return null;
				}
			});
			killNext = false;
			do {
				Logger.gears(name, "Retreiving Work");
				if(thread.read(new WriteReader<Boolean, PropAccessor<NativeRunThread>>() {
					@Override
					public Boolean read(PropAccessor<NativeRunThread> data) {
						future = queue.read(new IfReader<Task, PropAccessor<Q>>() {
							@Override
							public Task read(PropAccessor<Q> data) {
								cQueue = data.get();
								if(cQueue == null)
									return null;
								return cQueue.nextFuture(RunThread.this);
							}
						});
						if(future == null && killNext) {
							Logger.debug(name, "Quit");
							data.clear();
							return true;
						}
						return false;
					}
				}))
					return;
				try {
					Logger.gears(future);
					
					if (future == null) {
						Logger.gears(name, "Went Idle");
						Thread.sleep(60000 * 5);
						killNext = true;
					} else {
						Logger.gears(name, "Executing", future);
						killNext = false;
						future.execute();
					}
				} catch (InterruptedException ex) {
					Logger.gears(name, "Wokeup");
				} catch (RuntimeException run) {
					Logger.Level level = Logger.Level.Warning;
					if(run instanceof StopRepeating) {
						RunQueueScheduler.stopRepeating(future, cQueue);
						level = Logger.Level.Gears;
					}
					Logger.exception(level, run);
				}
			} while (true);
		}
	}

	private final String name;
	private final Prop<Q> queue;
	private final Prop<Priority> priority;
	private final Prop<NativeRunThread> thread = new Prop();

	public RunThread(String name, Q queue) {
		this(name, queue, Priority.Normal);
	}

	public RunThread(String name, Q queue, Priority priority) {
		this.name = name;
		this.queue = new Prop(queue);
		this.priority = new Prop(priority);
	}

	public void notifyTasksAvailable() {
		Logger.gears("Notifying Tasks Available", this);
		thread.write(new SoftWriter<PropAccessor<NativeRunThread>>() {
			@Override
			public void write(PropAccessor<NativeRunThread> data) {
				data.set(new NativeRunThread());
			}
			@Override
			public void soft(PropAccessor<NativeRunThread> data) {
				data.get().interrupt();
			}
		});
	}
	
	public void connect(Q q) {
		queue.set(q);
	}

	public void disconnect() {
		queue.clear();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(name=" + name + ", queue=" + queue.get() + ")";
	}
	
}
