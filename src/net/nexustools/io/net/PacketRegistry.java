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
import net.nexustools.utils.Pair;

/**
 *
 * @author kate
 */
public class PacketRegistry<P extends Packet> {
	
	public class Entry extends Pair<Constructor<? extends P>, Class<? extends P>> {
		
		public Entry(Constructor<? extends P> constructor, Class<? extends P> clazz) {
			super(constructor, clazz);
		}

		@Override
		public int hashCode() {
			return v.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Entry)obj).v == v;
		}
		
	}
	
	final PropList<Entry> registered = new PropList();
	
	public void register(Class<? extends P> packetClass) throws NoSuchMethodException {
		registered.unique(new Entry(packetClass.getConstructor(), packetClass));
	}
	
	public short idFor(Packet packet) throws NoSuchMethodException {
		return idFor((Class<? extends P>)packet.getClass());
	}
	
	public short idFor(Class<? extends P> packetClass) throws NoSuchMethodException {
		return (short) registered.indexOf(new Entry(packetClass.getConstructor(), packetClass));
	}
	
	public P create(short id) {
		try {
			return registered.get(id).i.newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (IndexOutOfBoundsException ex) {
			throw new RuntimeException("Packet ID not registered: " + id);
		}
	}
	
}
