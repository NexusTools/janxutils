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

package net.nexustools.io.monitor;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import net.nexustools.tasks.SimpleSynchronizedTask;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class ServerSocketChannelMonitor extends TaskChannelMonitor<ServerSocketChannel> {
	
	public ServerSocketChannelMonitor(ServerSocketChannel channel, TaskSink taskSink) {
		super(channel, taskSink, SelectionKey.OP_ACCEPT);
	}

	public abstract void onConnect(SocketChannel child);

	@Override
	public final int onSelect(int readyOps) throws IOException {
		final SocketChannel child = channel.accept();
		if(child != null)
			try {
				taskSink.push(new SimpleSynchronizedTask() {
					@Override
					protected void execute() throws InterruptedException {
						onConnect(child);
					}
				});
			} catch (Throwable t) {
				try {
					child.close();
				} catch(Throwable tt) {}
				throw NXUtils.wrapRuntime(t);
			}
		else if(!channel.isOpen())
			throw new ClosedChannelException();
		else
			Logger.performance("This should never happen", Thread.currentThread().getStackTrace());
		return SelectionKey.OP_ACCEPT;
	}
}
