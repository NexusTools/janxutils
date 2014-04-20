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
public class ShortAdaptor extends PrimitiveAdaptor<Short> {

	@Override
	public void write(Short target, DataOutputStream out) throws IOException {
		out.writeShort(target);
	}

	@Override
	public Short createInstance(DataInputStream in) throws IOException {
		return in.readShort();
	}

	@Override
	public Class<?> getType() {
		return Short.class;
	}

}
