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

import net.nexustools.tasks.annote.HeavyTask;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
@HeavyTask
public class TaskMonitor extends SimpleSynchronizedTask {
	private static final TaskSink monitorQueue = new ThreadedTaskQueue("TaskMonitorQueue");

	private long start;
	private final Task target;
	TaskMonitor(Task target) {
		this.target = target;
	}

	@Override
	protected void execute() throws InterruptedException {
		Logger.warn(target, "has been running for", Math.round(System.currentTimeMillis() - start)/1000d + " seconds");
	}

	void start() {
		TaskScheduler.scheduleRepeating(this, 0, 15000, 15, monitorQueue);
		start = System.currentTimeMillis();
	}
	
}
