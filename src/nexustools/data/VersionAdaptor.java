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
import nexustools.utils.Version;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import nexustools.data.primitives.PrimitiveAdaptor;

/**
 *
 * @author katelyn
 */
public class VersionAdaptor extends PrimitiveAdaptor<Version> {

	@Override
	public Class<? extends Version> getType() {
		return Version.class;
	}

	@Override
	public void write(Version target, DataOutputStream out) throws IOException {
		out.writeByte(target.major);
		out.writeByte(target.minor);
		out.writeByte(target.stage.ordinal());
		out.writeShort(target.revision);
	}

	@Override
	public Version createInstance(DataInputStream in, Class<? extends Version> target) throws IOException {
		return new Version(in.readByte(), in.readByte(), Version.Stage.forOrdinal(in.readByte()), in.readShort());
	}
	
}
