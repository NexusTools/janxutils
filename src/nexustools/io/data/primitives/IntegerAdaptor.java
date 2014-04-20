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
public class IntegerAdaptor extends PrimitiveAdaptor<Integer> {

	@Override
	public void write(Integer target, DataOutputStream out) throws IOException {
		out.writeInt(target);
	}

	@Override
	public Integer createInstance(DataInputStream in) throws IOException {
		return in.readInt();
	}

	@Override
	public Class<?> getType() {
		return Integer.class;
	}

}
