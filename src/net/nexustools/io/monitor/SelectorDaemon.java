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
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.CancellationException;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.utils.DaemonThread;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class SelectorDaemon extends DaemonThread {
	
	private static Selector selector;
	private static final Object selectorLock = new Object();
	public static void installMonitor(final ChannelMonitor monitor) throws IOException {
		synchronized(selectorLock) {
			if(selector == null) {
				selector = Selector.open();
				new SelectorDaemon().start();
			}
			monitor.key = monitor.channel.register(selector, monitor.interests(), monitor);
		}
	}
	
	private SelectorDaemon() throws IOException {
		super("SelectorDaemon", Thread.NORM_PRIORITY);
		Logger.debug("Starting SelectorDaemon");
	}

	@Override
	public void run() {
		StrongTypeList<ChannelMonitor> selectedMonitors = new StrongTypeList();
		while(true) {
			synchronized(selectorLock) {
				if(selector.keys().size() < 1) {
					selector = null; // TODO: Add timeout
					return;
				}
			}

			try {
				selector.select(5000);
				for(SelectionKey key : selector.selectedKeys())
					selectedMonitors.push((ChannelMonitor)key.attachment());
			} catch (IOException ex) {
				throw NXUtils.wrapRuntime(ex);
			}

			if(selectedMonitors.isTrue()) {
				for(ChannelMonitor monitor : selectedMonitors)
					try {
						Logger.gears("Monitor Selected", monitor);
						synchronized(monitor.interestsLock) {
							int interests = monitor.onSelect(monitor.key);
							if(monitor.key.interestOps() != interests) {
								Logger.gears("Updating interests", interests);
								monitor.key.interestOps(interests);
							}
						}
					} catch(Throwable t) {
						monitor.handleError(t);
					}
				selectedMonitors.clear();
			} else
				try {
					Thread.sleep(50);
				} catch (InterruptedException ex) {}
		}
	}
	
}
