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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author katelyn
 */
public class InputLineReader implements LineReader {
	
	final int maxLength;
	final InputStream inputStream;
	public InputLineReader(int maxLength, InputStream inputStream) {
		if(!inputStream.markSupported())
			throw new UnsupportedOperationException("Mark is required");
		
		this.inputStream = inputStream;
		this.maxLength = maxLength;
	}
	public InputLineReader(InputStream inputStream) {
		this(256, inputStream);
	}
	
	public String readNext() throws IOException {
		int read;
		int usefulLength = 0;
		char foundReturn = 0;
		StringBuilder builder = new StringBuilder();
		
		inputStream.mark(maxLength);
		while((read = inputStream.read()) > 0) {
			usefulLength ++;
			if(read == '\r' || read == '\n') {
				if(foundReturn != 0) {
					if(foundReturn == read)
						usefulLength--;
					break;
				}
				foundReturn = (char)read;
				continue;
			}
			if(foundReturn != 0) {
				usefulLength--;
				break;
			}
			builder.append((char)read);
		}
		
		if(usefulLength < 1)
			return null;
		
		inputStream.reset();
		inputStream.skip(usefulLength);
		return builder.toString();
	}
	
}
