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
import net.nexustools.tasks.Task;
import net.nexustools.tasks.TaskQueue;
import net.nexustools.tasks.TaskSink;
import net.nexustools.tasks.ThreadedTaskQueue;
import net.nexustools.utils.Handler;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class NBStreamProcessor {
	
	private static final TaskQueue defaultQueue = new ThreadedTaskQueue("NBStreamProcessorQueue");
	
	public static class DontCloseChannel extends RuntimeException {}
	
	private final TaskSink taskSink;
	public NBStreamProcessor() {
		this(defaultQueue);
	}
	public NBStreamProcessor(TaskSink taskSink) {
		this.taskSink = taskSink;
	}
	
	private Runnable wantMoreImpl;
	protected void nextStep() {
		readyForMore();
	}
	
	protected final void readyForMore() {
		wantMoreImpl.run();
	}
	
	public abstract void opened(InputStream in, OutputStream out);
	public abstract void readInput() throws IOException;
	public abstract void closed();

	Task useThread(final InputStream inStream, final OutputStream outStream) {
//		readTask = new Task() {
//			@Override
//			protected void aboutToExecute() {
//			}
//			@Override
//			protected void onFailure(Throwable reason) {
//			}
//			@Override
//			protected void onSuccess() {
//			}
//			@Override
//			protected void execute() {
//			}
//		};
//		
//		new TaskThread("NBStreamReader-" + StringUtils.randomString(8), 0).push(readTask);
//		return readTask;
		throw new UnsupportedOperationException();
	}

	void register(final SelectableChannel channel, final int fairHashCode) throws IOException {
		channel.configureBlocking(false);
		final Runnable readTask = new Runnable() {
			public void run() {
				try {
					readInput();
				} catch (Throwable t) {
					if(t instanceof DontCloseChannel)
						Logger.exception(Logger.Level.Gears, t);
					else {
						try {
							channel.close();
							closed();
						} catch(Throwable tt) {}
						throw NXUtils.wrapRuntime(t);
					}
					return;
				}
				nextStep();
			}
			public int fairHashCode() {
				return fairHashCode;
			}
			@Override
			public String toString() {
				return channel.toString() + "-Reader";
			}
		};
		wantMoreImpl = SelectorDaemon.install(readTask, new Handler<Throwable>() {
			public void handle(Throwable data) {
				if(data instanceof ClosedChannelException)
					closed();
				else
					try {
						channel.close();
						closed();
					} catch(Throwable t) {}
			}
		}, channel, SelectionKey.OP_READ);
		taskSink.push(new Runnable() {
			public int fairHashCode() {
				return fairHashCode;
			}
			public void run() {
				Logger.gears("Streams opened", channel);
				opened(new EfficientInputStream() {
					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						return ((ByteChannel)channel).read(ByteBuffer.wrap(b, off, len));
					}
					@Override
					public void close() throws IOException {
						try {
							channel.close();
							closed();
						} catch(Throwable t) {}
					}
				}, new EfficientOutputStream() {
					@Override
					public void write(byte[] b, int off, int len) throws IOException {
						((ByteChannel)channel).write(ByteBuffer.wrap(b, off, len));
					}
					@Override
					public void close() throws IOException {
						try {
							channel.close();
							closed();
						} catch(Throwable t) {}
					}
				});
				taskSink.push(readTask);
			}
			@Override
			public String toString() {
				return channel.toString() + "-Initializer";
			}
		});
	}
	
}
