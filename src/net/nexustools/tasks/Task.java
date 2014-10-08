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
import java.util.logging.Level;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.VoidReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.data.analyze.ClassDefinition;
import net.nexustools.tasks.annote.HeavyTask;
import net.nexustools.utils.Cancelable;
import net.nexustools.utils.Handler;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Pair;
import net.nexustools.utils.Provider;
import net.nexustools.utils.RuntimeTargetException;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class Task implements Cancelable {
	public static enum State {
		/**
		 * Created and is now ready to execute.
		 */
		Ready(true, false, false, false, false),
		/**
		 * Scheduled for execution.
		 * Can be canceled by calling {@code cancel}
		 */
		Scheduled(true, true, false, false, false),
		/**
		 * Waiting in a queue for execution.
		 * Can be canceled by calling {@code cancel}
		 */
		Enqueued(true, true, false, false, false),
		/**
		 * Being prepared for execution.
		 * Canceled by calling {@code cancel}
		 */
		Preparing(false, true, false, false, false),
		/**
		 * Executing.
		 * Canceled by calling {@code cancel}
		 * 
		 * If you want to ensure that once a task starts executing,
		 * it cannot be canceled, use a {@link SynchronizedTask} instead.
		 */
		Executing(false, true, false, false, false),
		/**
		 * Completed Execution Successfully.
		 * Can be executed again.
		 */
		Complete(true, false, true, true, false),
		/**
		 * Execution Failed.
		 * Can be executed again.
		 * 
		 * This is usually caused by a un-handled exception,
		 * check the log output for more details.
		 */
		Aborted(true, false, true, false, true),
		/**
		 * Execution Canceled.
		 * Can be executed again.
		 */
		Canceled(true, false, false, false, false);
		
		public final boolean ready;
		public final boolean waiting;
		public final boolean finished;
		public final boolean successful;
		public final boolean errored;
		State(boolean ready, boolean waiting, boolean finished, boolean successful, boolean errored) {
			this.ready = ready;
			this.waiting = waiting;
			this.finished = finished;
			this.successful = successful;
			this.errored = errored;
		}
	}
	
	protected Thread thread;
	protected Runnable success;
	protected boolean finalizeCalled;
	protected Runnable interrupt = new Runnable() {
		public void run() {
			ClassDefinition def = ClassDefinition.load(Task.this.getClass());
			if(!def.hasAnnotation(HeavyTask.class)) {
				exec0Impl = new Provider<Runnable>() {
					final TaskMonitor monitor = new TaskMonitor(Task.this);
					Runnable interrupt = new Runnable() {
						public void run() {
							thread.interrupt();
							monitor.cancel();
						}
					};
					
					public Runnable provide() {
						monitor.start();
						return interrupt;
					}
				};
			} else {
				exec0Impl = new Provider<Runnable>() {
					Runnable interrupt = new Runnable() {
						public void run() {
							thread.interrupt();
						}
					};
					public Runnable provide() {
						return interrupt;
					}
				};
			}
		}
	};
	private Provider<Runnable> exec0Impl;
	protected final Prop<State> state = new Prop(State.Ready);
	
	protected final void exec0(PropAccessor<State> data) throws CancellationException, IllegalStateException {
		if(data.get() != State.Preparing) {
			if(data.get() == State.Canceled) // Cancelled
				throw new CancellationException("Task was cancelled before execution could start");
			throw new IllegalStateException("Task is not prepared to execute: " + data);
		}

		try {
			interrupt.run();
		} catch(Throwable t) {
			Logger.exception(Logger.Level.Warning, t);
		}
		thread = Thread.currentThread();
		interrupt = exec0Impl.provide();
		data.set(State.Executing);
	}
	
	/**
	 * Used by {@code syncExecution()} and {@code finalExecution()}.
	 * Verifies the current state before executing.
	 */
	protected final void syncExec0(PropAccessor<State> data, Runnable block) throws CancellationException, IllegalStateException {
		if(data.get() != State.Executing) {
			if(data.get() == State.Canceled)
				NXUtils.throwException(CancellationException.class, Task.this, "syncExec0", "Task cancelled before execution could complete.");
			Logger.warn("syncExec0 called while not executing or cancelled, this usually means performExecution was called before prepareImpl");
			NXUtils.throwException(IllegalStateException.class, Task.this, "syncExec0", "Task is not executing: " + state);
		} else
			block.run();
	}
	
	/**
	 * Used by {@code execute()}.
	 * Synchronizes a critical section while executing.
	 * 
	 * @param block The block of code to execute
	 * @throws InterruptedException if the task was Canceled during execution
	 * @throws IllegalThreadStateException if the task state is anything other than Executing or Canceled
	 */
	protected void syncExecution(final Runnable block) throws CancellationException, IllegalStateException {
		state.read(new VoidReader<PropAccessor<State>>() {
			@Override
			public void readV(PropAccessor<State> data) {
				syncExec0(data, block);
			}
		});
	}
	
	/**
	 * Used by {@code execute()}.
	 * Synchronizes the final section while executing.
	 * 
	 * @param block The block of code to execute
	 * @throws InterruptedException if the task was Canceled during execution
	 * @throws IllegalThreadStateException if the task state is anything other than Executing or Canceled
	 */
	protected void finalExecution(final Runnable block) throws CancellationException {
		state.write(new Writer<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				syncExec0(data, block);
				finalizeCalled = true;
			}
		});
	}
	
	/**
	 * Synchronizes a critical section of code that relies on the state of this task remaining the same.
	 * 
	 * @param block The block of code to execute
	*/
	public void sync(final Handler<State> processor) {
		state.read(new VoidReader<PropAccessor<State>>() {
			@Override
			public void readV(PropAccessor<State> data) {
				processor.handle(data.get());
			}
		});
	}
	
	/**
	 * Registers a Scheduler Implementation.
	 * 
	 * @param schedule Callback used if this task can be scheduled.
	 * @param interrupt Implementation used to interrupt the scheduled execution.
	 * @param success Callback used on successful execution.
	 * @throws IllegalStateException if already in a scheduler or queue, prepared, or currently executing.
	 */
	public void scheduleImpl(final Runnable schedule, final Runnable interrupt, final Runnable success) throws IllegalStateException {
		state.write(new Writer<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				if(!data.get().ready)
					throw new IllegalStateException("Task is not ready to be scheduled: " + data.get());
				if(data.get() == State.Scheduled)
					throw new IllegalStateException("Task is already scheduled for execution: " + data.get());
				if(data.get() == State.Enqueued)
					throw new IllegalStateException("Task is already enqueued for execution: " + data.get());

				try {
					Task.this.interrupt.run();
				} catch(NullPointerException ex) {}

				Task.this.interrupt = interrupt;
				Task.this.success = success;
				if(success != null)
					Logger.gears("Using Schedule Success Callback", success);
				data.set(State.Scheduled);
				try {
					schedule.run();
				} catch(Throwable t) {
					data.set(State.Aborted);
					try {
						Task.this.interrupt.run();
					} catch(Throwable ex) {}
					Task.this.interrupt = null;
					throw new RuntimeException("Exception occured in schedule implementation", t);
				}
			}
		});
	}
	
	/**
	 * Registers a Enqueued Implementation.
	 * 
	 * @param enqueue The callback used if this task can be enqueue
	 * @param interrupt The implementation used to interrupt the enqueued execution.
	 * @throws IllegalStateException if already in a queue, prepared, or currently executing.
	 */
	public void enqueueImpl(final Runnable enqueue, final Runnable interrupt) throws IllegalStateException {
		state.write(new Writer<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				if(!data.get().ready)
					throw new IllegalStateException("Task is not ready to be moved to a sink: " + data);
				if(data.get() == State.Enqueued)
					throw new IllegalStateException("Task is already enqueued for execution: " + data);

				try {
					Task.this.interrupt.run();
				} catch(NullPointerException ex) {}

				if(data.get() != State.Scheduled &&
						Task.this.success != NXUtils.NOP) {
					Logger.gears("Clearing Schedule Success Callback");
					Task.this.success = NXUtils.NOP;
				}

				Task.this.interrupt = interrupt;
				data.set(State.Enqueued);
				try {
					enqueue.run();
				} catch(Throwable t) {
					data.set(State.Aborted);
					try {
						Task.this.interrupt.run();
					} catch(Throwable ex) {}
					Task.this.interrupt = null;
					throw new RuntimeException("Exception occured in enqueue implementation", t);
				}
			}
		});
	}
	
	/**
	 * Registers a Prepare Implementation.
	 * 
	 * @param prepared The callback used if this task was prepared.
	 * @param interrupt The implementation used to interrupt the enqueued execution.
	 * @throws IllegalStateException if already prepared or currently executing.
	 */
	public void prepareImpl(final Runnable prepared, final Runnable interrupt) throws IllegalStateException {
		state.write(new Writer<PropAccessor<State>>() {
			@Override
			public void write(PropAccessor<State> data) {
				if(!data.get().ready)
					throw new IllegalStateException("Task is not ready: " + data.get());

				try {
					Task.this.interrupt.run();
				} catch(NullPointerException ex) {}

				if(data.get() != State.Scheduled &&
						data.get() != State.Enqueued &&
						Task.this.success != NXUtils.NOP) {
					Logger.gears("Clearing Schedule Success Callback");
					Task.this.success = NXUtils.NOP;
				}

				Task.this.interrupt = interrupt;
				data.set(State.Preparing);
				try {
					prepared.run();
				} catch(Throwable t) {
					data.set(State.Aborted);
					try {
						Task.this.interrupt.run();
					} catch(Throwable ex) {}
					Task.this.interrupt = null;
					throw new RuntimeException("Exception occured in enqueue implementation", t);
				}
			}
		});
	}
	
	/**
	 * Starts Execution.
	 * 
	 * @throws IllegalStateException if not prepared to execute..
	 */
	public void performExecution() throws IllegalStateException {
		try {
			state.write(new Writer<PropAccessor<State>>() {
				@Override
				public void write(PropAccessor<State> data) {
					exec0(data);
				}
			});
			try {
				aboutToExecute();
			} catch(Throwable tt) {
				Logger.exception(tt);
			}
			finalizeCalled = false;
			try {
				execute();
				if(!finalizeCalled)
					Logger.warn("Task.finalExecution not called by", Task.this.getClass().getName() + ".execute, State may become out of sync if cancel is called between the end of .execute and handling of its response.\nIf your task only needs one main synchronized block, than use a SynchronizedTask instead");
			} catch(CancellationException ex) {
				throw ex;
			} catch(IllegalStateException ex) {
				return;
			} catch(Throwable t) {
				try {
					interrupt.run();
				} catch(Throwable tt) {}
				interrupt = null;
				state.set(State.Aborted);
				try {
					onFailure(t);
				} catch(Throwable tt) {
					Logger.exception(tt);
				}
				return;
			}
			try {
				interrupt.run();
			} catch(Throwable t) {}
			interrupt = null;
			state.write(new Writer<PropAccessor<State>>() {
				@Override
				public void write(PropAccessor<State> data) {
					data.set(State.Complete);
					try {
						success.run();
					} catch(Throwable tt) {
						Logger.exception(Logger.Level.Gears, tt);
					}
				}
			});
			try {
				onSuccess();
			} catch(Throwable tt) {
				Logger.exception(tt);
			}
		} catch(CancellationException cancelled) {
			Logger.exception(Logger.Level.Gears, cancelled);
			return;
		}
		try {
			onComplete();
		} catch(Throwable tt) {
			Logger.exception(tt);
		}
	}
	
	public final void cancel() throws IllegalStateException{
		cancel(NXUtils.NOP);
	}
	public void cancel(final Runnable after) throws IllegalStateException{
		sync(new Handler<State>() {
			public void handle(State data) {
				if(!data.waiting)
					throw new IllegalStateException();

				try {
					interrupt.run();
				} catch(Throwable t) {}
				interrupt = null;
				state.set(State.Canceled);
				try {
					after.run();
				} catch(Throwable tt) {
					Logger.exception(tt);
				}
			}
		});
		try {
			onCancel();
		} catch(Throwable tt) {
			Logger.exception(tt);
		}
		try {
			onComplete();
		} catch(Throwable tt) {
			Logger.exception(tt);
		}
	}
	
	protected abstract void aboutToExecute();
	protected abstract void execute() throws InterruptedException;
	protected abstract void onFailure(Throwable reason);
	protected abstract void onSuccess();
	protected abstract void onComplete();
	protected abstract void onCancel();

	@Override
	public String toString() {
		return state.read(new Reader<String, PropAccessor<Task.State>>() {
			@Override
			public String read(PropAccessor<State> data) {
				return NXUtils.toString(Task.this, new Pair("state", data.get()), new Pair("thread", thread));
			}
		});
	}
	
}
