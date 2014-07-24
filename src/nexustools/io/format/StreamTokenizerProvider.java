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

package nexustools.io.format;

import java.io.IOException;
import java.io.InputStream;
import nexustools.io.Stream;

/**
 *
 * @author katelyn
 */
public interface StreamTokenizerProvider {
    
	
	/**
	 * The format of data this parser is meant for
	 * 
	 * @return
	 */
	public String format();
	
	/**
	 * Opens a stream
	 * 
	 * @param inStream The InputStream to create a parser using
	 * @return
	 * @throws java.io.IOException
	 */
	public Stream create(InputStream inStream) throws IOException;
	
	
}