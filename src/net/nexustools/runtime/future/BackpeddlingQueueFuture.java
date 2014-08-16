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

import net.nexustools.concurrent.MapAccessor;
import net.nexustools.concurrent.logic.Writer;

/**
 *
 * @author katelyn
 */
public class BackpeddlingQueueFuture<R extends Runnable> extends TrackedQueueFuture<R> {

	public BackpeddlingQueueFuture(final R runnable, State state) {
		super(runnable, state);
		
		write(new Writer<MapAccessor<Runnable, TrackedQueueFuture>>() {
			@Override
			public void write(MapAccessor<Runnable, TrackedQueueFuture> data) {
				TrackedQueueFuture old = data.replace(runnable, BackpeddlingQueueFuture.this);
				if(old != null)
					old.sCancel();
			}
		});
	}
	
}
