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

package net.nexustools.data;

import java.io.IOException;

/**
 *
 * @author katelyn
 */
public interface Storage {
	
	public Storage partition(String part) throws SubPartitionsNotSupported;
	
	public byte readByte(String name) throws IOException;
	public short readShort(String name) throws IOException;
	public int readInt(String name) throws IOException;
	
	public float readFloat(String name) throws IOException;
	public double readDouble(String name) throws IOException;
	
	public boolean readBoolean(String name) throws IOException;
	public String readString(String name) throws IOException;
	
	/**
	 * Reads an instance of clazz out of Storage.
	 * 
	 * @param name
	 * @param clazz
	 * @return 
	 * @throws IOException 
	 */
	public Object readObject(String name, Class<?> clazz) throws IOException;
	
	/**
	 * Initializes an object by reading the contents of storage.
	 * 
	 * @param name
	 * @param instance
	 * @throws IOException 
	 */
	public void readObject(String name, Object instance) throws IOException;
	
	/**
	 * Reads an object generically out of storage.
	 * 
	 * The read object must have been stored via writeGenericObject(),
	 * as it uses the class information to figure out which object to load.
	 * 
	 * @param name
	 * @return
	 * @throws IOException 
	 */
	public Object readGenericObject(String name) throws IOException;
	
	public byte writeByte(String name) throws IOException;
	public short writeShort(String name) throws IOException;
	public int writeInt(String name) throws IOException;
	
	public float writeFloat(String name) throws IOException;
	public double writeDouble(String name) throws IOException;
	
	public boolean writeBoolean(String name) throws IOException;
	public String writeString(String name) throws IOException;
	
	/**
	 * Writes an object, without its class info,
	 * for use with readObject(Class<?>) and readObject(Object).
	 * 
	 * @param instance
	 * @throws IOException 
	 */
	public void writeObject(Object instance) throws IOException;
	
	/**
	 * Writes an object, storing its class info for use with readGenericObject().
	 * 
	 * @param instance
	 * @throws IOException 
	 */
	public void writeGenericObject(Object instance) throws IOException;
	
}
