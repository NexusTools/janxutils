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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.runtime.logic.RunTask;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Pair;
import net.nexustools.utils.Testable;
import net.nexustools.utils.log.Logger;
import net.nexustools.utils.sort.AscLongTypeComparator;
import net.nexustools.utils.sort.DescLongTypeComparator;

/**
 *
 * @author kate
 */
public class FairTaskDelegator<F extends Task> extends SortedTaskDelegator<F> {
	
	private static final ThreadedRunQueue fairnessProcessor = new ThreadedRunQueue("Fairness", ThreadedRunQueue.Delegator.FCFS);
	private static class FairnessUpdate implements Runnable {
		WeakReference<FairTaskDelegator> delegator;
		public FairnessUpdate(FairTaskDelegator delegator) {
			this.delegator = new WeakReference(delegator);
		}
		public void run() {
			FairTaskDelegator delegator = this.delegator.get();
			if(delegator == null)
				throw new RunQueueScheduler.StopRepeating();
			delegator.updateLifetimes();
		}
	}
	private static class TimedTask implements Task {
		private final Task internal;
		private final FairTaskDelegator delegator;
		public TimedTask(Task internal, FairTaskDelegator delegator) {
			if(internal == null)
				throw new NullPointerException();
			
			this.internal = internal;
			this.delegator = delegator;
		}
		public Task.State state() {
			return internal.state();
		}
		public boolean isCancelled() {
			return internal.isCancelled();
		}
		public boolean isExecutable() {
			return internal.isExecutable();
		}
		public boolean isWaiting() {
			return internal.isWaiting();
		}
		public boolean didAbort() {
			return internal.didAbort();
		}
		public boolean isComplete() {
			return internal.isComplete();
		}
		public boolean isDone() {
			return internal.isDone();
		}
		public void cancel() {
			internal.cancel();
		}
		public void execute() {
			int key = hashFor(internal);
			Logger.gears("Executing TrackedTask", key);
			long start = System.currentTimeMillis();
			try {
				internal.execute();
			} finally {
				start = System.currentTimeMillis() - start;
				Logger.gears("Execution took " + start + "ms", key);
				delegator.pushLifetime(key, start);
			}
		}
		public boolean onSchedule() {
			return internal.onSchedule();
		}
		public Task copy(State state) {
			return new TimedTask(internal.copy(state), delegator);
		}
		@Override
		public String toString() {
			return "Tracked" + internal;
		}
		public void sync(Runnable block) {
			internal.sync(block);
		}
	}
	
	public static int hashFor(Task task) {
		if(task instanceof FairTarget)
			return ((FairTarget)task).fairHashCode();
		if(task instanceof RunTask) {
			Runnable runnable = ((RunTask)task).runnable;
			if(runnable instanceof FairTarget)
				return ((FairTarget)runnable).fairHashCode();
			return runnable.hashCode();
		}
		
		return task.hashCode();
	}
	
	public static interface FairTarget {
		public int fairHashCode();
	}
	public static interface FairRunnable extends Runnable, FairTarget {}
	public static interface LifetimeMultiplier extends Runnable {
		public int lifetimeMultiplier();
	}
	
	private final int sampleLength;
	private final int maxSampleCount;
	private final Prop<Boolean> scheduled = new Prop<Boolean>(false);
	private final PropMap<Integer, Long> lifetimeMap = new PropMap();
	private final HashMap<Integer, StrongTypeList<Long>> lifetimeSamplesMap = new HashMap();
	private final PropMap<Integer, Long> totalLifetimeMap = new PropMap();
	private final Comparator<F> comparator = new DescLongTypeComparator<F>() {
		@Override
		public long value(F o) {
			return lifetimeFor(o);
		}
	};
	public FairTaskDelegator() {
		this(30000, 120 * 5); // Should turn out to about 5 hours worth of samples, 600 total samples
	}
	public FairTaskDelegator(int sampleLength, int sampleCount) {
		maxSampleCount = sampleCount;
		this.sampleLength = sampleLength;
	}
	
	private void pushLifetime(final int hash, final long time) {
		try {
			lifetimeMap.write(new Writer<MapAccessor<Integer, Long>>() {
				@Override
				public void write(MapAccessor<Integer, Long> data) {
					Logger.gears("Pushing Fairness Lifetime", hash, time, FairTaskDelegator.this);
					data.put(hash, data.get(hash, 0L) + time);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	private void updateLifetimes() {
		queue.dirtyOperation(new Runnable() {
			public void run() {
				try {
					lifetimeMap.write(new Writer<MapAccessor<Integer, Long>>() {
						@Override
						public void write(MapAccessor<Integer, Long> data) {
							Logger.gears("Updating Fairness Lifetimes", FairTaskDelegator.this);
							StrongTypeList<Integer> processedKeys = new StrongTypeList();
							for(Pair<Integer, Long> entry : data) {
								Logger.gears(entry);
								processedKeys.push(entry.i);
								StrongTypeList<Long> samples = lifetimeSamplesMap.get(entry.i);
								if(samples == null) {
									if(entry.v < 1) // Skip creating entry
										continue;
									
									samples = new StrongTypeList();
									lifetimeSamplesMap.put(entry.i, samples);
								}
								samples.push(entry.v);
							}
							data.clear();
							
							for(int key : lifetimeSamplesMap.keySet()) {
								StrongTypeList<Long> samples = lifetimeSamplesMap.get(key);
								if(!processedKeys.contains(key))
									samples.push(0L);
								if(samples.length() >= maxSampleCount) {
									Logger.gears("Shifting sample", key);
									samples.remove(0);
								}
								
								int sampleCount = 0;
								long lifetime = (long)Integer.MIN_VALUE;
								Logger.gears("Reading samples", key, samples.length());
								for(Long sample : samples) {
									lifetime += sample;
									sampleCount++;
									if(lifetime < 0) {
										lifetime = Long.MAX_VALUE;
										sampleCount = maxSampleCount;
										Logger.gears("Lifetime maxed out...");
										break;
									}
								}
								
								if(lifetime > Integer.MIN_VALUE) {
									if(sampleCount < maxSampleCount) // Average out the samples to try and be fair to older clients
										lifetime *= maxSampleCount / sampleCount;
									
									Logger.gears("Updating total lifetime", key, lifetime);
									totalLifetimeMap.put(key, lifetime);
								} else {
									Logger.gears("Resetting total lifetime", key);
									lifetimeSamplesMap.remove(key);
									totalLifetimeMap.remove(key);
								}
							}
						}
					});
				} catch (InvocationTargetException ex) {
					throw NXUtils.wrapRuntime(ex);
				}
			}
		});
	}

	@Override
	public Comparator<F> comparator() {
		return comparator;
	}
	
	private long lifetimeFor(F queueFuture) {
		return lifetimeMap.get(hashFor(queueFuture), (long)Integer.MIN_VALUE);
	}
	
	protected F wrap(final F task) {
		return (F)new TimedTask(task, this);
	}

	@Override
	public F nextTask(ListAccessor<F> queue) {
		F nextTask = queue.pop();
		if(nextTask == null)
			return null;
		try {
			scheduled.write(new IfWriter<PropAccessor<Boolean>>() {
				@Override
				public boolean test(PropAccessor<Boolean> against) {
					return !against.get();
				}
				@Override
				public void write(PropAccessor<Boolean> data) {
					Logger.gears("Scheduling Fairness Update Task", FairTaskDelegator.this);
					fairnessProcessor.scheduleRepeating(new FairnessUpdate(FairTaskDelegator.this), sampleLength);
					data.set(true);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
		return wrap(nextTask);
	}
	
}
