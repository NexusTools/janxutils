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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.tasks.SimpleSynchronizedTask;
import net.nexustools.tasks.Task;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public abstract class SocketChannelMonitor extends TaskChannelMonitor<SocketChannel> {

	protected final Task onConnect;
	protected final Task onRead;
	protected final Task onWrite;

	public SocketChannelMonitor(final SocketChannel channel, TaskSink taskSink) {
		super(channel, taskSink, SelectionKey.OP_CONNECT);
		onConnect = create(new Runnable() {
			public void run() {
				if(channel.isConnectionPending())
					try {
						channel.finishConnect();
					} catch (IOException ex) {
						NXUtils.passException(ex);
					}
				onConnect();
				try {
					registerInterests(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				} catch (IOException ex) {
					NXUtils.passException(ex);
				}
			}
		});
		onRead = create(new Runnable() {
			public void run() {
				if (onRead())
					try {
						registerInterests(SelectionKey.OP_READ);
					} catch (IOException ex) {
						NXUtils.passException(ex);
					}
			}
		});
		onWrite = create(new Runnable() {
			public void run() {
				onWrite();
			}
		});
	}

	protected abstract void onConnect();
	protected abstract boolean onRead();
	protected abstract void onWrite();

	@Override
	public final int onSelect(SelectionKey key) throws IOException {
		int interests = interests();
		if ((key.readyOps()& SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
			interests ^= SelectionKey.OP_CONNECT;
			push(onConnect);
		}
		if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
			push(onWrite);
		if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
			interests ^= SelectionKey.OP_WRITE;
			push(onRead);
		}
		return interests;
	}
}
