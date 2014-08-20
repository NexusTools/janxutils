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

package net.nexustools.runtime.logic;

import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class RunTask<R extends Runnable> extends DefaultTask {
	
	private static final ThreadLocal<RunTask> currentQueueFuture = new ThreadLocal();
	private static final ThreadLocal<Testable<Void>> currentTestable = new ThreadLocal();
	
	public static RunTask currentQueueFuture() {
		return currentQueueFuture.get();
	}
	
	public static boolean isCurrentCancelled() {
		return !currentTestable.get().test(null);
	}

	public final R runnable;
	public RunTask(R runnable, State state) {
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

	@Override
	public int hashCode() {
		return runnable.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RunTask<?> other = (RunTask<?>) obj;
		if (this.runnable != other.runnable && (this.runnable == null || !this.runnable.equals(other.runnable))) {
			return false;
		}
		return true;
	}
	
	
	
}