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
public class LongAdaptor extends PrimitiveAdaptor<Long> {

	@Override
	public void write(Long target, DataOutputStream out) throws IOException {
		out.writeLong(target);
	}

	@Override
	public Long createInstance(DataInputStream in, Class<? extends Long> target) throws IOException {
		return in.readLong();
	}

	@Override
	public Class<? extends Long> getType() {
		return Long.class;
	}

}
