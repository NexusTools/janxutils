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
// mm
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author katelyn
 * @param <O>
 */
public abstract class RegexpProcessor<O> extends StreamReader<O> {
	
	public static class NotCompatibleException extends Exception {}
	
	public static interface StringReader {
		public String read(int length);
		public void reset();
		public void mark();
	}
	
	public static interface Processor<O> {
		public O process(StringReader reader) throws NotCompatibleException;
	}
	
	private int readPos;
	private final InputStreamReader reader;
	private final StringBuffer buffer = new StringBuffer();
	private final ArrayList<Processor<O>> processors = new ArrayList();
	public RegexpProcessor(InputStreamReader reader) {
		this.reader = reader;
	}
	
	protected void add(Processor<O> processor) {
		processors.add(processor);
	}

	@Override
	public O readNext() throws StreamReaderException {
		for(Processor<O> processor : processors) {
			final int startPos = readPos;
			try {
				O read = processor.process(new StringReader() {
					int markPos;
					public String read(int length) {
						int rem = length - (buffer.length()-readPos);
						if(rem > 0) {
							int read;
							char[] buff = new char[rem];
							try {
								read = reader.read(buff, 0, rem);
							} catch (IOException ex) {
								throw new RuntimeException(ex);
							}
							buffer.append(buff, 0, read);
						}
						return buffer.substring(readPos, readPos+length);
					}
					public void reset() {
						readPos = markPos;
					}
					public void mark() {
						markPos = readPos;
					}
				});
				
				buffer.delete(0, readPos);
				readPos = 0;
				return read;
			} catch (NotCompatibleException ex) {}
			readPos = startPos;
		}
		return null;
	}
    
}
