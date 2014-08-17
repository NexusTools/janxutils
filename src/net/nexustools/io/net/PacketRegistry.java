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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.nexustools.concurrent.PropList;

/**
 *
 * @author kate
 */
public class PacketRegistry<P extends Packet> {
	
	final PropList<Constructor<? extends P>> registered = new PropList(); 
	
	public void register(Class<? extends P> packetClass) throws NoSuchMethodException {
		registered.unique(packetClass.getConstructor());
	}
	
	public P create(short id) {
		try {
			return registered.get(id).newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
