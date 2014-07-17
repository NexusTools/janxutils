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

package nexustools.data.primitives;

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
	public Integer createInstance(DataInputStream in, Class<? extends Integer> target) throws IOException {
		return in.readInt();
	}

	@Override
	public Class<? extends Integer> getType() {
		return Integer.class;
	}

}
