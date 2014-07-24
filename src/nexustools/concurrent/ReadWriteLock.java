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

package nexustools.concurrent;

/**
 *
 * @author katelyn
 */
public class ReadWriteLock {
	
	public static interface UpgradeActor {
		public boolean init(ReadWriteLock lock);
		public void perform(ReadWriteLock lock);
	}
	public static interface Testable {
		public boolean test();
	}
	public static abstract class UpgradeReader<R> implements UpgradeActor {
		R value;
		@Override
		public boolean init(ReadWriteLock lock) {
			lock.lock();
			return true;
		}
		@Override
		public final void perform(ReadWriteLock lock) {
			value = read();
		}
		public abstract R read();
	}
	public static abstract class UpgradeWriter implements UpgradeActor {
		@Override
		public boolean init(ReadWriteLock lock) {
			lock.lock(true);
			return true;
		}
	}
	public static abstract class IfUpgradeWriter implements UpgradeActor, Testable {
		@Override
		public final boolean init(ReadWriteLock lock) {
			return upgradeTest(this, lock);
		}
	}
	
	public static boolean upgradeTest(Testable uIf, ReadWriteLock lock) {
		lock.lock();
		if(uIf.test()) {
			if(!lock.tryFastUpgrade()) {
				lock.upgrade();
				if(!uIf.test())
					return false;
			}
			return true;
		}
		return false;
	}
	
	public void lock() {
		lock(false);
	}
	
	public void lock(boolean write) {
		
	}
	
	/**
	 * Upgrade from a read lock to a write lock,
	 * this process may involve temporarily unlocking.
	 * 
	 * Unlike lock, this method will not add weight
	 * and so unlock doesn't need to be called again.
	 * 
	 * @return true if upgraded without unlocking, false otherwise
	 */
	public boolean upgrade() {
		return false;
	}
	
	public void downgrade() {
	}
	
	public boolean tryLock(boolean write) {
		return false;
	}
	
	/**
	 * Attempts to both aquire an upgrade without blocking,
	 * and prevent unlocking at any point.
	 * 
	 * @return true if a write lock was aquired without waiting and without unlocking
	 */
	public boolean tryFastUpgrade() {
		return false;
	}
	
	/**
	 * Allows upgrading in the fastest possible manner concurrently.
	 * 
	 * Using the provided UpgradeHelper, this method runs a test
	 * while locking in a read-only manner, which if returns true attempts
	 * to gain a write lock very fast, and than perform the desired action.
	 * 
	 * If a write lock can be gained without losing the current lock or waiting,
	 * than the action can be performed immediately since that means nothing else
	 * is working with any sensative parts this lock protects.
	 * 
	 * Otherwise a write lock is made by waiting and the test is done again to
	 * make sure no other threads have already done what this is attempting to do.
	 * 
	 * This method is assured to be blocking.
	 * 
	 * @param actor
	 * @return 
	 */
	public boolean act(UpgradeActor actor) {
		boolean initialized = false;
		enterSection();
		try {
			if(initialized = actor.init(this))
				actor.perform(this);
		} finally {
			leaveSection();
		}
		return initialized;
	}
	
	public <R> R read(UpgradeReader<R> reader) {
		act(reader);
		return reader.value;
	}
	
	public void enterSection() {
		
	}
	
	public void leaveSection() {
		
	}

	/**
	 * Unlocks the last lock requested.
	 */
	public void unlock() {
	
	}
	
}
