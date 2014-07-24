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

package nexustools.io.format.impl;

import java.util.Map;
import nexustools.io.format.StreamTokenizer;
import nexustools.io.format.StreamTokenizerException;
import nexustools.io.format.impl.XMLTokenizer.Token;

/**
 *
 * @author katelyn
 */
public class XMLTokenizer extends StreamTokenizer<Token, Object> {
	
	public static enum Token {
		StartTag,
		EndTag,
		
		Comment,
		Attribute,
		TextElement,
		DataElement,
		
		XMLDeclaration,
		Instruction,
		DDT
	}

	@Override
	public Map.Entry<Token, TokenObject<Object>> readToken() throws StreamTokenizerException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void writeToken(Token token, TokenObject<Object> data) throws StreamTokenizerException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object readGenericObject() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void readObject(Object object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void writeGenericObject(Object object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void writeObject(Object object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
