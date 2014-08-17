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
import java.net.ServerSocket;
import java.net.Socket;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;
import net.nexustools.utils.Pair;

/**
 *
 * @author kate
 */
public class TCPStreamServer implements StreamServer {

	private final int port;
	private final ServerSocket serverSocket;
	public TCPStreamServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		this.port = port;
	}
	
	public Pair<DataInputStream,DataOutputStream> nextPendingStream() throws IOException {
		Socket socket = serverSocket.accept();
		return new Pair(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
	}
	
	@Override
	public String toString() {
		return "Server[TCP-" + port + "]";
	}
	
}
