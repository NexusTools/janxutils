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

package net.nexustools.io.net;

import java.util.EventListener;
import net.nexustools.event.Event;

/**
 *
 * @author kate
 */
public interface ClientConnectedListener<C extends Client, S> extends EventListener {
	
	public static class ClientConnectedEvent<C extends Client, S> extends Event<S> {
		final C client;
		public ClientConnectedEvent(S source, C client) {
			super(source);
			this.client = client;
		}
	}
	
	public void clientConnected(ClientConnectedEvent<C, S> clientConnectedEvent);
	
}
