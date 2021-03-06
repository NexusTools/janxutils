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

package net.nexustools.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author katelyn
 */
public abstract class EfficientOutputStream extends OutputStream {
	
	public static final EfficientOutputStream Void = new EfficientOutputStream() {
		@Override
		public void write(byte[] b, int off, int len) throws IOException {}
	};

	@Override
	public void write(int b) throws IOException {
		write(new byte[]{(byte)b});
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	@Override
	public abstract void write(byte[] b, int off, int len) throws IOException;
	
}
