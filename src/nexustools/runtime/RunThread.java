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
package nexustools.runtime;

import nexustools.concurrent.Accessor;
import nexustools.concurrent.Accessor.IfReader;
import nexustools.concurrent.Prop;

/**
 *
 * @author katelyn
 */
class RunThread<R extends Runnable, F extends QueueFuture<R>, Q extends RunQueue<R, F, RunThread>> {

	public static enum Priority {
		Low,
		Normal,
		High
	}

	class NativeRunThread extends Thread {

		private F future;

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
			setName(name);
			start();
		}

		@Override
		public void run() {
			// Make the queue this thread was created for current.
			queue.read(new IfReader<Accessor<Q>, Void>() {
				@Override
				public Void read(Accessor<Q> value) {
					value.internal().makeCurrent();
					return null;
				}
			});
			do {
				future = queue.read(new IfReader<Accessor<Q>, F>() {
					@Override
					public F read(Accessor<Q> value) {
						return value.internal().nextFuture(RunThread.this);
					}
				});
				if (future == null) {
					try { // Remain idle for about 5 minutes
						Thread.sleep(60000 * 5);
						// Assume this thread isn't needed anymore
						thread.clear();
					} catch (InterruptedException ex) {
						// Assume there are new futures
					}
				} else {
					future.execute();
				}
			} while (true);
			//readWrite.unlock();
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

	public void start() {
		thread.act(new Accessor.IfActor<Accessor<NativeRunThread>>() {
			@Override
			public void perform(Accessor<NativeRunThread> accessor) {
				NativeRunThread thread = accessor.internal();
				if(thread == null)
					accessor.init(new NativeRunThread());
				else
					thread.interrupt();
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
