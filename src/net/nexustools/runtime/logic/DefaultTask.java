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

import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.logic.IfReader;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.TestReader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.Testable;
/**
 *
 * @author katelyn
 */
public abstract class DefaultTask implements Task {
	
	private final Prop<State> state;
	private final Prop<Thread> runThread = new Prop();
	public DefaultTask(State state) {
		this.state = new Prop(state);
	}
	
	public State state() {
		return state.get();
	}
	
	public boolean isWaiting() {
		State st = state();
		return st == State.WaitingInQueue || st == State.Scheduled;
	}
	
	public boolean isDone() {
		State st = state();
		return st == State.Complete || st == State.Cancelled || st == State.Aborted;
	}
	
	public boolean isComplete() {
		return state() == State.Complete;
	}
	
	public boolean didAbort() {
		return state() == State.Aborted;
	}
	
	public boolean isExecutable() {
		return state() == State.WaitingInQueue;
	}
	
	public boolean isCancelled() {
		return state() == State.Cancelled;
	}
	
	public void cancel() {
		state.write(new IfWriter<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				state.set(State.Cancelled);
				runThread.read(new IfReader<Void, PropAccessor<Thread>>() {
					@Override
					public Void read(PropAccessor<Thread> data) {
						data.get().interrupt();
						return null;
					}
				});
			}
			@Override
			public boolean test(PropAccessor<State> against) {
				State state = against.get();
				return (state == State.WaitingInQueue || state == State.Scheduled || state == State.Executing);
			}
		});
	}
	
	public final void execute() {
		execute(new Testable<Void>() {
			public boolean test(Void against) {
				return state() == State.Executing;
			}
		});
	}
	public final void execute(Testable<Void> isRunning) {
		if(state.read(new WriteReader<Boolean, PropAccessor<State>>() {
					@Override
					public Boolean read(PropAccessor<State> data) {
						if(data.get() == State.WaitingInQueue) {
							data.set(State.Executing);
							return true;
						}
						return false;
					}
				})) {
			try {
				runThread.set(Thread.currentThread());
				executeImpl(isRunning);
				state.write(new IfWriter<PropAccessor<State>>() {
					@Override
					public void write(PropAccessor<State> data) {
						state.set(State.Complete);
					}
					@Override
					public boolean test(PropAccessor<State> against) {
						return against.get() == State.Executing;
					}
				});
			} catch (Throwable t) {
				state.set(State.Aborted);
				t.printStackTrace();
			} finally {
				runThread.clear();
			}
		}
	}
	
	public boolean onSchedule() {
		return state.read(new TestReader<PropAccessor<State>>() {
			@Override
			public boolean test(PropAccessor<State> against) {
				return against.get() == State.Scheduled;
			}
			@Override
			public void readV(PropAccessor<State> data) {
				data.set(State.WaitingInQueue);
			}
		});
	}
	
	protected abstract void executeImpl(Testable<Void> isRunning);

	
}
