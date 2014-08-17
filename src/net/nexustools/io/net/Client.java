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
import java.util.List;
import java.util.logging.Level;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropList;
import net.nexustools.data.AdaptorException;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.Pair;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public abstract class Client<P extends Packet, S extends Server<P, ?>> {
	
	public class SendThread extends Thread {
		public SendThread(String name) {
			super(name);
		}
		@Override
		public void run() {
			try {
				while(isAlive.get()) {
					List<P> packets = packetQueue.take();
					if(packets.size() > 0)
						for(P packet : packets)
							try {
								socket.v.write(packet.data());
							} catch (IOException ex) {
								throw ex;
							} catch (Throwable t) {
								Logger.exception(t);
							}
					else
						try {
							Thread.sleep(2 * 60 * 1000);
						} catch (InterruptedException ex) {}
				}
			} catch(IOException ex) {
			} finally {
				try {
					socket.v.close();
				} catch (IOException ex) {}
			}
		}
	}
	
	final RunQueue runQueue;
	final PacketRegistry packetRegistry;
	final Prop<Boolean> isAlive = new Prop(true);
	final PropList<P> packetQueue = new PropList();
	protected final Pair<DataInputStream,DataOutputStream> socket;
	final Thread receiveThread;
	final SendThread sendThread;
	final Server server;
	protected Client(String name, final Pair<DataInputStream,DataOutputStream> socket, final Server server) {
		sendThread = new SendThread(name + "-send");
		receiveThread = new Thread(name + "-receive") {
			@Override
			public void run() {
				try {
					while(true) {
						final Packet packet = nextPacket();
						assert(packet != null);
						
						Logger.debug("Received Packet", packet);
						runQueue.push(new Runnable() {
							public void run() {
								packet.recvFromClient(Client.this, server);
							}
						});
					}
				} catch (DisconnectedException ex) {
				} catch (IOException ex) {
					Logger.exception(ex);
				} finally {
					isAlive.set(false);
					sendThread.interrupt();
					Logger.debug("Client Disconnected", Client.this);
					try {
						socket.i.close();
					} catch (IOException ex) {}
				}
				
			}
		};
		
		this.packetRegistry = server.packetRegistry;
		this.runQueue = server.runQueue;
		this.socket = socket;
		this.server = server;
		
		receiveThread.start();
		sendThread.start();
	}
	
	public P nextPacket() throws IOException {
		P packet = (P)packetRegistry.create(socket.i.readShort());
		try {
			packet.read(socket.i);
		} catch (UnsupportedOperationException ex) {
			throw new IOException(ex);
		} catch (AdaptorException ex) {
			throw new IOException(ex);
		}
		return packet;
	}
	
	public void send(P packet) {
		sendThread.interrupt();
	}
	
	public void moveTo(Server server) {
		
	}

}
