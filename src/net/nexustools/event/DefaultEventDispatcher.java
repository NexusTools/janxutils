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
import net.nexustools.runtime.RunQueue;

/**
 *
 * @author katelyn
 */
public class DefaultEventDispatcher<R extends RunQueue, L extends EventListener, E extends Event> extends EventDispatcher<R, L, E> {
	
	public DefaultEventDispatcher(R queue) {
		super(queue);
	}
	
	public void connect() {}
	public void disconnect() {}
	
}
