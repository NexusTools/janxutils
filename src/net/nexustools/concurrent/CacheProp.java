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

import java.util.concurrent.Semaphore;

/**
 *
 * @author katelyn
 * @param <T>
 */
public abstract class CacheProp<T> extends Prop<T> {
	
	private final Semaphore cacheLock = new Semaphore(1);
	private String genID;

	public CacheProp() {}

	@Override
	public T get() {
		return read(new BaseReader<T, PropAccessor<T>>() {
			public T read(PropAccessor<T> data, Lockable lock) {
				lock.lock();
				try {
					if(!needsUpdate(data))
						return data.get();
				} finally {
					lock.unlock();
				}
				String myGenID = net.nexustools.utils.StringUtils.randomString(32);
				cacheLock.acquireUninterruptibly();
				try {
					genID = myGenID;
				} finally {
					cacheLock.release();
				}
				// Check if another thread already finished
				lock.lock(true);
				try {
					if(!needsUpdate(data))
						return data.get();
				} finally {
					lock.unlock();
				}
				T newData = create();
				cacheLock.acquireUninterruptibly();
				try {
					if(myGenID.equals(genID)) {
						lock.lock(true);
						try {
							data.set(newData);
						} finally {
							lock.unlock();
						}
					}
				} finally {
					cacheLock.release();
				}
				return newData;
			}
		});
	}
	
	protected abstract T create();
	
	/**
	 * Clears the internal cache, and aborts any current cache processor.
	 */
	@Override
	public void clear() {
		cacheLock.acquireUninterruptibly();
		try {
			// Reset active generation
			genID = null;
			super.clear();
		} finally {
			cacheLock.release();
		}
	}

	protected boolean needsUpdate(PropAccessor<T> data) {
		return !data.isset();
	}

}
