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
import java.util.ArrayList;
import java.util.regex.Pattern;
import nexustools.io.format.FormatParser.Matcher;

/**
 *
 * @author katelyn
 */
public abstract class FormatParser<M extends Matcher> extends ArrayList<M> {
	
	public static interface Matcher {
		public boolean matches(byte[] raw, String buffer);
		public int readSize();
	}
	
	public static abstract class PatternMatcher implements Matcher {
		
		private final Pattern _pattern;
		public PatternMatcher(String pattern) {
			this(pattern, 0);
		}
		
		public PatternMatcher(String pattern, int flags) {
			_pattern = Pattern.compile(pattern, flags);
		}

		@Override
		public boolean matches(byte[] raw, String buffer) {
			java.util.regex.Matcher matcher = _pattern.matcher(buffer);
			
			// TODO: Implement
			return false;
		}
		
	}
	
	public FormatParser() {}
	
	public M findNext(InputStream stream) throws IOException {
		int maxRead = 0;
		for(M matcher : this) {
			int nextRead = matcher.readSize();
			if(nextRead > maxRead)
				maxRead = nextRead;
		}
		stream.mark(maxRead);
		byte[] rawBuffer = new byte[maxRead];
		if(stream.read(rawBuffer) < maxRead)
			throw new IOException("Unexpected end of stream");
		String string = new String(rawBuffer);
		for(M matcher : this) {
			if(matcher.matches(rawBuffer, string))
				return matcher;
		}
		stream.reset();
		return null;
	}
	
}
