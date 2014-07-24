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

import nexustools.runtime.RunQueue;

/**
 *
 * @author katelyn
 */
public interface Task extends Runnable {
	
	public void onSchedule(long targetTime);
	public void onStart(RunQueue queue) throws TaskException;
	public void onError(Throwable t, RunQueue queue);
	public void onComplete(RunQueue queue);
	public String uniqueID();
	
}
