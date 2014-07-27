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

import net.nexustools.concurrent.ReadWriteLock;
import net.nexustools.concurrent.ReadWriteLock.IfUpgradeWriter;
import net.nexustools.concurrent.ReadWriteLock.UpgradeReader;
import net.nexustools.concurrent.ReadWriteLock.UpgradeWriter;

/**
 *
 * @author katelyn
 */
public class QueueFuture<R extends Runnable> {
	
	class ExecuteStart extends IfUpgradeWriter {
		boolean canExecute = false;

		@Override
		public void perform(ReadWriteLock lock) {
			runThread = Thread.currentThread();
			state = State.Executing;
			canExecute = true;
		}

		@Override
		public boolean test() {
			return state == State.WaitingInQueue;
		}
		
	}
	
	class ExecuteComplete extends UpgradeWriter {
		State toState;

		@Override
		public void perform(ReadWriteLock lock) {
			if(state == State.Executing)
				state = toState;
			runThread = null;
			runnable = null;
		}
		
	}
	
	public static enum State {
		Scheduled,
		WaitingInQueue,
		Executing,
		Complete,
		Aborted,
		
		Cancelled
	}
	
	private R runnable;
	private State state;
	private Thread runThread;
	private final ReadWriteLock lock = new ReadWriteLock();
	QueueFuture(R runnable) {
		this(State.WaitingInQueue, runnable);
	}
	QueueFuture(State state, R runnable) {
		this.runnable = runnable;
		this.state = state;
	}
	
	protected Runnable get() {
		return lock.read(new UpgradeReader<Runnable>() {
			@Override
			public Runnable read() {
				return runnable;
			}
		});
	}
	
	public State state() {
		return lock.read(new UpgradeReader<State>() {
			@Override
			public State read() {
				return state;
			}
		});
	}
	
	protected void setState(final State state) {
		lock.act(new UpgradeWriter() {
			@Override
			public void perform(ReadWriteLock lock) {
				QueueFuture.this.state = state;
			}
		});
	}
	
	public boolean isExecutable() {
		return state() == State.WaitingInQueue;
	}
	
	public void cancel() {
		lock.act(new IfUpgradeWriter() {
			@Override
			public void perform(ReadWriteLock lock) {
				state = State.Cancelled;
				if(runThread != null)
					runThread.interrupt();
			}
			@Override
			public boolean test() {
				return !(state == State.Complete || state == State.Aborted);
			}
		});
	}
	
	public void execute() {
		ExecuteStart executeStart = new ExecuteStart();
		if(!executeStart.canExecute)
			return;
			
		final ExecuteComplete executeComplete = new ExecuteComplete();
		try {
			runnable.run();
			executeComplete.toState = State.Complete;
		} catch(RuntimeException t) {
			executeComplete.toState = State.Aborted;
			t.printStackTrace();
		} finally {
			lock.act(executeComplete);
		}
	}
	
}
