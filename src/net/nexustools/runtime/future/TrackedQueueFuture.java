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

package net.nexustools.runtime.future;

import net.nexustools.concurrent.BaseReader;
import net.nexustools.concurrent.BaseWriter;
import net.nexustools.concurrent.IfWriter;
import net.nexustools.concurrent.MapAccessor;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.Writer;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class TrackedQueueFuture<R extends Runnable> extends RunnableQueueFuture {
	
	private static final PropMap<Runnable, TrackedQueueFuture> tracking = new PropMap();

	public TrackedQueueFuture(R runnable, State state) {
		super(runnable, state);
	}
	
	protected void write(BaseWriter<MapAccessor<Runnable, TrackedQueueFuture>> writer) {
		tracking.write(writer);
	}
	
	protected <R> R read(BaseReader<R, MapAccessor<Runnable, TrackedQueueFuture>> reader) {
		return tracking.read(reader);
	}
	
	protected void sCancel() {
		super.cancel();
	}

	@Override
	public void cancel() {
		removeFromTracker();
		sCancel();
	}
	
	protected void removeFromTracker() {
		tracking.write(new Writer<MapAccessor<Runnable, TrackedQueueFuture>>() {
			@Override
			public void write(MapAccessor<Runnable, TrackedQueueFuture> data) {
				TrackedQueueFuture queueFuture = data.take(runnable);
				if(queueFuture != TrackedQueueFuture.this)
					data.put(runnable, queueFuture); // Oops... apparently not myself anymore...
			}
		});
	}

	@Override
	protected void executeImpl(final Testable isRunning) {
		removeFromTracker();
		super.executeImpl(isRunning);
	}
	
}
