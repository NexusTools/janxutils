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
public interface ValueListener<V, S> extends EventListener {
	
	public static class ValueEvent<V, S> extends Event<S> {
		public final V value;
		public ValueEvent(S source, V val) {
			super(source);
			this.value = val;
		}
	}
	
	public void valueChanged(ValueEvent<V, S> event);
	
}
