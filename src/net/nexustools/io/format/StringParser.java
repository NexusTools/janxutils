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

import java.util.Iterator;

/**
 *
 * @author katelyn
 */
public abstract class StringParser<O> implements Iterable<O> {
	
	private final StringReader stringReader;
	public StringParser(StringReader stringReader) {
		this.stringReader = stringReader;
	}
	public StringParser() {
		this(null);
	}
	
	public final O parse() throws StringParserException{
		assert(stringReader != null);
		return parse(stringReader);
	}
	
	/**
	 * Parse an object out of a <code>StringReader</code>.
	 * 
	 * @param reader
	 * @return
	 * @throws StringParserException 
	 */
	public abstract O parse(StringReader reader) throws StringParserException;

	public Iterator<O> iterator() {
		return new Iterator<O>() {
			
			private O next;
			public boolean hasNext() {
				return (next = parse()) != null;
			}

			public O next() {
				return next;
			}

			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}
}
