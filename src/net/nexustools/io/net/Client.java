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

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import javax.activation.UnsupportedDataTypeException;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.event.DefaultEventDispatcher;
import net.nexustools.event.EventDispatcher;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;
import net.nexustools.io.net.Server.Protocol;
import net.nexustools.runtime.RunQueue;
import net.nexustools.runtime.ThreadedRunQueue;
import net.nexustools.utils.Pair;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public class Client<P extends Packet, S extends Server<P, ?>> {
	
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
								int packetID = packetRegistry.idFor(packet);
								Logger.gears("Writing Packet", packetID, packet);
								
								byte[] data;
								try {
									data = packet.data(Client.this);
								} catch(IOException ex) {
									Logger.exception(ex);
									continue;
								}
								
								Logger.debug(socket.v);
								socket.v.writeShort(packetID);
								socket.v.write(data);
								socket.v.flush();
								
								Logger.gears("Written and Flushed");
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
				Logger.exception(Logger.Level.Gears, ex);
			} finally {
				try {
					socket.v.close();
				} catch (IOException ex) {}
			}
		}
	}
	
	public static Pair<DataInputStream,DataOutputStream> open(String host, int port, Protocol protocol) throws IOException {
		switch(protocol) {
			case TCP:
				Logger.gears("Opening TCP Socket", host, port);
				
				Socket socket = new Socket(host, port);
				Pair pair = new Pair(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
				Logger.gears("Opened", socket, pair);
				return pair;
		}
		
		throw new UnsupportedDataTypeException();
	}
	
	protected final RunQueue runQueue;
	final PacketRegistry packetRegistry;
	final Prop<Boolean> isAlive = new Prop(true);
	final PropList<P> packetQueue = new PropList();
	protected final Pair<DataInputStream,DataOutputStream> socket;
	final DefaultEventDispatcher<?, ClientListener, ClientListener.ClientEvent> eventDispatcher;
	final DefaultEventDispatcher<?, PacketListener, PacketListener.PacketEvent> packetDispatcher;
	final Thread receiveThread;
	final SendThread sendThread;
	public Client(String name, final Pair<DataInputStream,DataOutputStream> socket, final Server server) {
		sendThread = new SendThread(name + "-send");
		receiveThread = new Thread(name + "-receive") {
			@Override
			public void run() {
				try {
					while(true) {
						final Packet packet = nextPacket();
						if(packet == null)
							throw new IOException("Unexpected end of stream");
						
						Logger.gears("Received Packet", packet);
						runQueue.push(new Runnable() {
							public void run() {
								packet.recvFromClient(Client.this, server);
							}
						});
					}
				} catch (DisconnectedException ex) {
					Logger.exception(Logger.Level.Gears, ex);
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
				eventDispatcher.dispatch(new EventDispatcher.Processor<ClientListener, ClientListener.ClientEvent>() {
					public ClientListener.ClientEvent create() {
						return new ClientListener.ClientEvent(server, Client.this);
					}
					public void dispatch(ClientListener listener, ClientListener.ClientEvent event) {
						listener.clientDisconnected(event);
					}
				});
				
			}
		};
		
		eventDispatcher = new DefaultEventDispatcher(server.runQueue);
		packetDispatcher = new DefaultEventDispatcher(server.runQueue);
		this.packetRegistry = server.packetRegistry;
		this.runQueue = server.runQueue;
		this.socket = socket;
		
		receiveThread.start();
		sendThread.start();
	}
	public Client(String name, final Pair<DataInputStream,DataOutputStream> socket, PacketRegistry packetRegistry) {
		sendThread = new SendThread(name + "-send");
		receiveThread = new Thread(name + "-receive") {
			@Override
			public void run() {
				try {
					while(true) {
						final Packet packet = nextPacket();
						if(packet == null)
							throw new IOException("Unexpected end of stream");
						
						Logger.gears("Received Packet", packet);
						runQueue.push(new Runnable() {
							public void run() {
								packet.recvFromServer(Client.this);
							}
						});
					}
				} catch (DisconnectedException ex) {
					Logger.exception(Logger.Level.Gears, ex);
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
				eventDispatcher.dispatch(new EventDispatcher.Processor<ClientListener, ClientListener.ClientEvent>() {
					public ClientListener.ClientEvent create() {
						return new ClientListener.ClientEvent(Client.this, Client.this);
					}
					public void dispatch(ClientListener listener, ClientListener.ClientEvent event) {
						listener.clientDisconnected(event);
					}
				});
				
			}
		};
		
		runQueue = new ThreadedRunQueue(name + "-RunQueue");
		eventDispatcher = new DefaultEventDispatcher(runQueue);
		packetDispatcher = new DefaultEventDispatcher(runQueue);
		this.packetRegistry = packetRegistry;
		this.socket = socket;
		
		receiveThread.start();
		sendThread.start();
	}
	public Client(String name, String host, int port, Protocol protocol, PacketRegistry packetRegistry) throws IOException {
		this(name, open(host, port, protocol), packetRegistry);
	}
	
	public final void addClientListener(ClientListener listener) {
		eventDispatcher.add(listener);
	}
	
	public final void removeClientListener(ClientListener listener) {
		eventDispatcher.remove(listener);
	}
	
	public final void addPacketListener(PacketListener listener) {
		packetDispatcher.add(listener);
	}
	
	public final void removePacketListener(PacketListener listener) {
		packetDispatcher.remove(listener);
	}
	
	public P nextPacket() throws IOException {
		Logger.gears("Waiting for packet", this);
		short packetID;
		try {
			packetID = socket.i.readShort();
		} catch(EOFException eof) {
			throw new DisconnectedException(eof);
		}
		Logger.gears("Reading packet", packetID);
		
		P packet = (P)packetRegistry.create(packetID);
		try {
			packet.read(socket.i, this);
			Logger.gears("Readed packet", packet);
		} catch (UnsupportedOperationException ex) {
			throw new IOException(ex);
		}
		return packet;
	}
	
	public void send(final P packet) {
		packetQueue.write(new Writer<ListAccessor<P>>() {
			@Override
			public void write(ListAccessor<P> data) {
				data.push(packet);
				sendThread.interrupt();
			}
		});
	}
	
	public void moveTo(Server server) {
		
	}

}
