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
