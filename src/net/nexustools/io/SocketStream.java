/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nexustools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class SocketStream extends Stream {
	
	public static enum Type {
		TCP("tcp"),
		UDP("udp"),
		
		/**
		 * Provides a Unix Socket or Named Pipe, requires a LocalSocketAddress.
		 * 
		 * On Unix like operating systems (mac, linux, bsd, solaris),
		 * this would use a unix socket.
		 * On Windows based operating systems, this would create a Named Pipe.
		 * 
		 * For compatibility, if a file path is given, it is passed directly to
		 * unix socket implementations, but transformed slightly for named pipes.
		 * 
		 * Where as if a string is given which does not appear to be a file path,
		 * a directory is created for storing the file relative to the program's configuration
		 * when using a unix socket, but passed directly for named pipes.
		 * 
		 * If you want to be assured no transformation is done to the address,
		 * use the Unix or Named types directly, as this is a proxy for those.
		 */
		Local("local"),
		
		/**
		 * Creates a Unix socket where possible.
		 * 
		 * Not supported on Windows.
		 */
		Unix("unix"),
		
		/**
		 * Creates a Named Pipe where possible.
		 * 
		 * Only supported on Windows.
		 */
		Named("unix");
		
		public final String scheme;
		Type(String scheme) {
			this.scheme = scheme;
		}
	}
	
	public static boolean isSupported(Type type) {
		switch(type) {
			case TCP:
			case UDP:
				return true;
			
			default:
				return false;
		}
	}
	
	static URI uriForAddress(SocketAddress address, Type type) throws UnsupportedOperationException {
		try {
			if(address instanceof InetSocketAddress)
				return new URI("tcp", null, ((InetSocketAddress)address).getAddress().getHostAddress(), ((InetSocketAddress)address).getPort(), null, null, null);
		} catch (URISyntaxException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
		
		throw new UnsupportedOperationException("Cannot create a uri for " + address + '[' + type + ']');
	}
	
	public static SocketStream open(SocketAddress address) throws IOException, UnsupportedOperationException {
		if(address instanceof InetSocketAddress)
			return open(address, Type.TCP);
		else if(address instanceof LocalSocketAddress)
			return open(address, Type.Local);
		
		throw new UnsupportedOperationException("`" + address.getClass().getName() + "` addresses cannot be handled at this time.");
	}
	
	public static SocketStream open(SocketAddress address, Type type) throws IOException {
		switch(type) {
			case TCP:
				return new SocketStream(SocketChannel.open(address), uriForAddress(address, type), type);
			
			default:
				throw new UnsupportedOperationException(type.name() + " sockets cannot be created, this may mean a library is missing or the underlying operating system cannot handle this type of socket.");
		}
		
	}
	public static SocketStream open(String host, int port, Type type) throws IOException {
		return open(new InetSocketAddress(host, port), type);
	}
	public static SocketStream open(String host, int port) throws IOException {
		return open(host, port, Type.TCP);
	}
	public static SocketStream open(String socketPath) throws IOException {
		return open(new LocalSocketAddress(socketPath), Type.Local);
	}
	
	
	private final URI uri;
	private final Type type;
	private final SocketChannel socketChannel;
	SocketStream(SocketChannel channel, Type type) {
		this(channel, uriForAddress(channel.socket().getRemoteSocketAddress(), type), type);
	}
	SocketStream(SocketChannel channel, URI uri, Type type) {
		this.socketChannel = channel;
		this.type = type;
		this.uri = uri;
	}

	@Override
	public boolean canWrite() {
		return !socketChannel.socket().isOutputShutdown();
	}

	@Override
	public boolean canRead() {
		return !socketChannel.socket().isInputShutdown();
	}
	
	public void shutdownInput() {
		if(socketChannel.socket().isOutputShutdown())
			close();
		else
			try {
				socketChannel.socket().shutdownInput();
			} catch (IOException ex) {}
	}
	
	public void shutdownOutput() {
		if(socketChannel.socket().isInputShutdown())
			close();
		else
			try {
				socketChannel.socket().shutdownInput();
			} catch (IOException ex) {}
	}

	@Override
	public long size() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Sockets cannot provide a size, if you wish to have size support than implement a protocol ontop of the SocketStream class which provides a size.");
	}

	@Override
	public InputStream createInputStream(final long pos) throws IOException {
		if(pos > 0)
			throw new UnsupportedOperationException();
		return socketChannel.socket().getInputStream();
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		if(pos > 0)
			throw new UnsupportedOperationException();
		return socketChannel.socket().getOutputStream();
	}

	@Override
	public URI toURI() {
		return uri;
	}

	@Override
	public String scheme() {
		return type.toString();
	}

	@Override
	public String path() {
		return "";
	}

	@Override
	public ByteChannel createChannel(Object... args) throws UnsupportedOperationException, IOException {
		if(args.length > 0)
			throw new UnsupportedOperationException();
		return socketChannel;
	}

	@Override
	public void close() {
		try {
			socketChannel.close();
		} catch (IOException ex) {}
	}
	
}
