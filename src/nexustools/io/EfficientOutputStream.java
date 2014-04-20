/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author katelyn
 */
public abstract class EfficientOutputStream extends OutputStream {

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
