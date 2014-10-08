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
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.CancellationException;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.SoftUpdateWriter;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.DaemonThread;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class SelectorDaemon extends DaemonThread {
	
	private static final Prop<Selector> selector = new Prop();
	public static void installMonitor(final ChannelMonitor monitor) throws IOException {
		selector.write(new SoftUpdateWriter<PropAccessor<Selector>>() {
			@Override
			public void write(PropAccessor<Selector> data) {
				try {
					data.set(Selector.open());
					new SelectorDaemon().start();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
			@Override
			public void update(PropAccessor<Selector> data) {
				try {
					data.get().wakeup();
					monitor.key = monitor.channel.register(data.get(), monitor.interests(), monitor);
				} catch (ClosedChannelException ex) {
					throw new RuntimeException(ex);
				}
				Logger.debug("Registered Monitor", monitor, monitor.key);
			}
			public boolean test(PropAccessor<Selector> against) {
				return !against.isTrue();
			}
		});
	}
	
	private SelectorDaemon() throws IOException {
		super("SelectorDaemon", Thread.NORM_PRIORITY);
		Logger.debug("Starting SelectorDaemon");
	}

	@Override
	public void run() {
		while(true) {
			Selector current;
			try {
				current = selector.read(new SoftWriteReader<Selector, PropAccessor<Selector>>() {
					@Override
					public boolean test(PropAccessor<Selector> against) {
						return against.get().keys().size() < 1;
					}
					@Override
					public Selector soft(PropAccessor<Selector> data) {
						return data.get();
					}
					@Override
					public Selector read(PropAccessor<Selector> data) {
						data.clear();
						throw new CancellationException();
					}
				});
			} catch(CancellationException ex) {
				return;
			}

			Set<SelectionKey> selectedKeys = null;
			try {
				current.select(150);
				selectedKeys = current.selectedKeys();
			} catch (IOException ex) {
				throw new RuntimeException("Error occured in SelectorDaemon", ex);
			}
			
			for(SelectionKey key : selectedKeys) {
				ChannelMonitor monitor = (ChannelMonitor)key.attachment();
				try {
					Logger.debug("Monitor Selected", monitor, monitor.key.readyOps());
					monitor.doSelect();
				} catch(Throwable t) {
					Logger.exception(t);
				}
			}
			selectedKeys.clear();
		}
	}
	
}
