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

/**
 *
 * @author katelyn
 */
public interface SelectionListener<I, S> extends EventListener {
	
	public static class SelectionEvent<I, S> extends Event<S> {
		public final I[] selection;
		public final long start;
		public final long stop;
		public SelectionEvent(S source, I[] selection, long start, long stop) {
			super(source);
			this.selection = selection;
			this.start = start;
			this.stop = stop;
		}
	}
	
	public void selectionChanged(SelectionEvent<I, S> event);
    
}
