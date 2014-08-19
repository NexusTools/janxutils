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
import net.nexustools.DefaultAppDelegate;
import net.nexustools.concurrent.Prop;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;
import net.nexustools.io.net.Server.Protocol;
import net.nexustools.utils.Pair;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public abstract class ServerAppDelegate<C extends Client, S extends Server> extends DefaultAppDelegate {

	private Prop<Runnable> mainLoop = new Prop();
	protected final PacketRegistry packetRegistry;
	public ServerAppDelegate(String[] args, String name, String organization, PacketRegistry packetRegistry) {
		super(args, name, organization);
		this.packetRegistry = packetRegistry;
	}
	public ServerAppDelegate(String[] args, String name, String organization) {
		this(args, name, organization, new PacketRegistry());
	}
	
	protected abstract void populate(PacketRegistry registry) throws NoSuchMethodException;
	
	protected C createClient(String host, int port) throws IOException {
		return (C)new Client(name + "-Client", host, port, Protocol.TCP, packetRegistry);
	}
    protected C createClient(Pair<DataInputStream,DataOutputStream> socket, S server) throws IOException {
        return (C)new Client(name + "-Client", socket, server);
    }
	protected S createServer(int port) throws IOException {
		return (S)new Server(port, Protocol.TCP, packetRegistry);
	}
	
	protected abstract void launchClient(C client);
	protected void launchServer(S server) {}

	protected void launch(String[] args) {
		Logger.quote("Poluating Packet Registry", this);
		
		try {
			populate(packetRegistry);
			if(args.length == 2) {
				Logger.gears("Creating Client", args);
				final C client = createClient(args[0], Integer.valueOf(args[1]));
				Logger.gears("Installing Client MainLoop", args);
				mainLoop.set(new Runnable() {
					boolean finished = false;
					
					public void run() {
						ClientListener clientListener = new ClientListener() {
							final Thread myself = Thread.currentThread();
							public void clientConnected(ClientListener.ClientEvent clientConnectedEvent) {}
							public void clientDisconnected(ClientListener.ClientEvent clientConnectedEvent) {
								finished = true;
								myself.interrupt();
							}
						};
						
						Logger.gears("Waiting for Client to Disconnect", client);
						while(!finished)
							try {
								Thread.sleep(Long.MAX_VALUE);
							} catch (InterruptedException ex) {}
					}
				});
				Logger.gears("Launching Client", args);
				launchClient(client);
			}else if(args.length == 1) {
				Logger.gears("Creating Server", args);
				final S server = createServer(Integer.valueOf(args[0]));
				Logger.gears("Installing Server MainLoop", args);
				mainLoop.set(new Runnable() {
					public void run() {
						while(server.isAlive())
							try {
								server.join();
							} catch (InterruptedException ex) {}
					}
				});
				Logger.gears("Launching Server", args);
				launchServer(server);
			} else
				throw new UnsupportedOperationException("Required 1 or 2 arguments, (HOST PORT) or (PORT)");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void mainLoop() {
		Thread myself = Thread.currentThread();
		String oldName = myself.getName();
		myself.setName(getClass().getSimpleName() + "-MainLoop");
		
		Logger.gears("Waiting for MainLoop");
		while(!mainLoop.isset())
			try {
				Thread.sleep(150);
			} catch (InterruptedException ex) {}
		
		Logger.gears("Entering ServerAppDelegate MainLoop");
		mainLoop.get().run();
		Logger.gears("Exiting ServerAppDelegate MainLoop");
		myself.setName(oldName);
	}

	public boolean needsMainLoop() {
		return false;
	}

	public String pathUri(Path path) {
		return null;
	}
	
}
