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

import java.util.ArrayList;
import net.nexustools.concurrent.ReadWriteLock.IfUpgradeWriter;
import net.nexustools.concurrent.ReadWriteLock.UpgradeReader;

/**
 *
 * @author katelyn
 */
public abstract class Accessor<T> {
	
	public static abstract class Actor<A extends Accessor> {
		public abstract void perform(A accessor);
	}
	public static abstract class IfActor<A extends Accessor> extends Actor<A> {
		public boolean test(A accessor) {
			return accessor.isset();
		}
	}
	public static abstract class Reader<A extends Accessor, R> {
		public abstract R read(A value);
	}
	public static abstract class IfReader<A extends Accessor, R> extends Reader<A, R> {
		public boolean test(A accessor) {
			return accessor.isset();
		}
	}
	public static abstract class Default<A extends Accessor> {
		public boolean isnull(A accessor) {
			return accessor.isnull();
		}
		public abstract void populate(A accessor);
	}
	public static interface Watcher<T> {
		public void changed(Accessor<T> accessor, T cur, T old);
	}
	
	protected final ReadWriteLock lock = new ReadWriteLock();
	protected final ArrayList<Watcher> watchers = new ArrayList();
	
	public final void watch(final Watcher<T> watcher) {
		/*wLock.act(new ReadWriteLock.UpgradeActor<Accessor>() {
			@Override
			public boolean test(Accessor accessor) {
				return !watchers.contains(watcher);
			}
			@Override
			public void perform(Accessor accessor) {
				watcher.changed(Accessor.this, internal(), null);
				watchers.add(watcher);
			}
		}, this);*/
		lock.act(new ReadWriteLock.IfUpgradeWriter() {
			@Override
			public void perform(ReadWriteLock value) {
				watcher.changed(Accessor.this, internal(), null);
				watchers.add(watcher);
			}
			@Override
			public boolean test() {
				return !watchers.contains(watcher);
			}
		});
	}
	
	/**
	 * Performs an action on this property,
	 * the action may be performed by a RunQueue.
	 * 
	 * Do not trust this method to be blocking.
	 * 
	 * @param actor Actor to use
	 */
	public final void act(final Actor<Accessor<T>> actor) {
		final Accessor target = actorTarget(actor);
		if(actor instanceof IfActor)
			lock.act(new IfUpgradeWriter() {
				@Override
				public boolean test() {
					return ((IfActor)actor).test(target);
				}
				@Override
				public void perform(ReadWriteLock lock) {
					actor.perform(target);
				}
			});
		else
			lock.act(new IfUpgradeWriter() {
				@Override
				public boolean test() {
					return true;
				}
				@Override
				public void perform(ReadWriteLock lock) {
					actor.perform(target);
				}
			});
	}
	
	protected <R> Accessor<T> actorTarget(Actor<Accessor<T>> actor) {
		return this;
	}
	
	/**
	 * Reads from this property in an advanced way.
	 * This method is assured to be blocking.
	 * 
	 * @param <R>
	 * @param reader
	 * @return 
	 */
	public final <R> R read(final Reader<Accessor<T>, R> reader) {
		final Accessor target = readTarget(reader);
		if(reader instanceof IfReader)
			return lock.read(new UpgradeReader<R>() {
				@Override
				public boolean init(ReadWriteLock lock) {
					lock.lock();
					return ((IfReader)reader).test(target);
				}

				@Override
				public R read() {
					return (R)reader.read(target);
				}
			});
		else
			return lock.read(new UpgradeReader<R>() {
				@Override
				public R read() {
					return (R)reader.read(target);
				}
			});
	}
	
	protected <R> Accessor<T> readTarget(Reader<Accessor<T>, R> reader) {
		return this;
	}
	
	/**
	 * Direct access to the internal variable, no lock checking.
	 * This method should only be used by actors.
	 * 
	 * @return 
	 */
	public abstract T internal();
	/**
	 * Direct access to the internal variable, no lock checking.
	 * This method should only be used by actors.
	 * 
	 * @return 
	 */
	public abstract T internal(T object);
	
	public abstract boolean isset();
	public final boolean isnull() {
		return !isset();
	}
	
}
