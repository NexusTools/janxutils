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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.logging.Level;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.ThreadCondition;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.tasks.RunTask;
import net.nexustools.tasks.Task;
import net.nexustools.tasks.TaskSink;
import net.nexustools.tasks.ThreadedTaskQueue;
import net.nexustools.utils.DaemonThread;
import net.nexustools.utils.Handler;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class SelectorDaemon extends DaemonThread {
	
	/**
	 * Installs a SelectableChannel for events.
	 * 
	 * The returned Runnable is used to notify the daemon we're interested in more events,
	 * after it runs, you will receive any incoming events, it is NOT called automatically.
	 * 
	 * Each received event is processed in its own thread,
	 * as such you will need to call the Runnable again after handling your events,
	 * otherwise you will stop receiving events after handling the first.
	 * 
	 * @param callback The callback used to handle events.
	 * @param channel The channel to watch.
	 * @param op The operation to watch.
	 * @return A Runnable used to notify the SelectorDaemon that we want more events.
	 * @throws UnsupportedOperationException if required features are missing in the JVM
	 * @throws ClosedChannelException if the channel is already closed
	 */
	public static Runnable install(final Runnable callback, final Handler<Throwable> errorCallback, final SelectableChannel channel, final int op) {
		final String type;
		switch(op) {
			case SelectionKey.OP_ACCEPT:
				type = "Accept";
				break;

			case SelectionKey.OP_CONNECT:
				type = "Connect";
				break;

			case SelectionKey.OP_READ:
				type = "Read";
				break;

			case SelectionKey.OP_WRITE:
				type = "Write";
				break;

			default:
				type = "Unknown";
				break;
		}
		
		return new Runnable() {
			Task task = new RunTask(callback);
			Handler<Selector> register = new Handler<Selector>() {
				public void handle(Selector data) {
					Logger.gears(channel, "Registering for more input");
					try {
						channel.register(data, op, task);
					} catch (ClosedChannelException ex) {
						java.util.logging.Logger.getLogger(SelectorDaemon.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				@Override
				public String toString() {
					return channel.toString() + "-" + type;
				}
			};
			public void run() {
				daemon.push(register);
			}
			@Override
			public String toString() {
				return channel.toString() + "-" + type + "Register";
			}
		};
	}

	private static final SelectorDaemon daemon;
	static {
		SelectorDaemon selector;
		try {
			selector = new SelectorDaemon();
		} catch (IOException ex) {
			selector = null;
		}
		daemon = selector;
	}
	
	private final PropList<Handler<Selector>> handlers = new PropList<Handler<Selector>>();
	private final Selector selector;
	private SelectorDaemon() throws IOException {
		super("SelectorDaemon", Thread.NORM_PRIORITY);
		selector = Selector.open();
	}
	
	public void push(Handler<Selector> task) {
		Logger.gears("Pushing Selector Mutation");
		handlers.push(task);
		if(isAlive()) {
			interrupt(); 
			selector.wakeup();
		} else
			start();
	}

	@Override
	public void run() {
		StrongTypeList<Task> callbacks = new StrongTypeList();
		ThreadedTaskQueue queue = new ThreadedTaskQueue("SelectorDaemonQueue");
		while(true) {
			try {
				if(interrupted())
					throw new InterruptedException();

				if(selector.keys().size() > 0) {
					Logger.gears("Waiting for events", selector.keys().size());
					selector.select(5000);
					Set<SelectionKey> selections = selector.selectedKeys();
					Logger.gears("Processing events", selections.size());
					for(SelectionKey key : selections) {
						callbacks.push((Task)key.attach(key));
						key.cancel();
					}
				} else {
					Logger.gears("Waiting for selectables");
					Thread.sleep(5000);
				}
			} catch (InterruptedException ex) {
				Logger.gears("Interrupted");
			} catch (IOException ex) {
				throw NXUtils.wrapRuntime(ex);
			}
			ListAccessor<Handler<Selector>> mutations = handlers.take();
			if(callbacks.length() >  0 || mutations.length() > 0) {
				Logger.gears("Handling callbacks", callbacks);
				for(Task task : callbacks)
					if(!queue.push(task))
						throw new RuntimeException("Input overflow");
				callbacks.clear();
				Logger.gears("Queue has", queue.waitingTasks(), "Waiting Tasks");
				Logger.gears("Handling mutations", mutations);
				for(Handler<Selector> handler : mutations)
					try {
						handler.handle(selector);
					} catch (Throwable ex) {
						throw NXUtils.wrapRuntime(ex);
					}
			}
		}
	}
	
}
