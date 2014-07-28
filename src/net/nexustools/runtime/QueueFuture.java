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

import net.nexustools.concurrent.IfWriter;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.utils.Testable;
/**
 *
 * @author katelyn
 */
public abstract class QueueFuture {
	
	public static QueueFuture wrap(State state, final Runnable run) {
		return new QueueFuture(state) {
			@Override
			public void execute(Testable<Void> isCancelled) {
				run.run();
			}
			@Override
			public Object unique() {
				return run;
			}
		};
	}
	
	public static enum State {
		Scheduled,
		WaitingInQueue,
		Executing,
		Complete,
		Aborted,
		
		Cancelled
	}
	
	private final Prop<State> state;
	private final Prop<Thread> runThread = new Prop();
	QueueFuture(State state) {
		this.state = new Prop(state);
	}

	@Override
	public final int hashCode() {
		return unique().hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		return unique().equals(obj);
	}
	
	public abstract Object unique();
	
	public State state() {
		return state.get();
	}
	
	public boolean isExecutable() {
		return state() == State.WaitingInQueue;
	}
	
	public void cancel() {
		state.write(new IfWriter<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				state.set(State.Cancelled);
				runThread.write(new IfWriter<PropAccessor<Thread>>() {
					@Override
					public void write(PropAccessor<Thread> data) {
						data.get().interrupt();
					}
				});
			}
			@Override
			public boolean test(PropAccessor<State> against) {
				State state = against.get();
				return !(state == State.Complete || state == State.Aborted);
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
	public abstract void execute(Testable<Void> isCancelled);
	
}
