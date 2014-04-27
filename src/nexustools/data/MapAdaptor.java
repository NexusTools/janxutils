/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nexustools.data;

import java.io.IOException;
import java.util.Map;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;

/**
 *
 * @author katelyn
 */
public class MapAdaptor extends Adaptor<Map> {

	@Override
	public void write(Map target, DataOutputStream out) throws IOException {
		out.writeInt(target.size());
		for (Object object : target.entrySet()) {
			if(object instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry)object;
				out.writeMutableObject(entry.getKey());
				out.writeMutableObject(entry.getValue());
			} else
				throw new UnsupportedOperationException("Cannot handle objects not of type Map.Entry");
		}
	}

	@Override
	public void read(Map target, DataInputStream in) throws IOException {
		int size = in.readInt();
		while(size-- > 0)
			target.put(in.readMutableObject(), in.readMutableObject());
	}

	@Override
	public Class<? extends Map> getType() {
		return Map.class;
	}

}
