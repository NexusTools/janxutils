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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.MapAccessor;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.VoidReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.runtime.logic.RunTask;
import net.nexustools.runtime.logic.Task;
import net.nexustools.utils.Pair;
import net.nexustools.utils.log.Logger;

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
	private static class TrackedTask implements Task {
		private final Task internal;
		private final FairTaskDelegator delegator;
		public TrackedTask(Task internal, FairTaskDelegator delegator) {
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
				Logger.gears("Execution took " + start + "ms", key);
				delegator.pushLifetime(key, System.currentTimeMillis() - start);
			}
		}
		public boolean onSchedule() {
			return internal.onSchedule();
		}
		public Task copy(State state) {
			return new TrackedTask(internal.copy(state), delegator);
		}
		@Override
		public String toString() {
			return "Tracked" + internal;
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
	private final HashMap<Integer, ArrayList<Long>> lifetimeSamplesMap = new HashMap();
	private final PropMap<Integer, Long> totalLifetimeMap = new PropMap();
	private final Comparator<F> comparator = new Comparator<F>() {
		public int compare(F o1, F o2) {
			long when = lifetimeFor(o1) - lifetimeFor(o2);
			if(when > Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			return (int)when;
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
		lifetimeMap.write(new Writer<MapAccessor<Integer, Long>>() {
			@Override
			public void write(MapAccessor<Integer, Long> data) {
				Logger.gears("Pushing Fairness Lifetime", hash, time, FairTaskDelegator.this);
				data.put(hash, data.get(hash, 0L) + time);
			}
		});
	}
	
	private void updateLifetimes() {
		lifetimeMap.read(new VoidReader<MapAccessor<Integer, Long>>() {
			@Override
			public void readV(MapAccessor<Integer, Long> data) {
				Logger.gears("Updating Fairness Lifetimes", FairTaskDelegator.this);
				for(Pair<Integer, Long> entry : data) {
					ArrayList<Long> samples = lifetimeSamplesMap.get(entry.i);
					if(samples == null) {
						if(entry.v < 1) // Skip creating entry
							continue;
						
						samples = new ArrayList();
					}
					samples.add(entry.v);
				}
				data.clear();
				
				for(int key : lifetimeSamplesMap.keySet()) {
					ArrayList<Long> samples = lifetimeSamplesMap.get(key);
					if(samples.size() >= maxSampleCount)
						samples.remove(0);
					
					long lifetime = 0;
					for(Long sample : samples) {
						lifetime += sample;
						if(lifetime < 0) {
							lifetime = Long.MAX_VALUE;
							Logger.gears("Lifetime maxd out...");
							break;
						}
					}
					Logger.gears("Updating total lifetime", key, lifetime);
					
					if(lifetime > 0)
						totalLifetimeMap.put(key, lifetime);
					else {
						lifetimeSamplesMap.remove(key);
						totalLifetimeMap.remove(key);
					}
				}
			}
		});
	}

	@Override
	public Comparator<F> comparator() {
		return comparator;
	}
	
	private long lifetimeFor(F queueFuture) {
		return lifetimeMap.get(hashFor(queueFuture), 0L);
	}
	
	protected F wrap(final F task) {
		return (F)new TrackedTask(task, this);
	}

	@Override
	public F nextTask(ListAccessor<F> queue) {
		F nextTask = queue.pop();
		if(nextTask == null)
			return null;
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
		return wrap(nextTask);
	}
	
}
