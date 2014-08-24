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

package net.nexustools.runtime;

import net.nexustools.data.accessor.BaseAccessor;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.ReadWriteLock;

/**
 *
 * @author katelyn
 * @param <R>
 * @param <F>
 */
public class DefaultRunQueue<R extends Runnable> extends ThreadedRunQueue<R> {

	private static DefaultRunQueue instance = new DefaultRunQueue("DefaultRunQueue");
	public static DefaultRunQueue instance() {
		return instance;
	}
	
	private final ReadWriteLock lock = new ReadWriteLock();
	public DefaultRunQueue(String name, int threads) {
		super(name, threads);
	}
	public DefaultRunQueue(String name) {
		this(name, -1);
	}
	public DefaultRunQueue(String name, Delegator delegator, int threads) {
		super(name, delegator, threads);
	}
	public DefaultRunQueue(String name, Delegator delegator) {
		this(name, delegator, -1);
	}
	public DefaultRunQueue(String name, FutureDelegator delegator, int threads) {
		super(name, delegator, threads);
	}
	public DefaultRunQueue(String name, FutureDelegator delegator) {
		this(name, delegator, -1);
	}

	public void write(BaseAccessor data, BaseWriter actor) {
		lock.write(data, actor);
	}

	public Object read(BaseAccessor data, BaseReader reader) {
		return lock.read(data, reader);
	}

}
