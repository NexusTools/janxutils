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

/**
 *
 * @author katelyn
 */
public class BoundLocks<A extends BaseAccessor> extends Lockable<A> {
	
	private final Lockable[] boundLocks;
	public BoundLocks(Lockable... locks) {
		boundLocks = locks;
	}

	@Override
	public void lock(boolean exclusive) {
		for(Lockable lock : boundLocks) {
			lock.lock(exclusive);
		}
	}

	@Override
	public void upgrade() {
		for(Lockable lock : boundLocks) {
			lock.upgrade();
		}
	}

	@Override
	public void downgrade() {
		for(Lockable lock : boundLocks) {
			lock.downgrade();
		}
	}

	@Override
	public boolean tryFastUpgrade() {
		ArrayList<Lockable> undo = new ArrayList();
		for(Lockable lock : boundLocks) {
			if(lock.tryFastUpgrade()) {
				undo.add(lock);
			} else {
				for(Lockable u : undo)
					u.downgrade();
			}
		}
		return true;
	}

	@Override
	public boolean tryLock(boolean write) {
		ArrayList<Lockable> undo = new ArrayList();
		for(Lockable lock : boundLocks) {
			if(lock.tryLock(write)) {
				undo.add(lock);
			} else {
				for(Lockable u : undo)
					u.unlock();
			}
		}
		return true;
	}

	@Override
	public void unlock() {
		for(Lockable lock : boundLocks) {
			lock.unlock();
		}
	}

	@Override
	public void fastUpgrade() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
