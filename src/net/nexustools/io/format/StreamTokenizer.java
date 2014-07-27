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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.nexustools.io.Stream;
import net.nexustools.io.format.StreamTokenizer.TokenObject;

/**
 * Each parser should implement its own methods for
 * obtaining information about the currently read token.
 * 
 * @author katelyn
 * @param <T> The token enum
 */
public abstract class StreamTokenizer <T extends Enum, D> implements Iterable<Map.Entry<T, TokenObject>> {
	
	public class TokenObject <V> {
		
		public final V value;
		public final String path;
		
		protected TokenObject(String path, V value) {
			this.path = path;
			this.value = value;
		}
		
	}
	
	private static HashMap<String,StreamTokenizerProvider> providers = new HashMap();
	
	public static StreamTokenizer create(String format, Stream stream) throws IOException {
		return create(format, stream.createInputStream(), stream.createOutputStream());
	}

	public static StreamTokenizer create(String format, InputStream inStream, OutputStream outStream) {
		return null;
	}

	public static StreamTokenizer create(String format, InputStream inStream) {
		return create(format, inStream, null);
	}

	public static StreamTokenizer create(String format, OutputStream outStream) {
		return create(format, null, outStream);
	}
	
	/**
	 * Reads a token from the stream
	 * 
	 * @return
	 * @throws StreamTokenizerException 
	 */
	public abstract Map.Entry<T, TokenObject<D>> readToken() throws StreamTokenizerException;
	
	/**
	 * Writes a token and data to the stream,
	 * 
	 * 
	 * @param token
	 * @param data
	 * @throws StreamTokenizerException 
	 */
	public abstract void writeToken(T token, TokenObject<D> data) throws StreamTokenizerException;

	@Override
	public Iterator<Map.Entry<T, TokenObject>> iterator() {
		return new Iterator<Map.Entry<T, TokenObject>>() {
			private Map.Entry<T, TokenObject> next;
			
			@Override
			public boolean hasNext() {
				try {
					Map.Entry<T, TokenObject<D>> token = readToken();
					if(token == null)
						return false;
					next = new AbstractMap.SimpleEntry<T, TokenObject>(token.getKey(), token.getValue());
					return true;
				} catch (StreamTokenizerException ex) {
					throw new RuntimeException("Uncaught error while parsing stream.", ex);
				}
			}
			
			@Override
			public Map.Entry<T, TokenObject> next() {
				return next;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Tokens cannot be removed yet.");
			}
		};
	}

	public abstract Object readGenericObject();
	public abstract void readObject(Object object);
	
	public abstract void writeGenericObject(Object object);
	public abstract void writeObject(Object object);
	
}
