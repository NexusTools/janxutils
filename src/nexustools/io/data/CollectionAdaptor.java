/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import static nexustools.io.data.Adaptor.resolveAndWriteMutable;

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
				resolveAndWriteMutable(obj, out);
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
				target.add(resolveAndReadMutable(in));
			} catch (AdaptorException | ClassNotFoundException ex) {
				throw new IOException(ex);
			}
			len --;
		}
	}

	@Override
	public Class<? extends Collection> getType() {
		return Collection.class;
	}

}
