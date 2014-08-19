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

package net.nexustools.io.net;

import java.io.IOException;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;

/**
 *
 * @author kate
 */
public abstract class SimplePacket<C extends Client, S extends Server> extends Packet<C, S> {
	
    @Override
    public final void read(DataInputStream dataInput, C client) throws UnsupportedOperationException, IOException {}

    @Override
    public final void write(DataOutputStream dataOutput, C client) throws UnsupportedOperationException, IOException {}
    
	
}
