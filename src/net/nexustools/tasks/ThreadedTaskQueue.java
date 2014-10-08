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

import static java.lang.Thread.MIN_PRIORITY;
import java.util.concurrent.CancellationException;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.ReadWriteLock;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Pair;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class ThreadedTaskQueue implements TaskQueue {
	
	public static final int DEFAULT_THREAD_LIMIT;
	static {
		int threadLimit = 0;
		try {
			threadLimit = Integer.valueOf(System.getProperty("taskqueuethreads", "0"));
		} catch(NumberFormatException t) {
			Logger.exception(t);
		}
		if(threadLimit < 1)
			threadLimit = Runtime.getRuntime().availableProcessors();
		DEFAULT_THREAD_LIMIT = threadLimit;
	}
	
	protected final String name;
	protected final int priority;
	private final ThreadGroup group;
	protected final int idleShutdown;
	protected final long created = System.currentTimeMillis();
	protected final ReadWriteLock queueLock = new ReadWriteLock();
	protected final StrongTypeList<Task> queue = new StrongTypeList();
	protected final PropList<Integer> emptyThreadSlots = new PropList();
	protected final PropList<Pair<Integer, TaskThread>> idleThreads = new PropList();
	public ThreadedTaskQueue(String name, int priority, int threadLimit, int idleShutdown) {
		for(int i=Math.max(threadLimit, 2); i>0; i--)
			emptyThreadSlots.push(i);
		
		group = new ThreadGroup(name) {
			{
				setDaemon(true);
			}
		};
		this.idleShutdown = idleShutdown;
		this.priority = priority;
		this.name = name;
	}
	public ThreadedTaskQueue(String name, int threadLimit, int idleShutdown) {
		this(name, MIN_PRIORITY, threadLimit, idleShutdown);
	}
	public ThreadedTaskQueue(String name, int idleShutdown) {
		this(name, DEFAULT_THREAD_LIMIT, idleShutdown);
	}
	public ThreadedTaskQueue(String name) {
		this(name, 60000 * 5 /* 5 Minutes */);
	}
	
	private void markIdle(final TaskThread taskThread, final int threadSlot) {
		Task nextTask;
		try {
			nextTask = queueLock.read(queue, new SoftWriteReader<Task, ListAccessor<Task>>() {
				@Override
				public boolean test(ListAccessor<Task> against) {
					return against.isTrue();
				}
				@Override
				public Task soft(ListAccessor<Task> data) {
					idleThreads.push(new Pair(threadSlot, taskThread));
					throw new CancellationException();
				}
				@Override
				public Task read(ListAccessor<Task> data) {
					return data.pop();
				}
			});
		} catch (CancellationException ex) {
			return;
		}
		if(nextTask != null) {
			try {
				taskThread.push(nextTask, new Runnable() {
					public void run() {
						markIdle(taskThread, threadSlot);
					}
				});
				return;
			} catch (ClosedSinkException ex) {
				emptyThreadSlots.push(threadSlot);
			} catch (FullSinkException ex) {
				Logger.exception(ex);
			}
			try {
				push(nextTask);
			} catch (FullSinkException ex) {
				throw NXUtils.wrapRuntime(ex);
			}
		} else
			throw new IllegalStateException();
	}

	public boolean push(final Task task) throws FullSinkException {
		return queueLock.read(queue, new SoftWriteReader<Boolean, ListAccessor<Task>>() {
			Boolean success = null;

			@Override
			public boolean test(ListAccessor<Task> against) {
				if(success != null)
					return false;
				
				while(true) {
					final Pair<Integer, TaskThread> nextTaskThread = idleThreads.pop();
					if(nextTaskThread == null)
						break;

					try {
						success = nextTaskThread.v.push(task, new Runnable() {
							public void run() {
								markIdle(nextTaskThread.v, nextTaskThread.i);
							}
						});
						return false;
					} catch (ClosedSinkException ex) {
						emptyThreadSlots.push(nextTaskThread.i);
					} catch (FullSinkException ex) {
						Logger.exception(ex);
					}
				}

				final Integer nextSlot = emptyThreadSlots.pop();
				if(nextSlot != null) {
					final TaskThread newTaskThread = new TaskThread(name + "-" + nextSlot, group, idleShutdown, priority);
					try {
						success = newTaskThread.push(task, new Runnable() {
							public void run() {
								markIdle(newTaskThread, nextSlot);
							}
						});
						return false;
					} catch (TaskSinkException ex) {
						emptyThreadSlots.push(nextSlot);
						Logger.exception(ex);
					}
				}

				return true;
			}
			@Override
			public Boolean soft(ListAccessor<Task> data) {
				return success;
			}
			@Override
			public Boolean read(ListAccessor<Task> data) {
				if(data.length() < Integer.MAX_VALUE) {
					data.push(task);
					return true;
				}

				throw new FullSinkException();
			}
		});
	}

	@Override
	public Task push(Runnable run) throws FullSinkException {
		Task task;
		push(task = RunTask.unique(run));
		return task;
	}

	public boolean schedule(Task task, int afterMillis) {
		return TaskScheduler.schedule(task, afterMillis, 15, this);
	}

	public Task schedule(Runnable run, int afterMillis) {
		Task task;
		schedule(task = RunTask.unique(run), afterMillis);
		return task;
	}

	public int totalSize() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public int waitingTasks() {
		return queue.length();
	}

	public long processed() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public long lifetime() {
		return System.currentTimeMillis() - created;
	}
	
}
