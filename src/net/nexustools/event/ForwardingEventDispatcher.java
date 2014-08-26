/*
 * jgenui is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * jgenui is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jgenui.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.nexustools.event;

import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.logic.VoidReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class ForwardingEventDispatcher<R extends RunQueue, L extends EventListener, E extends Event> extends DefaultEventDispatcher<R, L, E> {
	
	PropList<Processor<L, E>> sentDispatchers = new PropList();
	public ForwardingEventDispatcher(R queue) {
		super(queue);
	}

	@Override
	public void add(final L listener) {
		try {
			sentDispatchers.read(new VoidReader<ListAccessor<Processor<L, E>>>() {
				@Override
				public void readV(ListAccessor<Processor<L, E>> data) throws Throwable {
					ForwardingEventDispatcher.super.add(listener);
					for(Processor<L, E> processor : data)
						processor.dispatch(listener, processor.create());
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	@Override
	public void dispatch(final Processor<L, E> processor) {
		try {
			sentDispatchers.write(new Writer<ListAccessor<Processor<L, E>>>() {
				@Override
				public void write(ListAccessor<Processor<L, E>> data) throws Throwable {
					ForwardingEventDispatcher.super.dispatch(processor);
					data.push(processor);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
		
	}
	
}
