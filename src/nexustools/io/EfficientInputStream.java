/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
