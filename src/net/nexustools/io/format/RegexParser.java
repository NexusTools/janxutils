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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class RegexParser<K> extends StringParser<Pair<K, Matcher>> {
	
	private static final Pattern whitespace = Pattern.compile("^\\s*$");
	
	private int readAhead = 0;
	private Pair<K, Pattern> fallbackPattern = null;
	private final HashMap<K, Pair<Integer, Pattern>> patterns = new HashMap();
	public RegexParser(Map<K, Pair<Integer, Pattern>> otherMap) {
		patterns.putAll(otherMap);
	}
	public RegexParser() {}
	
	public void add(K key, String pattern, int flags) throws PatternSyntaxException {
		add(key, Pattern.compile(pattern, flags));
	}
	public void add(K key, String pattern) throws PatternSyntaxException {
		add(key, Pattern.compile(pattern));
	}
	public void add(K key, Pattern pattern) {
		add(key, pattern, 2048);
	}
	public void add(K key, Pattern pattern, int readAhead) {
		patterns.put(key, new Pair(readAhead, pattern));
		if(this.readAhead < 0)
			for(Pair<Integer, Pattern> pair : patterns.values())
				this.readAhead = Math.max(this.readAhead, pair.i);
		else
			this.readAhead = Math.max(this.readAhead, readAhead);
	}
	
	public void setFallback(K key, String pattern) {
		setFallback(key, Pattern.compile(pattern));
	}
	public void setFallback(K key, String pattern, int flags) {
		setFallback(key, Pattern.compile(pattern, flags));
	}
	public void setFallback(K key, Pattern pattern) {
		fallbackPattern = new Pair(key, pattern);
	}
	
	public void remove(K key) {
		patterns.remove(key);
	}

	public Pair<K, Matcher> parse(StringReader reader) throws StringParserException {
		reader.mark();
		int usefulLength = 0;
		try {
			Matcher matcher;
			String buffer = reader.read(readAhead);
			if(buffer == null)
				return null;
			
			for(Map.Entry<K, Pair<Integer, Pattern>> entry : patterns.entrySet()) {
				matcher = entry.getValue().v.matcher(buffer);
				if(matcher.find()) {
					usefulLength = matcher.group(0).length();
					return new Pair(entry.getKey(), matcher);
				}
			}
			
			if(fallbackPattern != null) {
				matcher = fallbackPattern.v.matcher(buffer);
				if(matcher.matches()) {
					usefulLength = matcher.group(0).length();
					return new Pair(fallbackPattern.i, matcher);
				}
			}
			matcher = whitespace.matcher(buffer);
			if(matcher.matches()) {
				usefulLength = matcher.group(0).length();
				return null;
			}
			System.out.println(buffer);
			throw new StringParserNotCompatible("No matching patterns found.");
		} catch (IOException ex) {
			throw new StringParserException(ex);
		} finally {
			reader.reset(usefulLength);
		}
	}
	
}
