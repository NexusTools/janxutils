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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import net.nexustools.io.monitor.SocketChannelMonitor;
import net.nexustools.tasks.TaskSink;
import net.nexustools.tasks.ThreadedTaskQueue;

/**
 *
 * @author katelyn
 */
public abstract class NBStreamProcessor {
	
	private static final TaskSink defaultTaskSink = new ThreadedTaskQueue("NBStreamProcessorQueue");
	public static class DontCloseChannel extends RuntimeException {}
	
	private final TaskSink taskSink;
	public NBStreamProcessor() {
		this(defaultTaskSink);
	}
	public NBStreamProcessor(TaskSink taskSink) {
		this.taskSink = taskSink;
	}

	void register(SelectableChannel selectableChannel) throws IOException {
		SocketChannelMonitor monitor = new SocketChannelMonitor((SocketChannel)selectableChannel, taskSink) {
			@Override
			protected void onConnect() {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
			@Override
			protected boolean onRead() {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
			@Override
			protected void onWrite() {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
			@Override
			public void handleError(Throwable t) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		};
		monitor.start();
	}
	
	public abstract void opened(InputStream in, OutputStream out);
	public abstract void readInput() throws IOException;
	public abstract void closed();

}
