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

/**
 *
 * @author katelyn
 */
public interface TaskQueue extends TaskSink {
	
	/**
	 * The total size of this TaskQueue.
	 * 
	 * @return 
	 */
	public int totalSize();
	/**
	 * The number of Tasks waiting in this TaskQueue
	 * @return 
	 */
	public int waitingTasks();
	/**
	 * The total number of Tasks ever processed.
	 * 
	 * @return 
	 */
	public long processed();
	/**
	 * The lifetime, in milliseconds, this TaskQueue has been alive for.
	 * 
	 * @return 
	 */
	public long lifetime();
	
}
