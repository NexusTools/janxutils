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

package net.nexustools.data.buffer.basic;

import java.io.IOException;

/**
 *
 * @author katelyn
 */
public abstract class AppendablePrimitiveBuffer<T, B> extends PrimitiveArrayBuffer<T, B> {

	public AppendablePrimitiveBuffer(Class<T> typeClass, B buffer) {
		super(typeClass, buffer);
	}
	
	public abstract void write(int pos, CharSequence cs, int offset, int len);
	public abstract void put(int pos, char ch);
	
	public Appendable createAppendable() {
		return new Appendable() {
			int pos = 0;
			public Appendable append(CharSequence cs) throws IOException {
				return append(cs, 0, cs.length());
			}
			public Appendable append(CharSequence cs, int offset, int len) throws IOException {
				write(pos, cs, offset, len);
				pos += len;
				return this;
			}
			public Appendable append(char c) throws IOException {
				put(pos++, c);
				return this;
			}
		};
	}
	
}
