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
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.VoidReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.Handler;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author kate
 */
public class ThreadCondition implements Condition {
	
	private final Prop<Boolean> condition;
	private final PropList<Thread> waitingThreads = new PropList<Thread>();
	public ThreadCondition(boolean initialState) {
		condition = new Prop<Boolean>(initialState);
	}
	public ThreadCondition() {
		this(false);
	}
	
	public boolean waitForUninterruptibly(long millis) {
		boolean ret;
		long until = System.currentTimeMillis() + millis;
		waitingThreads.unique(Thread.currentThread());
		while(!(ret = condition.isTrue())) {
			long left = until - System.currentTimeMillis();
			if(left > 0)
				try {
					Thread.sleep(left);
				} catch (InterruptedException ex) {}
			else
				break;
		}
		waitingThreads.remove(Thread.currentThread());
		return ret;
	}
	
	public void waitForUninterruptibly() {
		waitForUninterruptibly(Integer.MAX_VALUE);
	}
	
	public boolean waitFor(long millis) throws InterruptedException{
		boolean ret;
		waitingThreads.unique(Thread.currentThread());
		if(!(ret = condition.isTrue()))
			Thread.sleep(millis);
		waitingThreads.remove(Thread.currentThread());
		return ret;
	}
	
	public void waitFor() throws InterruptedException {
		waitFor(Integer.MAX_VALUE);
	}
	
	public void start() {
		condition.set(false);
	}
	public void start(final Runnable finalize) {
		condition.write(new IfWriter<PropAccessor<Boolean>>() {
			@Override
			public boolean test(PropAccessor<Boolean> against) {
				return against.isTrue();
			}
			@Override
			public void write(PropAccessor<Boolean> data) {
				finalize.run();
				data.set(false);
			}
		});
	}
	public void ifFalse(final Runnable block) {
		condition.read(new VoidReader<PropAccessor<Boolean>>() {
			@Override
			public void readV(PropAccessor<Boolean> data) {
				if(!data.get())
					block.run();
			}
		});
	}
	public void ifTrue(final Runnable block) {
		condition.read(new VoidReader<PropAccessor<Boolean>>() {
			@Override
			public void readV(PropAccessor<Boolean> data) {
				if(data.get())
					block.run();
			}
		});
	}
	public void ifRun(final Runnable trueBlock, final Runnable falseBlock) {
		condition.read(new VoidReader<PropAccessor<Boolean>>() {
			@Override
			public void readV(PropAccessor<Boolean> data) {
				if(data.get())
					trueBlock.run();
				else
					falseBlock.run();
			}
		});
	}
	public void finish() {
		condition.set(true);
		interrupt();
	}
	public void sync(final Handler<Boolean> syncHandler) {
		condition.write(new Writer<PropAccessor<Boolean>>() {
			@Override
			public void write(PropAccessor<Boolean> data) {
				syncHandler.handle(data.get());
			}
		});
	}
	public void finish(final Runnable finalize) {
		condition.write(new IfWriter<PropAccessor<Boolean>>() {
			@Override
			public boolean test(PropAccessor<Boolean> against) {
				return !against.isTrue();
			}
			@Override
			public void write(PropAccessor<Boolean> data) {
				finalize.run();
				data.set(true);
				interrupt();
			}
		});
	}

	public void interrupt() {
		for(Thread thread : waitingThreads)
			thread.interrupt();
	}

	public boolean check() {
		return condition.get();
	}
	
}
