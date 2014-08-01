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
public abstract class StringProcessingReader<O> extends StreamReader<O> {
	
	private int readPos;
	private final InputStreamReader reader;
	private final StringBuffer buffer = new StringBuffer();
	private final ArrayList<StringProcessor<O>> processors = new ArrayList();
	public StringProcessingReader(InputStreamReader reader) {
		this.reader = reader;
	}
	
	protected void add(StringProcessor<O> processor) {
		processors.add(processor);
	}

	@Override
	public O readNext() throws StreamReaderException {
		for(StringProcessor<O> processor : processors) {
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
					public void resetTrimmed(int usefulLength) {
						buffer.delete(0, markPos + usefulLength);
						readPos = 0;
					}
					public void reset() {
						readPos = markPos;
					}
					public void mark() {
						markPos = readPos;
					}
				});
				
				readPos = 0;
				return read;
			} catch (StringProcessorNotCompatibleException ex) {}
			readPos = startPos;
		}
		return null;
	}
    
}
