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
