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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.concurrent.logic.SoftUpdateWriter;
import net.nexustools.concurrent.logic.SoftWriter;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class ForwardingRunTask<R extends Runnable> extends TrackedTask<R> {

	public ForwardingRunTask(final R runnable, State state) {
		super(runnable, state);
		try {
			write(new SoftWriter<MapAccessor<Runnable, TrackedTask>>() {
				TrackedTask found;
				@Override
				public boolean test(MapAccessor<Runnable, TrackedTask> against) {
					found = against.get(runnable);
					return found == null || found.isDone();
				}
				@Override
				public void write(MapAccessor<Runnable, TrackedTask> data) {
					data.put(runnable, ForwardingRunTask.this);
				}
				@Override
				public void soft(MapAccessor<Runnable, TrackedTask> data) {
					sCancel();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	@Override
	public ForwardingRunTask copy(State state) {
		return new ForwardingRunTask(runnable, state);
	}
	
}
