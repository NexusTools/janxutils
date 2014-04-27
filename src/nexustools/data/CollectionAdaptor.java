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

package nexustools.data;

import java.io.IOException;
import java.util.Collection;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import static nexustools.data.Adaptor.resolveAndWriteMutable;

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
