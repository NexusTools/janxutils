/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nexustools.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import net.nexustools.io.monitor.ServerSocketChannelMonitor;
import net.nexustools.tasks.TaskQueue;
import net.nexustools.tasks.TaskSink;
import net.nexustools.tasks.ThreadedTaskQueue;
import net.nexustools.utils.Handler;

/**
 *
 * @author katelyn
 */
public class SocketStreamServer {
	private static final TaskQueue sharedMonitorQueue = new ThreadedTaskQueue("SocketStreamServerQueue");
	
	final SocketStream.Type type;
	final ServerSocketChannel channel;
	public SocketStreamServer(int port) throws IOException {
		this(new InetSocketAddress(port), SocketStream.Type.TCP);
	}
	public SocketStreamServer(int port, SocketStream.Type type) throws IOException {
		this(new InetSocketAddress(port), type);
	}
	public SocketStreamServer(SocketAddress address, SocketStream.Type type) throws IOException {
		switch(this.type = type) {
			case TCP:
				channel = ServerSocketChannel.open();
				break;
				
			default:
				throw new UnsupportedOperationException();
		}
		
        try {
            channel.socket().bind(address);
		} catch(IllegalArgumentException e) {
			channel.close();
			throw e;
        } catch(SecurityException e) {
            channel.close();
            throw e;
        } catch(IOException e) {
            channel.close();
            throw e;
        }
	}
	
	public ServerSocketChannelMonitor startMonitor(final Handler<Stream> childConnected, final Handler<Throwable> errorOccured) throws IOException {
		return startMonitor(childConnected, errorOccured, sharedMonitorQueue);
	}
	public ServerSocketChannelMonitor startMonitor(final Handler<Stream> childConnected, final Handler<Throwable> errorOccured, TaskSink taskSink) throws IOException {
		ServerSocketChannelMonitor monitor = new ServerSocketChannelMonitor(channel, taskSink) {
			@Override
			public void onConnect(SocketChannel child) {
				childConnected.handle(new SocketStream(child, type));
			}
			@Override
			public void handleError(Throwable t) {
				errorOccured.handle(t);
			}
		};
		monitor.start();
		return monitor;
	}
	
	public SocketStream accept() throws IOException {
		SocketChannel socketChannel = channel.accept();
		if(socketChannel != null)
			return new SocketStream(socketChannel, type);
		return null;
	}
	
	public void close() throws IOException{
		channel.close();
	}
	
}
