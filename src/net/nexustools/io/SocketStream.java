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
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class SocketStream extends Stream {
	
	public static enum Type {
		TCP("tcp"),
		UDP("udp"),
		
		Local("unix");
		
		private final String scheme;
		Type(String scheme) {
			this.scheme = scheme;
		}
		@Override
		public String toString() {
			return scheme;
		}
	}
	
	public static SocketStream open(SocketAddress address, Type type) throws IOException {
		switch(type) {
			case TCP:
				InetSocketAddress inetAddress = (InetSocketAddress)address;
				try {
					return new SocketStream(SocketChannel.open(address), new URI("tcp", null, inetAddress.getAddress().getHostAddress(), inetAddress.getPort(), null, null, null), type);
				} catch (URISyntaxException ex) {
					throw NXUtils.wrapRuntime(ex);
				}
			
			default:
				throw new IllegalArgumentException("Not supported yet: " + type);
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
	SocketStream(SocketChannel channel, URI uri, Type type) {
		this.socketChannel = channel;
		this.type = type;
		this.uri = uri;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public long size() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
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
	
}
