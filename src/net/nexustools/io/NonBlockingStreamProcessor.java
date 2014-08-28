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

package net.nexustools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.Semaphore;
import net.nexustools.runtime.FairTaskDelegator;
import net.nexustools.runtime.RunQueue;
import net.nexustools.runtime.ThreadedRunQueue;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class NonBlockingStreamProcessor<I extends InputStream> {
	
	private static final Semaphore selectorLock = new Semaphore(1);
	private static final Selector nonBlockingSelector;
	static {
		Selector selector = null;
		Thread selectorThread = null;
		try {
			selector = Selector.open();
			selectorThread = new Thread("NonBlockingStreamSelector") {
				{
					setDaemon(true);
				}
				RunQueue queue = new ThreadedRunQueue("NonBlockingStream");
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
							for(SelectionKey key : keys) {
								selectorLock.acquireUninterruptibly();
								try {
									key.cancel();
								} finally {
									selectorLock.release();
								}
								queue.push((Runnable)key.attachment());
							}
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
	
	public static boolean isSupported() {
		return nonBlockingSelector != null;
	}
	
	protected void nextStep() {
		notifyReadyForInput();
	}
	
	protected final void notifyReadyForInput() {
		while(!selectorLock.tryAcquire())
			nonBlockingSelector.wakeup();
		try {
			((SelectableChannel)channel).register(nonBlockingSelector, SelectionKey.OP_READ).attach(readTask);
		} catch(ClosedChannelException ex) {
			try {
				channel.close();
			} catch (IOException ex1) {}
			channel = null;
		} finally {
			selectorLock.release();
		}
	}
	
	public abstract void opened(InputStream in, OutputStream out);
	public abstract void readInput() throws IOException;
	public abstract void closed();

	private Runnable readTask;
	private SelectableChannel channel;
	void register(final SelectableChannel channel, final int fairHashCode) throws IOException {
		this.channel = channel;
		channel.configureBlocking(false);
		readTask = new FairTaskDelegator.FairRunnable() {
			public void run() {
				try {
					readInput();
					nextStep();
				} catch (Throwable t) {
					closed();
					try {
						channel.close();
					} catch(IOException ex) {}
					if(t instanceof IOException)
						Logger.exception(Logger.Level.Gears, t);
					else
						throw NXUtils.wrapRuntime(t);
				}
			}
			public int fairHashCode() {
				return fairHashCode;
			}
		};
		opened(new EfficientInputStream() {
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return ((ByteChannel)channel).read(ByteBuffer.wrap(b, off, len));
			}
			@Override
			public void close() throws IOException {
				channel.close();
			}
		}, new EfficientOutputStream() {
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				((ByteChannel)channel).write(ByteBuffer.wrap(b, off, len));
			}
			@Override
			public void close() throws IOException {
				channel.close();
			}
		});
		notifyReadyForInput();
	}
	
}
