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
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class ChannelMonitor<C extends SelectableChannel> {
	protected C channel;
	private final Prop<Integer> interests;
	SelectionKey key;

	public ChannelMonitor(C channel, int interests) {
		this.channel = channel;
		this.interests = new Prop(interests);
	}
	
	public final void start() throws IOException {
		Logger.debug("Installing Monitor", this);
		
		channel.configureBlocking(false);
		SelectorDaemon.installMonitor(this);
	}

	void doSelect() {
		interests.write(new Writer<PropAccessor<Integer>>() {
			@Override
			public void write(PropAccessor<Integer> data) {
				try {
					data.set(onSelect(key.readyOps()));
				} catch (Throwable t) {
					handleError(t);
				}
				try {
					if(key.interestOps() != data.get())
						key.interestOps(data.get());
				} catch(CancelledKeyException ex) {} // Removed
			}
		});
	}

	/**
	 * Any errors that occur while processing this Monitor are handled here.
	 * 
	 * ClosedChannelException can be handled here which indicates the channel has closed.
	 * IOExceptions during read() and accept() are also handled here.
	 * 
	 * @param t 
	 */
	public abstract void handleError(Throwable t);
	public abstract int onSelect(int readyOps) throws IOException;

	/**
	* Requests new interests for this Monitor.
	*
	* <p> This method may be invoked at any time.  Whether or not it blocks,
	* and for how long, is implementation-dependent.  </p>
	*
	* @param  ops  The interests to add
	*
	* @throws  IllegalArgumentException
	*          If a bit in the set does not correspond to an operation that
	*          is supported by this Monitor's channel, that is, if
	*          <tt>(ops & ~channel.validOps()) != 0</tt>
	*
	* @throws  CancelledKeyException
	*          If this key has been cancelled
	*/
	public void registerInterests(final int op) throws IOException {
		interests.write(new IfWriter<PropAccessor<Integer>>() {
			@Override
			public boolean test(PropAccessor<Integer> against) {
				return (against.get() & op) != op;
			}

			@Override
			public void write(PropAccessor<Integer> data) {
				data.set(data.get() | op);
				try {
					key.interestOps(data.get());
				} catch(CancelledKeyException ex) {
					key = null;
					try {
						start();
					} catch (IOException iex) {
						handleError(iex);
					}
				}
			}
		});
	}

	/**
	 * Returns the operations this Monitor is interested in.
	 * 
	 * @return 
	 */
	public int interests() {
		return interests.get();
	}
}
