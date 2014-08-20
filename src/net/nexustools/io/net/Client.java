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
import javax.activation.UnsupportedDataTypeException;
import net.nexustools.concurrent.Condition;
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
	
	private static final ThreadedRunQueue sendQueue = new ThreadedRunQueue("ClientOut");
	private abstract class ReceiveThread extends Thread {
		public ReceiveThread(String name) {
			super(name + "In");
			setDaemon(true);
		}
		public abstract Runnable packetProcessor(P packet);
		public abstract Object eventSource();
		@Override
		public void run() {
			try {
				while(true) {
					final P packet = nextPacket();
					if(packet == null)
						throw new IOException("Unexpected end of stream");

					Logger.gears("Received Packet", packet);
					runQueue.push(packetProcessor(packet));
				}
			} catch (DisconnectedException ex) {
				Logger.exception(Logger.Level.Gears, ex);
			} catch (IOException ex) {
				Logger.exception(ex);
			} finally {
				isAlive.set(false);
				Logger.debug("Client Disconnected", Client.this);
				try {
					socket.i.close();
				} catch (IOException ex) {}

				eventDispatcher.dispatch(new EventDispatcher.Processor<ClientListener, ClientListener.ClientEvent>() {
					public ClientListener.ClientEvent create() {
						return new ClientListener.ClientEvent(eventSource(), Client.this);
					}
					public void dispatch(ClientListener listener, ClientListener.ClientEvent event) {
						listener.clientDisconnected(event);
					}
				});
				shutdown.finish();
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
	final Condition shutdown = new Condition();
	final Prop<Boolean> isAlive = new Prop(true);
	final PropList<P> packetQueue = new PropList();
	protected final Pair<DataInputStream,DataOutputStream> socket;
	final DefaultEventDispatcher<?, ClientListener, ClientListener.ClientEvent> eventDispatcher;
	final DefaultEventDispatcher<?, PacketListener, PacketListener.PacketEvent> packetDispatcher;
	final Runnable processSendQueue = new Runnable() {
		public void run() {
			for(P packet : packetQueue.take())
				try {
					int packetID = packetRegistry.idFor(packet);
					Logger.gears("Writing Packet", packetID, packet);

					byte[] data;
					try {
						data = packet.data(Client.this);
					} catch(Throwable t) {
						Logger.warn("Error generating packet contents");
						Logger.warn("Client may now become unstable");
						Logger.exception(Logger.Level.Warning, t);
						continue;
					}

					Logger.debug(socket.v);
					socket.v.writeShort(packetID);
					socket.v.write(data);
					socket.v.flush();

					Logger.gears("Written and Flushed");
				} catch (IOException ex) {
					Logger.exception(ex);
				} catch (NoSuchMethodException ex) {
					Logger.exception(ex);
				}
		}
	};
	final ReceiveThread receiveThread;
	public Client(String name, Pair<DataInputStream,DataOutputStream> socket, final Server server) {
		receiveThread = new ReceiveThread(name) {
			@Override
			public Runnable packetProcessor(final P packet) {
				return new Runnable() {
					public void run() {
						packet.recvFromClient(Client.this, server);
					}
				};
			}
			@Override
			public Object eventSource() {
				return server;
			}
		};
		
		eventDispatcher = new DefaultEventDispatcher(server.runQueue);
		packetDispatcher = new DefaultEventDispatcher(server.runQueue);
		this.packetRegistry = server.packetRegistry;
		this.runQueue = server.runQueue;
		this.socket = socket;
		
		receiveThread.start();
	}
	public Client(String name, final Pair<DataInputStream,DataOutputStream> socket, RunQueue runQueue, PacketRegistry packetRegistry) {
		receiveThread = new ReceiveThread(name) {
			@Override
			public Runnable packetProcessor(final P packet) {
				return new Runnable() {
					public void run() {
						packet.recvFromServer(Client.this);
					}
				};
			}
			@Override
			public Object eventSource() {
				return Client.this;
			}
		};
		
		this.runQueue = runQueue;
		eventDispatcher = new DefaultEventDispatcher(runQueue);
		packetDispatcher = new DefaultEventDispatcher(runQueue);
		this.packetRegistry = packetRegistry;
		this.socket = socket;
		
		receiveThread.start();
	}
	public Client(String name, String host, int port, Protocol protocol, RunQueue runQueue, PacketRegistry packetRegistry) throws IOException {
		this(name, open(host, port, protocol), runQueue, packetRegistry);
	}
	public Client(String name, String host, int port, Protocol protocol, PacketRegistry packetRegistry) throws IOException {
		this(name, host, port, protocol, new ThreadedRunQueue(name), packetRegistry);
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
				sendQueue.push(processSendQueue, RunQueue.QueuePlacement.ReplaceExisting);
			}
		});
	}
	
	public void moveTo(Server server) {
		
	}

}
