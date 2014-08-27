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

import java.lang.reflect.InvocationTargetException;
import net.nexustools.utils.Processor;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public class RunTask<R extends Runnable> extends DefaultTask {
	
	public final R runnable;
	public RunTask(R runnable, State state) {
		super(state);
		if(runnable == null)
			throw new NullPointerException();
		this.runnable = runnable;
	}

	@Override
	protected void executeImpl() throws Throwable {
		syncExecute(runnable);
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

	public RunTask copy(State state) {
		return new RunTask(runnable, state);
	}
	
}
