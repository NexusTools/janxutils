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

import net.nexustools.concurrent.logic.VoidReader;
import net.nexustools.concurrent.logic.Writer;

/**
 *
 * @author kate
 */
public class Condition {
	
	private final Prop<Boolean> condition;
	private final PropList<Thread> waitingThreads = new PropList<Thread>();
	public Condition(boolean initialState) {
		condition = new Prop<Boolean>(initialState);
	}
	public Condition() {
		this(false);
	}
	
	public void waitFor() {
		waitingThreads.unique(Thread.currentThread());
		while(!condition.isTrue())
			try {
				Thread.sleep(60 * 60 * 1000);
			} catch (InterruptedException ex) {}
		waitingThreads.remove(Thread.currentThread());
	}
	
	public void start() {
		condition.set(false);
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
		for(Thread thread : waitingThreads)
			thread.interrupt();
	}
	public void finish(final Runnable finalize) {
		condition.write(new Writer<PropAccessor<Boolean>>() {
			@Override
			public void write(PropAccessor<Boolean> data) {
				data.set(true);
				finalize.run();
			}
		});
		for(Thread thread : waitingThreads)
			thread.interrupt();
	}

	public boolean isFinished() {
		return condition.get();
	}
	
}
