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

import java.util.concurrent.CancellationException;
import net.nexustools.concurrent.Condition;
import net.nexustools.concurrent.ThreadCondition;
import net.nexustools.utils.DaemonThread;
import net.nexustools.utils.Handler;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class TaskThread extends DaemonThread implements TaskSink, Condition {

	protected Task currentTask;
	protected Runnable runAfter;
	protected final int idleShutdown;
	protected final ThreadCondition shutdown = new ThreadCondition();
	protected final ThreadCondition hasTask = new ThreadCondition();
	public TaskThread(String name) {
		this(name, null);
	}
	public TaskThread(String name, int idleShutdown) {
		this(name, idleShutdown, MIN_PRIORITY);
	}
	public TaskThread(String name, ThreadGroup threadGroup) {
		this(name, threadGroup, 60000 * 5);
	}
	public TaskThread(String name, ThreadGroup threadGroup, int idleShutdown) {
		this(name, threadGroup, idleShutdown, MIN_PRIORITY);
	}
	public TaskThread(String name, int idleShutdown, int priority) {
		this(name, null, idleShutdown, priority);
	}
	public TaskThread(String name, ThreadGroup threadGroup, int idleShutdown, int priority) {
		super(name, threadGroup, priority);
		this.idleShutdown = idleShutdown;
	}
	protected void push0(final Task task, final Runnable after) throws ClosedSinkException, FullSinkException {
		shutdown.ifRun(new Runnable() {
			public void run() {
				throw NXUtils.wrapRuntime(new ClosedSinkException());
			}
		}, new Runnable() {
			public void run() {
				if(currentTask != null)
					throw NXUtils.wrapRuntime(new FullSinkException());
				task.prepareImpl(new Runnable() {
					public void run() {
						runAfter = after;
						currentTask = task;
						hasTask.finish();
						if(!isAlive())
							start();
					}
				}, new Runnable() {
					public void run() {
						shutdown.sync(new Handler<Boolean>() {
							public void handle(Boolean data) {
								assert(currentTask == task);
								currentTask = null;
							}
						});
					}
				});
			}
		});
	}
	@Override
	public boolean push(final Task task) throws ClosedSinkException, FullSinkException {
		return push(task, NXUtils.NOP);
	}
	public boolean push(final Task task, final Runnable after) throws ClosedSinkException, FullSinkException {
		try {
			push0(task, after);
		} catch(IllegalStateException ex) {
			Logger.exception(Logger.Level.Gears, ex);
			after.run(); // Ensure the thread is made idle if the task failed to push
			return false;
		}
		
		return true;
	}
	@Override
	public Task schedule(Runnable run, int afterMillis) throws TaskSinkException, IllegalStateException {
		RunTask task = RunTask.unique(run);
		TaskScheduler.schedule0(task, afterMillis, 15, this);
		return task;
	}

	@Override
	public Task push(Runnable run) throws TaskSinkException, IllegalStateException {
		Task task;
		push0(task = RunTask.unique(run), NXUtils.NOP);
		return task;
	}

	@Override
	public void run() {
		while(true) {
			if(hasTask.waitForUninterruptibly(idleShutdown)) {
				Runnable after = runAfter;
				currentTask.performExecution();
				shutdown.sync(new Handler<Boolean>() {
					public void handle(Boolean data) {
						currentTask = null;
						hasTask.start();
					}
				});
				after.run();
			} else
				try {
					shutdown.finish(new Runnable() {
						public void run() {
							if(currentTask != null)
								throw new CancellationException();
						}
					});
					break;
				} catch(CancellationException ex) {}
		}
	}

	public boolean waitForUninterruptibly(long millis) {
		return shutdown.waitForUninterruptibly(millis);
	}

	public void waitForUninterruptibly() {
		shutdown.waitForUninterruptibly();
	}

	public boolean waitFor(long millis) throws InterruptedException {
		return shutdown.waitFor(millis);
	}

	public void waitFor() throws InterruptedException {
		shutdown.waitFor();
	}

	public boolean check() {
		return shutdown.check();
	}
	
}
