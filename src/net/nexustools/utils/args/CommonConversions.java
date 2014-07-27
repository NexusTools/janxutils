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

package net.nexustools.utils.args;

/**
 *
 * @author katelyn
 */
public class CommonConversions {
	public final static ArgumentConverter<Void> MustBeEmpty = new ArgumentConverter() {
		@Override
		public Void test(String[] strings) throws ArgumentParserException {
			if(strings.length > 0)
				throw new ArgumentParserException("Expects no value");
			
			return null;
		}
	};
	public final static ArgumentConverter<String> String = new ArgumentConverter() {
		@Override
		public String test(String[] strings) throws ArgumentParserException {
			if(strings.length != 1)
				throw new ArgumentParserException("Expect a single string");
			
			return strings[0];
		}
	};
	public final static ArgumentConverter<String[]> StringList = new ArgumentConverter() {
		@Override
		public String[] test(String[] strings) {
			return strings;
		}
	};
	
}
