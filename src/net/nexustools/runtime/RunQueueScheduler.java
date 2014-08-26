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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.SortedPropList;
import net.nexustools.concurrent.logic.SoftWriter;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.runtime.logic.RunTask;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;
import net.nexustools.utils.sort.DescLongTypeComparator;

/**
 *
 * @author katelyn
 */
public final class RunQueueScheduler {
	
	private static class SchedulerThread extends Thread {
		public SchedulerThread() {
			super("RunQueueScheduler");
			setPriority(MAX_PRIORITY);
			setDaemon(true);
			start();
		}
		@Override
		public void run() {
			while(true) {
				FutureTask fTask;
				try {
					try {
						fTask = scheduledTasks.first();
					} catch(IndexOutOfBoundsException ex) {
						Logger.gears("Waiting for more tasks");
						try {
							Thread.sleep(60000 * 60 * 5);
						} catch (InterruptedException ie) {}
						continue;
					}
				
					Logger.gears("Waiting for Scheduled Task", fTask, fTask.when);
					long rem = fTask.when - System.currentTimeMillis();
					if(rem > fTask.accuracy) {
						Logger.gears("Sleeping for " + rem + "ms");
						try {
							Thread.sleep(Math.min(rem-fTask.accuracy, 5000));
						} catch(InterruptedException ex) {}
						continue;
					}
					
					while((fTask.when - System.currentTimeMillis()) > 0);
					
					scheduledTasks.remove(fTask);
					if(fTask.future.isDone()) {
						Logger.gears("Task was Cancelled", fTask);
						continue;
					}
					
					Logger.gears("Executing Scheduled Task", fTask);
					if(fTask.future.onSchedule()) {
						fTask.onQueue();
						fTask.queue.push(fTask.future);
					}
				} catch(Throwable t) {
					Logger.exception(Logger.Level.Gears, t);
				}
			}
		}
	}
	
	private static class FutureTask {
		public final long when;
		public final int accuracy;
		public final RunQueue queue;
		public final Task future;
		
		public FutureTask(int when, int accuracy, RunQueue queue, Task future) {
			this(System.currentTimeMillis() + when, accuracy, queue, future);
		}
		public FutureTask(long when, int accuracy, RunQueue queue, Task future) {
			this.when = when;
			this.queue = queue;
			this.future = future;
			this.accuracy = accuracy;
		}
		public void onQueue() {}

		@Override
		public String toString() {
			return future.toString();
		}
		
		
	}
	private static class RepeatingFutureTask extends FutureTask {
		public final int interval;
		public RepeatingFutureTask(int interval, int accuracy, RunQueue queue, Task future) {
			super(interval, accuracy, queue, future);
			this.interval = interval;
		}
		public RepeatingFutureTask(long when, int interval, int accuracy, RunQueue queue, Task future) {
			super(when + interval, accuracy, queue, future);
			this.interval = interval;
		}
		@Override
		public void onQueue() {
			Logger.gears("Repeating", future, when + interval);
			schedule(new RepeatingFutureTask(when, interval, accuracy, queue, future.copy(Task.State.Scheduled)));
		}
	}
	
	private static final PropList<FutureTask> scheduledTasks = new SortedPropList<FutureTask>(new DescLongTypeComparator<FutureTask>() {
		@Override
		public long value(FutureTask o) {
			return o.when;
		}
	});
	public static Prop<Thread> schedulerThread = new Prop<Thread>();
	
	public static class StopRepeating extends RuntimeException {}
	
	private static void schedule(final FutureTask entry) {
		Logger.gears("Scheduling", entry.future);
		try {
			scheduledTasks.write(new Writer<ListAccessor<FutureTask>>() {
				@Override
				public void write(final ListAccessor<FutureTask> taskList) throws InvocationTargetException {
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
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
				
	}
	public static void schedule(Task task, int when, int accuracy, RunQueue targetQueue) {
		schedule(new FutureTask(when, accuracy, targetQueue, task));
	}
	public static void scheduleRepeating(final Task task, int delay, final int repeat, final int accuracy, final RunQueue targetQueue) {
		Logger.gears("Scheduling Repeating", task, delay, repeat);
		if(delay > 0) 
			schedule(new RunTask(new Runnable() {
				public void run() {
					scheduleRepeating(task, 0, accuracy, repeat, targetQueue);
				}
			}, Task.State.Scheduled), delay, accuracy, targetQueue);
		else
			schedule(new RepeatingFutureTask(repeat, accuracy, targetQueue, task));
	}
	public static void stopRepeating(final Task task, final RunQueue runQueue) {
		try {
			scheduledTasks.write(new Writer<ListAccessor<FutureTask>>() {
				@Override
				public void write(ListAccessor<FutureTask> data) {
					Iterator<FutureTask> iterator = data.iterator();
					Logger.gears("Searchng for task to stop", task);
					while(iterator.hasNext()) {
						FutureTask entry = iterator.next();
						if(entry.future.equals(task) && entry.queue.equals(runQueue)) {
							Logger.gears("Removing", task);
							iterator.remove();
							break;
						}
					}
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
}
