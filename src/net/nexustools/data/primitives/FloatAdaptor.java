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

package net.nexustools.data.primitives;

import java.io.IOException;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;

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
	public Float createInstance(DataInputStream in, Class<? extends Float> target) throws IOException {
		return in.readFloat();
	}

	@Override
	public Class<? extends Float> getType() {
		return Float.class;
	}

}
