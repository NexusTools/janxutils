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
				Logger.gears("Starting Client", args);
				launchClient(createClient(args[0], Integer.valueOf(args[1])));
			}else if(args.length == 1) {
				Logger.gears("Starting Server", args);
				launchServer(createServer(Integer.valueOf(args[0])));
			} else
				throw new UnsupportedOperationException("Required 1 or 2 arguments, (HOST PORT) or (PORT)");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void mainLoop(String[] args) {}

	public boolean needsMainLoop() {
		return false;
	}

	public String pathUri(Path path) {
		return null;
	}
	
}
