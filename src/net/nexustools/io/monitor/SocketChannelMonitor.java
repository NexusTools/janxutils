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
import net.nexustools.concurrent.Condition;
import net.nexustools.concurrent.ThreadCondition;
import net.nexustools.tasks.Task;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public abstract class SocketChannelMonitor extends TaskChannelMonitor<SocketChannel> {

	private final Condition connected;
	protected final Task onConnect;
	protected final Task onRead;

	public SocketChannelMonitor(final SocketChannel channel, TaskSink taskSink) {
		super(channel, taskSink, channel.isConnected() ? SelectionKey.OP_READ | SelectionKey.OP_WRITE : SelectionKey.OP_CONNECT);
		if(channel.isConnected()) {
			onConnect = wrap(new Runnable() {
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
			// TODO: Replace with a fake condition that always returns true
			connected = new ThreadCondition(true);
		} else {
			connected = new ThreadCondition();
			push(new Runnable() {
				public void run() {
					onConnect();
					((ThreadCondition)connected).finish();
				}
			});
			onConnect = null;
		}
		onRead = wrap(new Runnable() {
			public void run() {
				if (onRead())
					try {
						registerInterests(SelectionKey.OP_READ);
					} catch (IOException ex) {
						NXUtils.passException(ex);
					}
			}
		});
	}

	protected abstract void onConnect();
	protected abstract boolean onRead();
	protected abstract void onWrite();
	
	public final void registerWrite() throws IOException {
		registerInterests(SelectionKey.OP_WRITE);
	}

	@Override
	public final int onSelect(int readyOps) throws IOException {
		int interests = interests();
		if ((readyOps& SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
			interests ^= SelectionKey.OP_CONNECT;
			push(onConnect);
		} else {
			connected.waitForUninterruptibly();
			if ((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
				try {
					onWrite();
					interests ^= SelectionKey.OP_WRITE;
				} catch(Throwable t) {
					handleError(t);
				}
			if ((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
				interests ^= SelectionKey.OP_READ;
				push(onRead);
			}
		}
		return interests;
	}
}
