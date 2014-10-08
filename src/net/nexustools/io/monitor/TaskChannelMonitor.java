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

package net.nexustools.io.monitor;

import java.nio.channels.SelectableChannel;
import net.nexustools.tasks.RunTask;
import net.nexustools.tasks.Task;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public abstract class TaskChannelMonitor<C extends SelectableChannel> extends ChannelMonitor<C> {
	
	protected final TaskSink taskSink;
	public TaskChannelMonitor(C channel, TaskSink taskSink, int opts) {
		super(channel, opts);
		this.taskSink = taskSink;
	}
	
	protected Task wrap(Runnable run) {
		return new RunTask(run) {
			@Override
			protected void execute() {
				try {
					super.execute();
				} catch(Throwable t) {
					handleError(NXUtils.unwrapTarget(t));
				}
			}
		};
	}
	
	protected void push(Runnable block) {
		taskSink.push(wrap(block));
	}
	
	public void push(Task task) {
		taskSink.push(task);
	}

}
