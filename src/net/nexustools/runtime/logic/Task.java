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

package net.nexustools.runtime.logic;

import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public interface Task {
	
	public static enum State {
		Scheduled,
		WaitingInQueue,
		Executing,
		Complete,
		Aborted,
		
		Cancelled
	}
	
	public State state();
	
	public boolean isCancelled();
	public boolean isExecutable();
	public boolean isWaiting();
	
	public boolean didAbort();
	public boolean isComplete();
	public boolean isDone();
	
	public void cancel();
	public void execute();
	public boolean onSchedule();
	public void sync(Runnable block);
	public Task copy(State state);
	
}
