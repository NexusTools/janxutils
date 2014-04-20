/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data;

import java.io.IOException;
import nexustools.Version;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import nexustools.io.data.primitives.PrimitiveAdaptor;

/**
 *
 * @author katelyn
 */
public class VersionAdaptor extends PrimitiveAdaptor<Version> {

	@Override
	public Class<?> getType() {
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
	public Version createInstance(DataInputStream in) throws IOException {
		return new Version(in.readByte(), in.readByte(), Version.Stage.forOrdinal(in.readByte()), in.readShort());
	}
	
}
