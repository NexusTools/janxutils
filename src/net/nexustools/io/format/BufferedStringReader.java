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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import net.nexustools.io.Stream;

/**
 *
 * @author katelyn
 */
public class BufferedStringReader implements StringReader {
	
	private int pos = 0;
	private int mark = -1;
	private final InputStreamReader inReader;
	private final StringBuffer buffer = new StringBuffer();
	public BufferedStringReader(InputStream inStream) {
		inReader = new InputStreamReader(inStream);
	}
	public BufferedStringReader(Stream stream) throws IOException {
		this(stream.createInputStream());
	}
	public BufferedStringReader(String uri) throws IOException, URISyntaxException {
		this(Stream.open(uri));
	}

	public String read(int length) throws IOException {
		if(pos+length <= buffer.length())
			return buffer.substring(pos += length, length);
		
		StringBuilder readBuff = new StringBuilder();
		if(pos < buffer.length()) {
			readBuff.append(buffer.subSequence(pos, buffer.length()));
			length -= readBuff.length();
		}
		
		if(length > 0) {
			int len = Math.min(length, 512), read;
			char[] buff = new char[len];
			while(length > 0) {
				length -= read = inReader.read(buff, 0, len);
				if(read < 1)
					break; // Nothing Left
				buffer.append(buff, 0, read);
				readBuff.append(buff, 0, read);
				if(length > 0)
					len = Math.min(length, 512);
			}
		}
		
		if(readBuff.length() < 1)
			return null;
		
		pos += readBuff.length();
		return readBuff.toString();
	}

	public void reset(int usefulLength) throws StringIndexOutOfBoundsException {
		assert(mark > -1);
		assert(buffer.length() < usefulLength);
		buffer.delete(0, usefulLength);
		mark = -1;
		pos = 0;
	}

	public void seek(int pos) {
		this.pos = pos;
	}
	
	public void reset() {
		assert(mark > -1);
		pos = mark;
		mark = -1;
	}

	public void mark() {
		mark = pos;
	}
	
}
