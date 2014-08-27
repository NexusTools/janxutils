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
import net.nexustools.data.accessor.BaseAccessor;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public abstract class Lockable<A extends BaseAccessor> implements ConcurrentStage<A> {
	
	public final void lock() {
		lock(false);
	}
	public abstract void lock(boolean exclusive);
	
	/**
	 * Upgrade by first unlocking fully, than locking again.
	 * 
	 * A thread can upgrade multiple times, for each upgrade a call to
	 * downgrade is required before you can get back to a shared lock.
	 */
	public abstract void upgrade();
	
	/**
	 * Upgrade as fast as possible, may or may not require unlocking first.
	 * 
	 * A thread can upgrade multiple times, for each upgrade a call to
	 * downgrade is required before you can get back to a shared lock.
	 */
	public abstract void fastUpgrade();
	/**
	 * Give up the last exclusive lock we gained by upgrading.
	 */
	public abstract void downgrade();
	
	public <I> boolean writeLockTest(I against, Testable<I> testable) throws Throwable {
		boolean worked = false;
		lock();
		try {
			if(testable.test(against)) {
				if(!tryFastUpgrade()) {
					upgrade();
					if(!testable.test(against))
						return false;
				}
				worked = true;
			}
		} finally {
			if(!worked)
				unlock();
		}
		return worked;
	}
	
	public <I> boolean fastUpgradeTest(I against, Testable<I> testable) throws Throwable {
		boolean worked = false;
		try {
			if(testable.test(against)) {
				if(!tryFastUpgrade()) {
					upgrade();
					worked = true;
					if(!testable.test(against)) {
						downgrade();
						return false;
					}
				} else
					worked = true;
			}
		} catch(Throwable t) {
			if(worked)
				downgrade();
			throw NXUtils.wrapRuntime(t);
		}
		return worked;
	}
	
	public boolean fastUpgradeTest(Testable<Void> testable) throws Throwable {
		return fastUpgradeTest(null, testable);
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
	public final void write(A data, BaseWriter<A> actor) throws InvocationTargetException {
		try {
			actor.write(data, this);
		} catch (Throwable ex) {
			if(ex instanceof InvocationTargetException)
				throw (InvocationTargetException)ex;
			throw new InvocationTargetException(ex);
		}
	}
	/**
	 * Runs a Reader using this Lockable.
	 * 
	 * @param data
	 * @param reader
	 * @return  
	 */
	public final <R> R read(A data, BaseReader<R, A> reader) throws InvocationTargetException{
		try {
			return reader.read(data, this);
		} catch (Throwable ex) {
			if(ex instanceof InvocationTargetException)
				throw (InvocationTargetException)ex;
			throw new InvocationTargetException(ex);
		}
	}

	public void invokeLater(Runnable run) {
		run.run();
	}
	
}
