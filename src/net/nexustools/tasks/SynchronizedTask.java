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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.Handler;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class SynchronizedTask extends Task {

	@Override
	protected final void syncExecution(Runnable block) throws CancellationException, IllegalThreadStateException {}
	@Override
	protected final void finalExecution(Runnable block) throws CancellationException {}

	@Override
	public final void performExecution() throws IllegalStateException{
		state.write(new Writer<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				try {
					exec0(data);
				} catch(CancellationException ex) {
					return;
				}

				try {
					aboutToExecute();
				} catch(Throwable tt) {
					Logger.exception(tt);
				}

				try {
					execute();
					data.set(State.Complete);
					try {
						success.run();
					} catch(Throwable tt) {
						Logger.exception(Logger.Level.Debug, tt);
					}
					try {
						onSuccess();
					} catch(Throwable tt) {
						Logger.exception(tt);
					}
				} catch(Throwable t) {
					data.set(State.Aborted);
					Logger.exception(t);
					try {
						onFailure(t);
					} catch(Throwable tt) {
						Logger.exception(tt);
					}
				} finally {
					try {
						onComplete();
					} catch(Throwable t) {
						Logger.exception(t);
					}
					try {
						interrupt.run();
					} catch(Throwable tt) {
						Logger.exception(Logger.Level.Gears, tt);
					}
					interrupt = null;
				}
			}
		});
	}
	
	@Override
	public void cancel(final Runnable after) throws IllegalStateException{
		sync(new Handler<State>() {
			public void handle(State data) {
				if(!data.waiting)
					throw new IllegalStateException("Task is not waiting to be or on execution: " + data);

				try {
					interrupt.run();
				} catch(Throwable tt) {}
				interrupt = null;
				state.set(State.Canceled);
				try {
					onCancel();
				} catch(Throwable t) {
					Logger.exception(t);
				}
				try {
					onComplete();
				} catch(Throwable t) {
					Logger.exception(t);
				}
				try {
					after.run();
				} catch(Throwable tt) {
					Logger.exception(tt);
				}
			}
		});
	}

	@Override
	public String toString() {
		return NXUtils.toString(SynchronizedTask.this);
	}
	
}
