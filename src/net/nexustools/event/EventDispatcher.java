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

import java.util.EventListener;
import java.util.List;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.PropList;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class EventDispatcher<R extends RunQueue, L extends EventListener, E extends Event> {
	
	public static interface Processor<L extends EventListener, E extends Event> {
		public E create();
		public void dispatch(L listener, E event);
	}
	
	private final R queue;
	protected final PropList<L> listeners = new PropList();
	public EventDispatcher(R queue) {
		this.queue = queue;
	}
	
	public void dispatch(final Processor<L, E> processor) {
		final List<L> cListeners = listeners.copy();
		if(cListeners.size() < 1)
			return;
		
		queue.push(new Runnable() {
			public void run() {
				final E event = processor.create();
				Logger.debug(event);
				if(cListeners.size() == 1) {
					Logger.debug("Dispatching Event");
					processor.dispatch(cListeners.get(0), event);
					return;
				}
				
				Logger.debug("Dispatching Event to " + cListeners.size() + " listeners");
				for(final L listener : cListeners)
					queue.push(new Runnable() {
						public void run() {
							Logger.debug("Dispatching Event");
							processor.dispatch(listener, event);
						}
					});
			}
		});
	}
	
	public void add(final L listener) {
		listeners.write(new IfWriter<ListAccessor<L>>() {
			@Override
			public void write(ListAccessor<L> data) {
				if(!data.isTrue())
					connect();
				
				data.push(listener);
			}
			@Override
			public boolean test(ListAccessor<L> against) {
				return !against.contains(listener);
			}
		});
	}
	
	public void remove(final L listener) {
		listeners.write(new IfWriter<ListAccessor<L>>() {
			@Override
			public void write(ListAccessor<L> data) {
				data.remove(listener);
				
				if(!data.isTrue())
					disconnect();
			}
			@Override
			public boolean test(ListAccessor<L> against) {
				return against.contains(listener);
			}
		});
	}
	
	public abstract void connect();
	public abstract void disconnect();
	
}
