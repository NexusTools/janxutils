/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nexustools.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.Semaphore;
import net.nexustools.runtime.RunQueue;
import net.nexustools.runtime.ThreadedRunQueue;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Processor;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class SocketStreamServer {
	
	
	private static final Semaphore selectorLock = new Semaphore(1);
	private static final Selector nonBlockingSelector;
	static {
		Selector selector = null;
		Thread selectorThread = null;
		try {
			selector = Selector.open();
			selectorThread = new Thread("NonBlockingServerSelector") {
				{
					setDaemon(true);
				}
				RunQueue queue = new ThreadedRunQueue("NonBlockingServer");
				@Override
				public void run() {
					while(true) {
						Set<SelectionKey> keys;
						selectorLock.acquireUninterruptibly();
						try {
							nonBlockingSelector.select();
							keys = nonBlockingSelector.selectedKeys();
						} catch (IOException ex) {
							throw NXUtils.wrapRuntime(ex);
						} finally {
							selectorLock.release();
						}
						
						if(keys.size() > 0)
							for(SelectionKey key : keys)
								queue.push((Runnable)key.attachment());
						else
							try {
								Thread.sleep(100);
							} catch (InterruptedException ex) {}
					}
				}
			};
		} catch (IOException ex) {
			Logger.warn("Failed to open Selector, readNonBlocking will be unavailable.", ex);
		}
		
		nonBlockingSelector = selector;
		if(selectorThread != null)
			selectorThread.start();
	}
	
	
	public static Closeable bind(SocketAddress address, final SocketStream.Type type, final Processor<Stream> processor) throws IOException {
		switch(type) {
			case TCP:
				final ServerSocketChannel server = ServerSocketChannel.open();
				server.configureBlocking(false);
				server.bind(address);
				
				while(!selectorLock.tryAcquire())
					nonBlockingSelector.wakeup();
				try {
					server.register(nonBlockingSelector, SelectionKey.OP_ACCEPT).attach(new Runnable() {
						public void run() {
							try {
								SocketChannel channel = server.accept();
								InetSocketAddress inetAddress = (InetSocketAddress)channel.getRemoteAddress();
								SocketStream child = new SocketStream(channel, new URI("tcp", null, inetAddress.getAddress().getHostAddress(), inetAddress.getPort(), null, null, null), type);
								Logger.debug("Client connected", child);
								processor.process(child);
							} catch(Throwable t) {
								Logger.exception(t);
								try {
									server.close();
								} catch (IOException ex) {}
							}
						}
					});
				} finally {
					selectorLock.release();
				}
				return server;
		}
		throw new UnsupportedOperationException();
	}
	
}
