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

package nexustools.runtime.tasks;

import nexustools.runtime.tasks.Task;
import java.util.ArrayList;
import nexustools.runtime.QueueFuture.State;
import nexustools.runtime.tasks.AbstractTaskQueue.TaskFuture;
import nexustools.runtime.QueueFuture;
import nexustools.runtime.RunQueue;
import nexustools.runtime.RunThread;

/**
 *
 * @author katelyn
 */
public class AbstractTaskQueue extends RunQueue<Task, TaskFuture> {
	
	public class TaskFuture extends QueueFuture<Task> {

		private Task task;
		private QueueFuture furtherFuture;
		public TaskFuture(State state, Task runnable) {
			super(state, runnable);
			task = runnable;
		}

		@Override
		public void cancel() {
			if(furtherFuture != null)
				furtherFuture.cancel();
			super.cancel();
		}

		@Override
		protected void complete() {
			super.complete();
			synchronized(mainTasks) {
				mainTasks.add(furtherFuture = new QueueFuture(new Runnable() {
					@Override
					public void run() {
						task.onComplete(AbstractTaskQueue.this);
					}
				}));
			}
		}

		@Override
		protected void error(final Throwable t) {
			super.error(t);
			synchronized(mainTasks) {
				mainTasks.add(furtherFuture = new QueueFuture(new Runnable() {
					@Override
					public void run() {
						task.onError(t, AbstractTaskQueue.this);
					}
				}));
			}
		}
		
		
		
	}

	private final RunThread mainThread;
	protected final ArrayList<QueueFuture> mainTasks = new ArrayList();
	public AbstractTaskQueue(String name) {
		super(name);
		mainThread = new RunThread(name + "[Main]", RunThread.Priority.High) {
			@Override
			protected boolean hasFuture() {
				synchronized(mainTasks) {
					return !mainTasks.isEmpty();
				}
			}
			@Override
			protected QueueFuture nextFuture() {
				synchronized(mainTasks) {
					if(mainTasks.isEmpty())
						return null;
					return mainTasks.remove(0);
				}
			}
		};
	}

	@Override
	protected TaskFuture wrap(Task runnable, State state) {
		return new TaskFuture(state, runnable);
	}
	
}
