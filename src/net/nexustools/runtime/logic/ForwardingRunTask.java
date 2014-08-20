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

import net.nexustools.concurrent.MapAccessor;
import net.nexustools.concurrent.logic.SoftWriter;

/**
 *
 * @author katelyn
 */
public class ForwardingRunTask<R extends Runnable> extends TrackedQueueFuture<R> {

	public ForwardingRunTask(final R runnable, State state) {
		super(runnable, state);
		
		write(new SoftWriter<MapAccessor<Runnable, TrackedQueueFuture>>() {
			TrackedQueueFuture found;
			@Override
			public boolean test(MapAccessor<Runnable, TrackedQueueFuture> against) {
				found = against.get(runnable);
				return found == null || found.isDone();
			}
			@Override
			public void write(MapAccessor<Runnable, TrackedQueueFuture> data) {
				data.put(runnable, ForwardingRunTask.this);
			}
			@Override
			public void soft(MapAccessor<Runnable, TrackedQueueFuture> data) {
				sCancel();
			}
		});
	}

	@Override
	public ForwardingRunTask copy(State state) {
		return new ForwardingRunTask(runnable, state);
	}
	
}
