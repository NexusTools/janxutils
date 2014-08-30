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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import net.nexustools.data.buffer.basic.ByteArrayBuffer;
import net.nexustools.utils.StringUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class InputLineReader implements LineReader {
	
	final int maxLength;
	final Charset charset;
	final InputStream inputStream;
	public InputLineReader(int maxLength, InputStream inputStream, Charset charset) {
		if(!inputStream.markSupported())
			throw new IllegalArgumentException("Mark is required");
		
		this.inputStream = inputStream;
		this.maxLength = maxLength;
		this.charset = charset;
	}
	public InputLineReader(int maxLength, InputStream inputStream) {
		this(maxLength, inputStream, StringUtils.UTF8);
	}
	public InputLineReader(InputStream inputStream, Charset charset) {
		this(256, inputStream, charset);
	}
	public InputLineReader(InputStream inputStream) {
		this(inputStream, StringUtils.UTF8);
	}
	
	public String readNext() throws IOException {
		int usefulLength = 0;
		int stringLength = 0;
		boolean killNext = false;
		
		inputStream.mark(maxLength);
		byte[] buffer = new byte[maxLength];
		int read = inputStream.read(buffer);
		byte current;
		for(int i=0; i<read; i++) {
			current = buffer[i];
			usefulLength ++;
			
			// TODO: Investigate if charsets provide different line endings
			if(current == (byte)'\r' && !killNext){
				killNext = true;
				continue;
			}
			if(current == (byte)'\r' || current == (byte)'\n')
				break;
			if(killNext) {
				usefulLength --;
				break;
			}
			stringLength++;
		}
		
		if(usefulLength < 1)
			throw new EOFException();
		
		inputStream.reset();
		inputStream.skip(usefulLength);
		return new String(buffer, 0, stringLength, charset);
	}
	
}
