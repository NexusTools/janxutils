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
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 * @author katelyn
 */
public class ReadWriteLock extends Lockable {
	
	private static int defaultPermitCount = 20;
	
	private abstract class SemiLocked extends FakeLock {
		int unlock;
		public SemiLocked(int unlock) {
			update(unlock);
		}
		protected void update(int unlock) {
			this.unlock = unlock;
		}
		@Override
		public void unlock() {
			if(unlock > 0) {
				semaphore.release(unlock);
				//System.out.println("[" + Thread.currentThread().getName() + "] Released " + unlock + " Permits");
			}
		}
	}
	
	private class FullyLocked extends SemiLocked {
		public FullyLocked(int unlock) {
			super(unlock);
		}
		@Override
		public void lock(boolean exclusive) {
			tryLock(exclusive);
		}
		@Override
		public boolean tryLock(boolean exclusive) {
			pushFrame(new FullyLocked(0));
			return true;
		}
	}
	
	private final int sharedRem;
	private class SharedLock extends SemiLocked {

		private int upgradeCount = 0;
		private final int initialUnlock;
		public SharedLock(int unlock) {
			super(unlock);
			initialUnlock = unlock;
		}
		
		@Override
		public void lock(boolean exc) {
			if(exc) {
				//System.out.println("[" + Thread.currentThread().getName() + "] Acquiring " + sharedRem + " Permits");
				if(!semaphore.tryAcquire(sharedRem))
					try {
						semaphore.release();
						exclusive.acquireUninterruptibly();
						semaphore.acquireUninterruptibly(sharedRem);
					} finally {
						exclusive.release();
					}
				pushFrame(new FullyLocked(sharedRem));
				//System.out.println("[" + Thread.currentThread().getName() + "] Permits Obtained");
			} else
				pushFrame(new SharedLock(0));
		}
		@Override
		public void upgrade() {
			upgradeCount++;
			if(upgradeCount > 1)
				return;
			
			//System.out.println("[" + Thread.currentThread().getName() + "] Waiting on " + sharedRem + " Permits");
			try {
				semaphore.release();
				exclusive.acquireUninterruptibly();
				semaphore.acquireUninterruptibly(sharedRem);
			} finally {
				exclusive.release();
			}
			update(sharedRem);
			//System.out.println("[" + Thread.currentThread().getName() + "] Permits Obtained");
		}
		@Override
		public void downgrade() {
			if(upgradeCount < 1)
				throw new IllegalThreadStateException("This thread has not been upgraded, and cannot be downgraded because of that.");
			
			upgradeCount --;
			if(upgradeCount < 1) {
				//System.out.println("[" + Thread.currentThread().getName() + "] Released " + sharedRem + " Permits");
	
				semaphore.release(sharedRem);
				update(initialUnlock);
			}
		}
		@Override
		public boolean tryFastUpgrade() {
			if(upgradeCount > 0 || semaphore.tryAcquire(sharedRem)) {
				update(sharedRem);
				upgradeCount++;
				return true;
			}
			return false;
		}
		@Override
		public boolean tryLock(boolean ex) {
			if(ex)
				try {
					return semaphore.tryAcquire(sharedRem);
				} finally {
					pushFrame(new FullyLocked(sharedRem));
				}
			pushFrame(new SharedLock(0));
			return true; // Already have a shared lock
		}
	}
	
	private Lockable fullyUnlocked = new Lockable() {
		@Override
		public void lock(boolean exc) {
			if(exc) {
				//System.out.println("[" + Thread.currentThread().getName() + "] Acquiring " + totalPermits + " Permits");
				if(!semaphore.tryAcquire(totalPermits))
					try {
						//System.out.println("[" + Thread.currentThread().getName() + "] Waiting for Other Threads");
						exclusive.acquireUninterruptibly();
						semaphore.acquireUninterruptibly(totalPermits);
					} finally {
						exclusive.release();
					}
				pushFrame(new FullyLocked(totalPermits));
			} else {
				//System.out.println("[" + Thread.currentThread().getName() + "] Acquiring 1 Permit");
				semaphore.acquireUninterruptibly();
				pushFrame(new SharedLock(1));
			}
			//System.out.println("[" + Thread.currentThread().getName() + "] Permits Obtained");
		}

		@Override
		public void upgrade() {
			throw new IllegalThreadStateException("This thread does not yet have a lock, and so cannot be upgraded");
		}
		@Override
		public void downgrade() {
			throw new IllegalThreadStateException("This thread does not yet have a lock, and so cannot be downgraded");
		}
		@Override
		public boolean tryFastUpgrade() {
			throw new IllegalThreadStateException("This thread does not yet have a lock, and so cannot be upgraded");
		}

		@Override
		public boolean tryLock(boolean ex) {
			if(ex)
				try {
					return semaphore.tryAcquire(sharedRem);
				} finally {
					pushFrame(new FullyLocked(sharedRem));
				}
			
			try {
				return semaphore.tryAcquire(totalPermits);
			} finally {
				pushFrame(new SharedLock(0));
			}
		}

		@Override
		public void unlock() {
			throw new IllegalThreadStateException("This thread does not yet have a lock, and so cannot be unlocked");
		}
		
	};
	
	private final Semaphore semaphore;
	private final Semaphore exclusive = new Semaphore(1);
	private final ThreadLocal<ArrayList<Lockable>> frames = new ThreadLocal() {
		@Override
		protected Object initialValue() {
			return new ArrayList<Lockable>();
		}
	};
	private final int totalPermits;
	
	public ReadWriteLock() {
		this(defaultPermitCount);
	}
	
	public ReadWriteLock(int permits) {
		semaphore = new Semaphore(totalPermits = permits);
		sharedRem = totalPermits-1;
	}
	
	protected Lockable current() {
		List<Lockable> list = frames.get();
		int pos = list.size() - 1;
		if(pos < 0)
			return fullyUnlocked;
		return list.get(pos);
	}
	
	protected void pushFrame(Lockable lock) {
		frames.get().add(lock);
	}

	@Override
	public void lock(boolean exclusive) {
		current().lock(exclusive);
	}

	@Override
	public void upgrade() {
		current().upgrade();
	}

	@Override
	public void downgrade() {
		current().downgrade();
	}

	@Override
	public boolean tryFastUpgrade() {
		return current().tryFastUpgrade();
	}

	@Override
	public boolean tryLock(boolean exclusive) {
		return current().tryLock(exclusive);
	}

	@Override
	public void unlock() {
		List<Lockable> list = frames.get();
		int pos = list.size() - 1;
		if(pos < 0)
			fullyUnlocked.unlock();
		else
			list.remove(pos).unlock();
	}

}
