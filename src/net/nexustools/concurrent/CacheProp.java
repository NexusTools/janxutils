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

import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.utils.Creator;
import net.nexustools.utils.SimpleCreator;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 * @param <T>
 * @param <U>
 */
public class CacheProp<T, U> {
	
	private final Prop<T> prop;
	private final Lockable cacheLock;
	private final Lockable sourceLock;
	private final Creator<T, U> creator;
	private final U creatorUsing;
	private String genID;
	
	public static BoundLocks locksForProps(Lockable propLock, Prop... reliesOn) {
		Lockable[] locks = new Lockable[reliesOn.length+1];
		locks[0] = propLock;
		int i = 1;
		for(Prop prop : reliesOn)
			locks[i++] = prop.lock;
		return new BoundLocks(locks);
	}

	public CacheProp(Lockable cacheLock, Lockable propLock, Lockable sourceLock, T def, Creator<T, U> creator, U creatorUsing) {
		this.creator = creator;
		this.creatorUsing = creatorUsing;
		this.prop = new Prop(propLock, def);
		this.sourceLock = sourceLock;
		this.cacheLock = cacheLock;
	}

	public CacheProp(Lockable cacheLock, Lockable propLock, Lockable sourceLock, T def, SimpleCreator<T, U> creator) {
		this(cacheLock, propLock, sourceLock, def, creator, null);
	}

	public CacheProp(Lockable cacheLock, Lockable propLock, T def, Creator<T, U> creator, U creatorUsing, Prop<T>... sourceProps) {
		this(cacheLock, propLock, locksForProps(propLock, sourceProps), def, creator, creatorUsing);
	}

	public CacheProp(Lockable cacheLock, Lockable propLock, T def, SimpleCreator<T, U> creator, Prop<T>... sourceProps) {
		this(cacheLock, propLock, locksForProps(propLock, sourceProps), def, creator, null);
	}

	public CacheProp(Lockable cacheLock, Lockable propLock, Creator<T, U> creator, U creatorUsing, Prop<T>... sourceProps) {
		this(cacheLock, propLock, null, creator, creatorUsing, sourceProps);
	}

	public CacheProp(Lockable cacheLock, Lockable propLock, SimpleCreator<T, U> creator, Prop<T>... sourceProps) {
		this(cacheLock, propLock, null, creator, null, sourceProps);
	}
	
	public CacheProp(T def, Creator<T, U> creator, U creatorUsing, Prop<T>... sourceProps) {
		this(new ReadWriteLock(), new ReadWriteLock(), def, creator, creatorUsing, sourceProps);
	}
	
	public CacheProp(T def, SimpleCreator<T, U> creator, Prop<T>... sourceProps) {
		this(def, creator, null, sourceProps);
	}
	
	public CacheProp(Creator<T, U> creator, U creatorUsing, Prop<T>... sourceProps) {
		this(new ReadWriteLock(), new ReadWriteLock(), creator, creatorUsing, sourceProps);
	}
	
	public CacheProp(SimpleCreator<T, U> creator, Prop<T>... sourceProps) {
		this(creator, null, sourceProps);
	}
	
	public void set(T value) {
		sourceLock.lock();
		try {
			prop.clear();
			cacheLock.lock(true);
			try {
				// Reset active generation
				genID = null;
			} finally {
				cacheLock.unlock();
			}
		} finally {
			sourceLock.unlock();
		}
	}
	
	public T getInternal() {
		return prop.get();
	}

	public T get() {
		return prop.read(new BaseReader<T, PropAccessor<T>>() {

			public T read(PropAccessor<T> data, Lockable<PropAccessor<T>> lock) {
				lock.lock();
				try {
					if(!needsUpdate(data))
						return data.get();
					
					final String myGenID;
					cacheLock.lock(true);
					try {
						// Check if another thread already finished
						if(!needsUpdate(data))
							return data.get();

						myGenID = net.nexustools.utils.StringUtils.randomString(32);
						genID = myGenID;
					} finally {
						cacheLock.unlock();
					}

					return doUpdate(lock, data, new Testable<Void>() {
						public boolean test(Void against) {
							return myGenID.equals(genID);
						}
					});
				} finally {
					lock.unlock();
				}
			}
		});
	}
	
	protected T doUpdate(Lockable lock, PropAccessor<T> data, Testable<Void> test) {
		T newData;
		sourceLock.lock(); // Lock all sources for reading
		try {
			newData = creator.create(creatorUsing);
			cacheLock.lock(true);
			try {
				if(test.test(null)) {
					lock.upgrade();
					try {
						init(newData);
						data.set(newData);
					} finally {
						lock.downgrade();
					}
				}
				genID = null;
			} finally {
				cacheLock.unlock();
			}
		} finally {
			sourceLock.unlock();
		}
		return newData;
	}
	
	protected void init(T object) {}
	
	/**
	 * Clears the internal cache, and aborts any current cache processor.
	 */
	public void clear() {
		set(null);
	}

	protected boolean needsUpdate(PropAccessor<T> data) {
		return !data.isset();
	}
	
	public void update() {
		sourceLock.lock();
		try {
			prop.write(new BaseWriter<PropAccessor<T>>() {
				public void write(PropAccessor<T> data, Lockable lock) {
					lock.lock(true);
					try {
						data.clear();
						doUpdate(lock, data, Testable.TRUE);
					} finally {
						lock.unlock();
					}
				}
			});
		} finally {
			sourceLock.unlock();
		}
	}

}
