/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import static nexustools.io.data.Adaptor.writeMutable;

/**
 *
 * @author katelyn
 */
public class CollectionAdaptor extends Adaptor<Collection> {

	@Override
	public void write(Collection target, DataOutputStream out) throws IOException {
		try {
			out.writeInt(target.size());
			for(Object obj : target)
				writeMutable(obj, out);
		} catch (UnsupportedOperationException | AdaptorException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void read(Collection target, DataInputStream in) throws IOException {
		target.clear();
		int len = in.readInt();
		while(len > 0) {
			try {
				target.add(readMutable(in));
			} catch (AdaptorException | ClassNotFoundException ex) {
				throw new IOException(ex);
			}
			len --;
		}
	}

	@Override
	public Collection createInstance(DataInputStream in) throws IOException {
		return new ArrayList();
	}

	@Override
	public Class<?> getType() {
		return Collection.class;
	}

}
