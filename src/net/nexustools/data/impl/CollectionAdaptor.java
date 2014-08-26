/*
 * janxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * janxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with janxutils.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.nexustools.data.impl;

import java.io.IOException;
import java.util.Collection;
import net.nexustools.data.adaptor.Adaptor;
import net.nexustools.data.adaptor.AdaptorException;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;
import static net.nexustools.data.adaptor.Adaptor.resolveAndWriteMutable;

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
		} catch (UnsupportedOperationException ex) {
			throw new IOException(ex);
		} catch (AdaptorException ex) {
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
			} catch(ClassNotFoundException ex) {
				throw new IOException(ex);
			} catch (AdaptorException ex) {
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
