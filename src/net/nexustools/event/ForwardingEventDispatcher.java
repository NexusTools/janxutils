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
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.logic.VoidReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class ForwardingEventDispatcher<S extends TaskSink, L extends EventListener, E extends Event> extends DefaultEventDispatcher<S, L, E> {
	
	PropList<Processor<L, E>> sentDispatchers = new PropList();
	public ForwardingEventDispatcher(S queue) {
		super(queue);
	}

	@Override
	public void add(final L listener) {
		sentDispatchers.read(new VoidReader<ListAccessor<Processor<L, E>>>() {
			@Override
			public void readV(ListAccessor<Processor<L, E>> data) {
				ForwardingEventDispatcher.super.add(listener);
				for(Processor<L, E> processor : data)
					processor.dispatch(listener, processor.create());
			}
		});
	}

	@Override
	public void dispatch(final Processor<L, E> processor) {
		sentDispatchers.write(new Writer<ListAccessor<Processor<L, E>>>() {
			@Override
			public void write(ListAccessor<Processor<L, E>> data) {
				ForwardingEventDispatcher.super.dispatch(processor);
				data.push(processor);
			}
		});
	}
	
}
