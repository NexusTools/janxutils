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

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author katelyn
 */
public abstract class EfficientInputStream extends InputStream {

	@Override
	public final int read() throws IOException {
		byte[] arr = new byte[1];
		if(read(arr) == 1)
			return arr[0] & 0xFF;

		return -1;
	}
			
	@Override
	public final int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public abstract int read(byte[] b, int off, int len) throws IOException;
	
}
