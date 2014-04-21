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
public class StringAdaptor extends PrimitiveAdaptor<String> {

	@Override
	public void write(String target, DataOutputStream out) throws IOException {
		out.writeUTF8(target);
	}

	@Override
	public String createInstance(DataInputStream in, Class<? extends String> target) throws IOException {
		return in.readUTF8();
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

}
