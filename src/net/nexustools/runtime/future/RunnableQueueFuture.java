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

package net.nexustools.runtime.future;

import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class RunnableQueueFuture<R extends Runnable> extends QueueFuture {
	
	private static final ThreadLocal<RunnableQueueFuture> currentQueueFuture = new ThreadLocal();
	private static final ThreadLocal<Testable<Void>> currentTestable = new ThreadLocal();
	
	public static RunnableQueueFuture currentQueueFuture() {
		return currentQueueFuture.get();
	}
	
	public static boolean isCurrentCancelled() {
		return !currentTestable.get().test(null);
	}

	public final R runnable;
	public RunnableQueueFuture(R runnable, State state) {
		super(state);
		this.runnable = runnable;
	}

	@Override
	protected void executeImpl(Testable<Void> isRunning) {
		currentQueueFuture.set(this);
		try {
			if(isRunning.test(null)) {
				currentTestable.set(isRunning);
				try {
					runnable.run();
				} finally {
					currentTestable.remove();
				}
			}
		} finally {
			currentQueueFuture.remove();
		}
	}
	
}
