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

import net.nexustools.concurrent.IfReader;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.SoftWriter;
import net.nexustools.concurrent.WriteReader;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 * @param <Q>
 */
public class RunThread<R extends Runnable, F extends QueueFuture, Q extends RunQueue<R, F, RunThread>> {

	public static enum Priority {
		Low,
		Normal,
		High
	}

	class NativeRunThread extends Thread {

		private F future;
		private boolean killNext;

		{
			switch (priority.get()) {
				case Low:
					setPriority(MIN_PRIORITY);
					break;
				case Normal:
					setPriority(NORM_PRIORITY);
					break;
				case High:
					setPriority(MAX_PRIORITY);
					break;
			}
			System.out.println("Spawned: " + name);
			setDaemon(true);
			setName(name);
			start();
		}

		@Override
		public void run() {
			System.out.println("Entered: " + name);
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
				if(thread.read(new WriteReader<Boolean, PropAccessor<NativeRunThread>>() {
					@Override
					public Boolean read(PropAccessor<NativeRunThread> data) {
						future = queue.read(new IfReader<F, PropAccessor<Q>>() {
							@Override
							public F read(PropAccessor<Q> data) {
								return data.get().nextFuture(RunThread.this);
							}
						});
						if(future == null && killNext) {
							System.out.println("Quit: " + name);
							data.clear();
							return true;
						}
						return false;
					}
				}))
					return;
				try {
					if (future == null) {
						System.out.println("Went Idle: " + name);
						Thread.sleep(60000 * 5);
						killNext = true;
					} else {
						System.out.println("Executing: " + name);
						System.out.println(future);
						killNext = false;
						future.execute();
					}
				} catch (InterruptedException ex) {
					System.out.println("Wokeup: " + name);
				} catch (RuntimeException run) {
					run.printStackTrace();
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

}
