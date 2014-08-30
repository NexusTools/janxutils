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
import java.lang.reflect.InvocationTargetException;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.BaseAccessor;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 */
public class ReadWriteLock extends Lockable {
	
	private static boolean verbose;
	public static final byte defaultPermitCount;
	static {
		verbose = System.getProperty("readwritelog", "").equalsIgnoreCase("verbose");
		defaultPermitCount = Byte.valueOf(System.getProperty("readwritepermits", String.valueOf(Runtime.getRuntime().availableProcessors()*2))); // handle up to 2 threads per core, maximum 255
	}
	
	private static final PropMap<Object, ReadWriteLock> lockMap = new PropMap(PropMap.Type.WeakHashMap);
	public static Lockable lockFor(final Object target) {
		if(target instanceof ReadWriteConcurrency)
			return ((ReadWriteConcurrency)target).lockable();
		
		return lockMap.read(new SoftWriteReader<Lockable, MapAccessor<Object, ReadWriteLock>>() {
			Lockable lock;
			@Override
			public boolean test(MapAccessor<Object, ReadWriteLock> against) {
				return (lock = against.get(target)) == null;
			}
			@Override
			public Lockable soft(MapAccessor<Object, ReadWriteLock> data) {
				return lock;
			}
			@Override
			public Lockable read(MapAccessor<Object, ReadWriteLock> data) {
				lock = new ReadWriteLock();
				data.put(target, (ReadWriteLock)lock);
				return lock;
			}
		});
	}

	public static void setVerbose(boolean b) {
		verbose = b;
	}
	
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
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Released " + unlock + " Permits");
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
		private final int initialPermits;
		public SharedLock(int initial) {
			super(initial);
			initialPermits = initial;
		}
		
		@Override
		public void lock(boolean exc) {
			if(upgradeCount > 0) {
				pushFrame(new FullyLocked(0));
				return;
			}
			
			if(exc) {
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Acquiring " + sharedRem + " Permits");
				
				if(!semaphore.tryAcquire(sharedRem))
					try {
						semaphore.release();
						exclusive.acquireUninterruptibly();
						semaphore.acquireUninterruptibly(totalPermits);
					} finally {
						exclusive.release();
					}
				pushFrame(new FullyLocked(sharedRem));
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] " + sharedRem + " Permits Obtained");
			} else
				pushFrame(new SharedLock(0));
		}
		@Override
		public void fastUpgrade() {
			if(!tryFastUpgrade())
				upgrade();
		}
		@Override
		public void upgrade() {
			upgradeCount++;
			if(upgradeCount > 1)
				return;
			
			if(verbose)
				System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Waiting on " + sharedRem + " Permits");
			try {
				semaphore.release();
				exclusive.acquireUninterruptibly();
				semaphore.acquireUninterruptibly(totalPermits);
			} finally {
				exclusive.release();
			}
			update(totalPermits);
			if(verbose)
				System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Permits Obtained");
		}
		@Override
		public void downgrade() {
			if(upgradeCount < 1)
				throw new IllegalThreadStateException("This thread has not been upgraded, and cannot be downgraded because of that.");
			
			upgradeCount --;
			if(upgradeCount < 1) {
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Released " + sharedRem + " Permits");
	
				semaphore.release(sharedRem);
				update(initialPermits);
			}
		}
		@Override
		public boolean tryFastUpgrade() {
			if(verbose)
				System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Trying Fast Upgrade");
			
			if(upgradeCount > 0 || semaphore.tryAcquire(sharedRem)) {
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Acquired " + sharedRem + " Permits");
	
				update(totalPermits);
				upgradeCount++;
				return true;
			}
			return false;
		}
		@Override
		public boolean tryLock(boolean ex) {
			if(upgradeCount > 0) {
				pushFrame(new FullyLocked(0));
				return true;
			}
			
			if(ex)
				if(semaphore.tryAcquire(sharedRem)) {
					pushFrame(new FullyLocked(sharedRem));
					if(verbose)
						System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] " + sharedRem + " Permits Obtained");
				} else
					return false;
			else
				pushFrame(new SharedLock(0));
			return true; // Already have a shared lock
		}
	}
	
	private Lockable fullyUnlocked = new Lockable() {
		@Override
		public void lock(boolean exc) {
			if(exc) {
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Acquiring " + totalPermits + " Permits");
				if(!semaphore.tryAcquire(totalPermits))
					try {
						if(verbose)
							System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Waiting for Other Threads");
						exclusive.acquireUninterruptibly();
						semaphore.acquireUninterruptibly(totalPermits);
					} finally {
						exclusive.release();
					}
				pushFrame(new FullyLocked(totalPermits));
			} else {
				if(verbose)
					System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Acquiring 1 Permit");
				semaphore.acquireUninterruptibly();
				pushFrame(new SharedLock(1));
			}
			if(verbose)
				System.out.println("[" + Thread.currentThread().getName() + "] [" + ReadWriteLock.this.toString() + "] Permits Obtained");
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
				if(semaphore.tryAcquire(totalPermits)) {
					pushFrame(new FullyLocked(totalPermits));
				} else
					return false;
			else if(semaphore.tryAcquire())
				pushFrame(new SharedLock(0));
			else
				return false;
			return true;
		}

		@Override
		public void unlock() {
			throw new IllegalThreadStateException("This thread does not yet have a lock, and so cannot be unlocked");
		}

		@Override
		public void fastUpgrade() {
			throw new IllegalThreadStateException("This thread does not yet have a lock, and so cannot be upgraded");
		}
		
	};
	
	private final Semaphore semaphore;
	private final Semaphore exclusive;
	private final ThreadLocal<StrongTypeList<Lockable>> frames = new ThreadLocal() {
		@Override
		protected Object initialValue() {
			return new StrongTypeList<Lockable>();
		}
	};
	
	private final int totalPermits;
	public ReadWriteLock() {
		this(defaultPermitCount, false);
	}
	public ReadWriteLock(boolean fair) {
		this(defaultPermitCount, fair);
	}
	public ReadWriteLock(int permits) {
		this(permits, false);
	}
	public ReadWriteLock(int permits, boolean fair) {
		if(permits < 2) {
			System.out.println(permits + " permits requested, but at least 2 required.");
			permits = 2;
		}
		semaphore = new Semaphore(totalPermits = permits);
		exclusive = new Semaphore(1, fair);
		sharedRem = totalPermits-1;
	}
	
	protected Lockable current() {
		StrongTypeList<Lockable> list = frames.get();
		int pos = list.length() - 1;
		if(pos < 0)
			return fullyUnlocked;
		return list.get(pos);
	}
	
	protected void pushFrame(Lockable lock) {
		frames.get().push(lock);
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

	public void fastUpgrade() {
		current().fastUpgrade();
	}

	@Override
	public void unlock() {
		StrongTypeList<Lockable> list = frames.get();
		int pos = list.length() - 1;
		if(pos < 0)
			fullyUnlocked.unlock();
		else
			list.remove(pos).unlock();
	}

}
