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

package net.nexustools.tasks;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.SortedPropList;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.concurrent.logic.SoftWriter;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;
import net.nexustools.utils.sort.AscLongTypeComparator;
import net.nexustools.utils.sort.DescLongTypeComparator;

/**
 *
 * @author katelyn
 */
public final class TaskScheduler {
	
	private static class SchedulerThread extends Thread {
		public SchedulerThread() {
			super("TaskScheduler");
			setPriority(MAX_PRIORITY);
			setDaemon(true);
			start();
		}
		@Override
		public void run() {
			while(true) {
				long waitTime = scheduledTasks.read(new SoftWriteReader<Long, ListAccessor<FutureTask>>() {
					Long nextWait;
					@Override
					public boolean test(ListAccessor<FutureTask> against) {
						try {
							FutureTask next = against.last();
							long rem = next.when - System.currentTimeMillis();
							if(rem > next.accuracy) {
								nextWait = rem;
								return false;
							}

							return true;
						} catch(NoSuchElementException ex) {
						} catch(IndexOutOfBoundsException ex) {
						}

						Logger.gears("Waiting for more tasks");
						nextWait = (long)(60000 * 60 * 5);
						return false;
					}
					@Override
					public Long read(ListAccessor<FutureTask> data) {
						FutureTask next = data.pop();
						next.queue.push(next.task);
						return 0L;
					}
					@Override
					public Long soft(ListAccessor<FutureTask> data) {
						return nextWait;
					}
				});

				if(!interrupted())
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException ex) {}
			}
		}
	}
	
	private static class FutureTask {
		public final long when;
		public final int accuracy;
		public final TaskSink queue;
		public final Task task;
		
		public FutureTask(int when, int accuracy, TaskSink taskSync, Task future) {
			this(System.currentTimeMillis() + when, accuracy, taskSync, future);
		}
		public FutureTask(long when, int accuracy, TaskSink queue, Task future) {
			this.when = when;
			this.queue = queue;
			this.task = future;
			this.accuracy = accuracy;
		}
		public void onQueue() {}

		@Override
		public String toString() {
			return task.toString();
		}
		
		
	}
	private static class RepeatingFutureTask extends FutureTask {
		public final int interval;
		public RepeatingFutureTask(int interval, int accuracy, TaskSink queue, Task future) {
			super(interval, accuracy, queue, future);
			this.interval = interval;
		}
		public RepeatingFutureTask(long when, int interval, int accuracy, TaskSink queue, Task future) {
			super(when + interval, accuracy, queue, future);
			this.interval = interval;
		}
//		@Override
//		public void onQueue() {
//			Logger.gears("Repeating", task, when + interval);
//			schedule(new RepeatingFutureTask(when, interval, accuracy, queue, task.copy(Task.State.Scheduled)));
//		}
	}
	
	private static final PropList<FutureTask> scheduledTasks = new SortedPropList<FutureTask>(new AscLongTypeComparator<FutureTask>() {
		@Override
		public long value(FutureTask o) {
			return o.when;
		}
	});
	public static Prop<Thread> schedulerThread = new Prop<Thread>();
	
	private static void schedule(final FutureTask entry) {
		Logger.gears("Scheduling", entry.task);
		scheduledTasks.write(new Writer<ListAccessor<FutureTask>>() {
			@Override
			public void write(final ListAccessor<FutureTask> taskList) {
				taskList.push(entry);
				if(taskList.length() == 1)
					schedulerThread.write(new SoftWriter<PropAccessor<Thread>>() {
						@Override
						public void write(PropAccessor<Thread> data) {
							Logger.gears("Spawning Scheduler Thread");
							data.set(new SchedulerThread());
						}
						@Override
						public void soft(PropAccessor<Thread> data) {
							Logger.gears("Notifying Scheduler Thread");
							data.get().interrupt();
						}
					});
			}
		});
				
	}
	static void schedule0(final Task task, final int afterMillis, final int accuracy, final TaskSink targetQueue) {
		new Runnable() {
			FutureTask futureTask;
			public void run() {
				task.scheduleImpl(new Runnable() {
					public void run() {
						schedule(futureTask = new FutureTask(afterMillis, accuracy, targetQueue, task));
					}
				}, new Runnable() {
					public void run() {
						scheduledTasks.remove(futureTask);
					}
				}, NXUtils.NOP);
			}
		}.run();
	}
	public static boolean schedule(final Task task, final int afterMillis, final int accuracy, final TaskSink targetSink) {
		try {
			schedule0(task, afterMillis, accuracy, targetSink);
		} catch(IllegalStateException ex) {
			Logger.exception(Logger.Level.Debug, ex);
			return false;
		}
		return true;
	}
	public static boolean scheduleRepeating(final Task task, final int delay, final int repeat, final int accuracy, final TaskSink targetQueue) {
		try {
			new Runnable() {
				FutureTask futureTask;
				long next = System.currentTimeMillis() + delay;
				public void scheduleNext() {
					next += repeat;
					task.scheduleImpl(new Runnable() {
						public void run() {
							schedule(futureTask = new FutureTask(next, accuracy, targetQueue, task));
						}
					}, new Runnable() {
						public void run() {
							scheduledTasks.remove(futureTask);
						}
					}, new Runnable() {
						public void run() {
							Logger.debug("Scheduling Repeat");
							scheduleNext();
						}
					});
				}
				public void run() {
					scheduleNext();
				}
			}.run();
		} catch(IllegalStateException ex) {
			Logger.exception(Logger.Level.Debug, ex);
			return false;
		}
		return true;
	}
	
}