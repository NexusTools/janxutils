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
public class DoubleAdaptor extends PrimitiveAdaptor<Double> {

	@Override
	public void write(Double target, DataOutputStream out) throws IOException {
		out.writeDouble(target);
	}

	@Override
	public Double createInstance(DataInputStream in, Class<? extends Double> target) throws IOException {
		return in.readDouble();
	}

	@Override
	public Class<? extends Double> getType() {
		return Double.class;
	}

}
