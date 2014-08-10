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

package net.nexustools.io.format;

import java.io.IOException;

/**
 *
 * @author katelyn
 */
public interface StringReader {
	/**
	 * Reads a string from the underlying stream.
	 * 
	 * @param length Maximum length to read
	 * @return A string or null if nothing left
	 */
	public String read(int length) throws IOException;
	
	/**
	 * Trims data off the internal reader,
	 * this trims off all data since mark, as well as "usefulLength".
	 * 
	 * @param usefulLength The number of characters since mark that were actually useful, and should be trimmed
	 */
	public void reset(int usefulLength);
	
	/**
	 * Resets the internal reader to where it was when mark was called last.
	 */
	public void reset();
	
	/**
	 * Marks the current position in the internal reader,
	 * The reader can be {@code reset()} to this mark after.
	 */
	public void mark();
}
