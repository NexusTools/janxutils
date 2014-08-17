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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.Adaptor;
import net.nexustools.data.AdaptorException;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;
import net.nexustools.io.MemoryStream;

/**
 *
 * @author kate
 */
public abstract class Packet<C, S> {
	
	private final Prop<byte[]> cache = new Prop();
	
	protected abstract void recvFromServer(C client, S server);
	protected abstract void recvFromClient(C client, S server);
	
	public void read(DataInputStream dataInput) throws UnsupportedOperationException, IOException, AdaptorException {
		Adaptor.resolveAndRead(this, dataInput);
	}
	public void write(DataOutputStream dataOutput) throws UnsupportedOperationException, IOException, AdaptorException {
		Adaptor.resolveAndWrite(this, dataOutput);
	}
	public byte[] data() throws UnsupportedOperationException, IOException, AdaptorException {
		return cache.read(new SoftWriteReader<byte[], PropAccessor<byte[]>>() {
			@Override
			public byte[] soft(PropAccessor<byte[]> data) {
				return data.get();
			}
			@Override
			public byte[] read(PropAccessor<byte[]> data) {
				try {
					MemoryStream memoryStream = new MemoryStream();
					write(memoryStream.createDataOutputStream());
					data.set(memoryStream.toByteArray());
					return data.get();
				} catch (UnsupportedOperationException ex) {
					throw new RuntimeException(ex);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				} catch (AdaptorException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}
	
}
