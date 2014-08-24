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

import net.nexustools.concurrent.Lockable;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.logic.SoftUpdateWriter;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class TrackedTask<R extends Runnable> extends RunTask {
	
	private static final PropMap<Runnable, TrackedTask> tracking = new PropMap();

	public TrackedTask(R runnable, State state) {
		super(runnable, state);
	}
	
	protected void write(BaseWriter<MapAccessor<Runnable, TrackedTask>> writer) {
		tracking.write(writer);
	}
	
	protected <R> R read(BaseReader<R, MapAccessor<Runnable, TrackedTask>> reader) {
		return tracking.read(reader);
	}
	
	protected void sCancel() {
		super.cancel();
	}

	@Override
	public void cancel() {
		removeFromTracker(new Runnable() {
			public void run() {
				sCancel();
			}
		});
	}
	
	protected void removeFromTracker(final Runnable block) {
		final Testable<MapAccessor<Runnable, TrackedTask>> isMe = new Testable<MapAccessor<Runnable, TrackedTask>>() {
			public boolean test(MapAccessor<Runnable, TrackedTask> against) {
				return against.get(runnable) == TrackedTask.this;
			}
		};
		tracking.write(new BaseWriter<MapAccessor<Runnable, TrackedTask>>() {
			public void write(MapAccessor<Runnable, TrackedTask> data, Lockable lock) {
				lock.lock();
				try {
					block.run();
					if(isMe.test(data)) {
						try {
							boolean failed = false;
							if(!lock.tryFastUpgrade()) {
								lock.upgrade();
								if(!isMe.test(data))
									failed = true;
							}
							if(!failed)
								data.remove(runnable);
						} finally {
							lock.downgrade();
						}
					}
				} finally {
					lock.unlock();
				}
			}
		});
	}

	@Override
	protected void executeImpl(final Testable isRunning) {
		removeFromTracker(new Runnable() {
			public void run() {
				TrackedTask.super.executeImpl(isRunning);
			}
		});
	}
	
}
