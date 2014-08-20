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

import java.util.Comparator;
import java.util.Iterator;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.SortedPropList;
import net.nexustools.concurrent.logic.SoftWriter;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.runtime.logic.RunTask;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public final class RunQueueScheduler {
	
	private static class SchedulerThread extends Thread {
		public SchedulerThread() {
			super("RunQueueScheduler");
			start();
		}
		@Override
		public void run() {
			while(true) {
				FutureTask fTask = scheduledTasks.pop();
				if(fTask == null) {
					try {
						Thread.sleep(60000 * 60 * 5);
					} catch (InterruptedException ex) {}
					continue;
				}
				
				if(fTask.future.onSchedule()) {
					fTask.onQueue();
					fTask.queue.push(fTask.future);
				}
			}
		}
	}
	
	private static class FutureTask {
		public final long when;
		public final RunQueue queue;
		public final Task future;
		
		public FutureTask(int when, RunQueue queue, Task future) {
			this.when = System.currentTimeMillis() + when;
			this.queue = queue;
			this.future = future;
		}
		public void onQueue() {}

		@Override
		public String toString() {
			return future.toString();
		}
		
		
	}
	private static class RepeatingFutureTask extends FutureTask {
		public final int interval;
		public RepeatingFutureTask(int interval, RunQueue queue, Task future) {
			super(interval, queue, future);
			this.interval = interval;
		}
		@Override
		public void onQueue() {
			Logger.info(Logger.Level.Gears, "Repeating", this);
			scheduleRepeating(future, 0, interval, queue);
		}
	}
	
	private static final PropList<FutureTask> scheduledTasks = new SortedPropList<FutureTask>(new Comparator<FutureTask>() {
		public int compare(FutureTask o1, FutureTask o2) {
			long when = o2.when - o1.when;
			if(when > Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			return (int)when;
		}
	});
	public static Prop<Thread> schedulerThread = new Prop<Thread>();
	
	public static class StopRepeating extends java.lang.RuntimeException {}
	
	private static void schedule(final FutureTask entry) {
		Logger.info(Logger.Level.Gears, "Scheduling", entry);
		scheduledTasks.push(entry);
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
	public static void schedule(Task task, int when, RunQueue targetQueue) {
		schedule(new FutureTask(when, targetQueue, task));
	}
	public static void scheduleRepeating(final Task task, int delay, final int repeat, final RunQueue targetQueue) {
		Logger.info(Logger.Level.Gears, "Scheduling Repeating", task, delay, repeat);
		if(delay > 0) 
			schedule(new RunTask(new Runnable() {
				public void run() {
					scheduleRepeating(task, 0, repeat, targetQueue);
				}
			}, Task.State.Scheduled), delay, targetQueue);
		else
			schedule(new RepeatingFutureTask(repeat, targetQueue, task));
	}
	public static void stopRepeating(final Task task, final RunQueue runQueue) {
		scheduledTasks.write(new Writer<ListAccessor<FutureTask>>() {
			@Override
			public void write(ListAccessor<FutureTask> data) {
				Iterator<FutureTask> iterator = data.iterator();
				while(iterator.hasNext()) {
					FutureTask entry = iterator.next();
					if(entry.future.equals(task) && entry.queue.equals(runQueue))
						iterator.remove();
				}
			}
		});
	}
	
}
