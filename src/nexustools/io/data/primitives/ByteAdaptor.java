/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data.primitives;

import java.io.IOException;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;

/**
 *
 * @author katelyn
 */
public class ByteAdaptor extends PrimitiveAdaptor<Byte> {

	@Override
	public void write(Byte target, DataOutputStream out) throws IOException {
		out.writeByte(target);
	}

	@Override
	public Byte createInstance(DataInputStream in) throws IOException {
		return in.readByte();
	}

	@Override
	public Class<?> getType() {
		return Byte.class;
	}

}
