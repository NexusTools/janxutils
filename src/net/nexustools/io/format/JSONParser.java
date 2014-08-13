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
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import net.nexustools.io.Stream;
import net.nexustools.io.format.JSONParser.Chunk;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class JSONParser extends StringParser<Chunk> {
	
	public static class Chunk {
		public static enum Type {
			StartBlock,
			StartList,
			EndBlock,
			EndList,
			
			Value,
			Key
		}
		public final Type type;
		public final String content;
		protected Chunk(Type type, String content) {
			this.type = type;
			this.content = content;
		}
		
		public String decodeContent() {
			// Decode xml entities
			return content;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(type.toString());
			builder.append(' ');
			builder.append(content);
			return builder.toString();
		}
	}
	
	private static final RegexParser<Chunk.Type> regexParser;
	static {
		regexParser = new RegexParser();
		regexParser.add(Chunk.Type.StartBlock, "^[\\n\\r\\s]*\\{");
		regexParser.add(Chunk.Type.StartList, "^[\\n\\r\\s]*\\[");
		
		regexParser.add(Chunk.Type.EndList, "^[\\n\\r\\s]*\\]");
		regexParser.add(Chunk.Type.EndBlock, "^[\\n\\r\\s]*\\}");
		
		regexParser.add(Chunk.Type.Key, "^[\\n\\r\\s]*\"([^\"]*)\":");
		regexParser.add(Chunk.Type.Value, "^[\\n\\r\\s]*\"([^\"]*)\",");
	}
	
	public JSONParser(String uri) throws IOException, URISyntaxException {
		super(new BufferedStringReader(uri));
	}
	public JSONParser(Stream stream) throws IOException {
		super(new BufferedStringReader(stream));
	}
	public JSONParser(StringReader stringReader) {
		super(stringReader);
	}
	public JSONParser() {}
	
	public Chunk parse(StringReader reader) throws StringParserException {
		Pair<Chunk.Type, Matcher> data = regexParser.parse(reader);
		if(data == null)
			return null;
		
		String content = null;
		switch(data.i) {
			case Key:
			case Value:
				content = data.v.group(1);
				break;
		}
		
		return new Chunk(data.i, content);
	}
	
}
