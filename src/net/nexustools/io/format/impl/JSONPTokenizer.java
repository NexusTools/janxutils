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

package net.nexustools.io.format.impl;

import java.util.Map;
import net.nexustools.io.format.impl.JSONPTokenizer.Token;
import net.nexustools.io.format.StreamTokenizer;
import net.nexustools.io.format.StreamTokenizerException;

/**
 *
 * @author katelyn
 */
public class JSONPTokenizer extends StreamTokenizer<Token, Object> {
	
	public static enum Token {
		StartArray,
		StartObject,
		EndObject,
		EndArray,
		
		NumberValue,
		StringValue,
		
		Comment // non-strict only
	}
	
	protected static interface SyntaxValidator {
		public void validate(String block) throws StreamTokenizerException;
	}
	
	private final SyntaxValidator validator;
	
	// Non-strict parsing is the basis of YML
	public JSONPTokenizer() {
		this(false);
	}
	
	public JSONPTokenizer(boolean strict) {
		this(strict ? new SyntaxValidator() {
			@Override
			public void validate(String block) throws StreamTokenizerException {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		} : new SyntaxValidator() {
			@Override
			public void validate(String block) throws StreamTokenizerException {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		});
	}
	
	protected JSONPTokenizer(SyntaxValidator validator) {
		this.validator = validator;
	}

	@Override
	public Map.Entry<Token, TokenObject<Object>> readToken() throws StreamTokenizerException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void writeToken(Token token, TokenObject data) throws StreamTokenizerException {
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