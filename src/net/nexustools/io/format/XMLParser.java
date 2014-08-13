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
import java.util.regex.Pattern;
import net.nexustools.io.Stream;
import net.nexustools.io.format.XMLParser.Chunk;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class XMLParser extends StringParser<Chunk> {
	
	public static class Chunk {
		public static enum Type {
			DocumentType,
			Instruction,
			Comment,

			StartTag,
			StartShortTag,
			Attribute,
			EndShortTag,
			Content,
			EndTag,
			CData
		}
		public final Type type;
		public final String key;
		public final String content;
		protected Chunk(Type type, String key, String content) {
			this.type = type;
			
			this.key = key;
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
			builder.append(key);
			builder.append(' ');
			builder.append(content);
			return builder.toString();
		}
	}
	
	private static final RegexParser<Chunk.Type> regexParser;
	static {
		regexParser = new RegexParser();
		regexParser.add(Chunk.Type.Comment, "^>?[\\n\\r\\s]*<!--\\s*(.+?)\\s*--!>");
		regexParser.add(Chunk.Type.Instruction, "^>?[\\n\\r\\s]*<\\?([a-zA-Z]+[a-zA-Z0-9\\-]*)(\\s+(.+?))?\\?>");
		regexParser.add(Chunk.Type.DocumentType, "^>?[\\n\\r\\s]*<!DOCTYPE\\s+([^>]+)>", Pattern.CASE_INSENSITIVE);
		
		regexParser.add(Chunk.Type.StartTag, "^>?[\\n\\r\\s]*<([a-zA-Z]+[a-zA-Z0-9\\:]*)\\s+");
		regexParser.add(Chunk.Type.StartShortTag, "^>?[\\n\\r\\s]*<([a-zA-Z]+[a-zA-Z0-9\\:]*)\\s*>([a-zA-Z0-9\\s\\n\\r]+)?");
		regexParser.add(Chunk.Type.Attribute, "^\\s*([a-zA-Z]+[a-zA-Z0-9\\-]*)=(\"([^\"]*)\"|'([^']*)')\\s*");
		regexParser.add(Chunk.Type.EndTag, "^[\\n\\r\\s]*</\\s*([a-zA-Z]+[a-zA-Z0-9\\:]*)\\s*>");
		regexParser.add(Chunk.Type.Content, "^\\s*>([a-zA-Z0-9\\s\\n\\r]+)?");
		regexParser.add(Chunk.Type.EndShortTag, "^\\s*/>");
		
		regexParser.add(Chunk.Type.CData, "^[\\n\\r\\s]*<!\\[CDATA\\[(.+?)\\]\\]>");
	}
	
	public XMLParser(String uri) throws IOException, URISyntaxException {
		super(new BufferedStringReader(uri));
	}
	public XMLParser(Stream stream) throws IOException {
		super(new BufferedStringReader(stream));
	}
	public XMLParser(StringReader stringReader) {
		super(stringReader);
	}
	public XMLParser() {}
	
	public Chunk parse(StringReader reader) throws StringParserException {
		Pair<Chunk.Type, Matcher> data = regexParser.parse(reader);
		if(data == null)
			return null;
		
		String key = null;
		String content = null;
		switch(data.i) {
			case Content:
				content = data.v.group(1).trim();
				if(content.length() < 1)
					content = null;
				break;
				
			case CData:
			case Comment:
			case DocumentType:
				content = data.v.group(1);
				break;
				
			case Attribute:
				key = data.v.group(1);
				content = data.v.group(3);
				break;
				
			case Instruction:
				key = data.v.group(1);
				content = data.v.group(2);
				break;
				
			case StartShortTag:
				content = data.v.group(2).trim();
				if(content.length() < 1)
					content = null;
			case StartTag:
				key = data.v.group(1);
				break;
			
			case EndTag:
				key = data.v.group(1);
				break;
		}
		
		return new Chunk(data.i, key, content);
	}
	
}
