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

import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public abstract class Lockable implements ConcurrentStage {
	
	public final void lock() {
		lock(false);
	}
	public abstract void lock(boolean exclusive);
	
	/**
	 * Upgrade as fast as possible from a shared to a exclusive lock.
	 * 
	 * A thread can upgrade multiple times, for each upgrade a call to
	 * downgrade is required before you can get back to a shared lock.
	 */
	public abstract void upgrade();
	/**
	 * Give up the last exclusive lock we gained by upgrading.
	 */
	public abstract void downgrade();
	
	/**
	 * Attempts to upgrade as fast as possible,
	 * while also checking if a test is valid.
	 * 
	 * The test is run once with a read lock,
	 * and another time with a write lock.
	 * 
	 * @param testable The test to run
	 * @return Returns true if the test remained true after a full upgrade was completed.
	 */
	public <I> boolean tryFastUpgradeTest(I against, Testable<I> testable) {
		lock();
		if(testable.test(against)) {
			if(!tryFastUpgrade()) {
				upgrade();
				if(!testable.test(against)) {
					downgrade();
					unlock();
					return false;
				}
			}
			return true;
		}
		unlock();
		return false;
	}
	
	public boolean tryFastUpgradeTest(Testable<Void> testable) {
		return tryFastUpgradeTest(null, testable);
	}
	
	/**
	 * Attempts to both aquire an upgrade without blocking,
	 * and prevent unlocking at any point.
	 * 
	 * @return true if a write lock was aquired without waiting and without unlocking
	 */
	public abstract boolean tryFastUpgrade();
	
	/**
	 * Attempts to aquire a lock without blocking.
	 * 
	 * @param write
	 * @return true upon success, false otherwise
	 */
	public abstract boolean tryLock(boolean write);
	
	/**
	 * Unlocks this lockable.
	 * 
	 * This also automatically downgrades any upgrade calls that were made.
	 */
	public abstract void unlock();
	
	/**
	 * Runs a Actor using this Lockable.
	 * 
	 * @param <I>
	 * @param <A>
	 * @param data
	 * @param actor 
	 */
	public void write(BaseAccessor data, BaseWriter actor) {
		actor.write(data, this);
	}
	/**
	 * Runs a Reader using this Lockable.
	 * 
	 * @param <R>
	 * @param <T>
	 * @param <A>
	 * @param data
	 * @param reader
	 * @return  
	 */
	public Object read(BaseAccessor data, BaseReader reader) {
		return reader.read(data, this);
	}

	public void invokeLater(Runnable run) {
		run.run();
	}
	
}
