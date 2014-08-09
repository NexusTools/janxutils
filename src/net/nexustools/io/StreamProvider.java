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

package net.nexustools.io;

import java.io.IOException;
import java.net.URI;

/**
 * Provides Streams
 * 
 * @author katelyn
 */
public interface StreamProvider {
	
	/**
	 * The scheme this provider is meant to open,
	 * null indicates that this acts as a fallback
	 * 
	 * @return
	 */
	public String scheme();
	
	/**
	 * Opens a stream
	 * 
	 * @param path The path to try and open, if protocol() returned null this may be a full url
	 * @param raw The raw URL String passed to Stream.open
	 * @param supportWriting
	 * @return
	 * @throws java.io.IOException
	 */
	public Stream open(String path, URI raw, boolean supportWriting) throws IOException;
	
}
