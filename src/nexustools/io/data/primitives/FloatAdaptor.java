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
public class FloatAdaptor extends PrimitiveAdaptor<Float> {

	@Override
	public void write(Float target, DataOutputStream out) throws IOException {
		out.writeFloat(target);
	}

	@Override
	public Float createInstance(DataInputStream in) throws IOException {
		return in.readFloat();
	}

	@Override
	public Class<?> getType() {
		return Float.class;
	}

}
