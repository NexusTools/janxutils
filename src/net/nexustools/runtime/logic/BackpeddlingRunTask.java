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

import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.concurrent.logic.SoftUpdateWriter;
import net.nexustools.concurrent.logic.Writer;

/**
 *
 * @author katelyn
 */
public class BackpeddlingRunTask<R extends Runnable> extends TrackedTask<R> {

	public BackpeddlingRunTask(final R runnable, State state) {
		super(runnable, state);
		write(new Writer<MapAccessor<Runnable, TrackedTask>>() {
			@Override
			public void write(MapAccessor<Runnable, TrackedTask> data) {
				TrackedTask old = data.replace(runnable, BackpeddlingRunTask.this);
				if(old != null)
					old.sCancel();
			}
		});
	}

	@Override
	public BackpeddlingRunTask copy(State state) {
		return new BackpeddlingRunTask(runnable, state);
	}
	
}
