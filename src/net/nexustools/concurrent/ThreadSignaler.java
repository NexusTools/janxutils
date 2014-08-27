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

package net.nexustools.concurrent;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.logic.IfReader;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class ThreadSignaler {
	
	private final PropList<Thread> waitingThreads = new PropList<Thread>();
	
	public void await() throws InterruptedException {
		await(Long.MAX_VALUE);
	}
	
	public void await(long length) throws InterruptedException {
		Thread current = Thread.currentThread();
		waitingThreads.push(current);
		try {
			current.sleep(length);
		} finally {
			waitingThreads.remove(current);
		}
	}
	
	public void signalAll() {
		try {
			waitingThreads.read(new IfReader<Void, ListAccessor<Thread>>() {
				@Override
				public Void read(ListAccessor<Thread> data) {
					for(Thread thread : data)
						thread.interrupt();
					return null;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public void signal() {
		try {
			waitingThreads.read(new IfReader<Void, ListAccessor<Thread>>() {
				@Override
				public Void read(ListAccessor<Thread> data) {
					data.first().interrupt();
					return null;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
}
