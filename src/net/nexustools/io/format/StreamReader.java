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
import java.io.OutputStream;
import java.util.Iterator;
import net.nexustools.concurrent.PropMap;
import net.nexustools.io.Stream;

/**
 * @author katelyn
 */
public abstract class StreamReader <O> implements Iterable<O> {
	
	private static final PropMap<String,StreamReaderProvider> providers = new PropMap();
	
	public static StreamReader create(String format, Stream stream) throws IOException {
		return create(format, stream.createInputStream(), stream.createOutputStream());
	}

	public static StreamReader create(String format, InputStream inStream, OutputStream outStream) {
		return null;
	}

	public static StreamReader create(String format, InputStream inStream) {
		return create(format, inStream, null);
	}

	public static StreamReader create(String format, OutputStream outStream) {
		return create(format, null, outStream);
	}
	
	/**
	 * Reads the next object from this stream.
	 * 
	 * @return
	 * @throws StreamReaderException 
	 */
	public abstract O readNext() throws StreamReaderException;
	
	@Override
	public Iterator<O> iterator() {
		return new Iterator<O>() {
			private O next;
			
			@Override
			public boolean hasNext() {
				try {
					O token = readNext();
					if(token == null)
						return false;
					next = token;
					return true;
				} catch (StreamReaderException ex) {
					throw new RuntimeException("Uncaught error while parsing stream.", ex);
				}
			}
			
			@Override
			public O next() {
				return next;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Tokens cannot be removed yet.");
			}
		};
	}

}
