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

package nexustools.runtime.tasks;

import nexustools.runtime.RunQueue;
import nexustools.runtime.tasks.TaskException;
import nexustools.runtime.tasks.Task;

/**
 *
 * @author katelyn
 * 80
 */
public abstract class ProcessingTask<T> implements Task {

	private T data;
	
	protected abstract T process();
	protected abstract void error(Throwable t);
	protected abstract void finish(T data);

	@Override
	public void onStart(RunQueue queue) throws TaskException {
		if(!(queue instanceof ProcessingTaskQueue)) {
			throw new ClassCastException("ProcessingTask can only be used with ProcessingTaskQueue");
		}
	}

	@Override
	public void onComplete(RunQueue queue) throws TaskException {
		data = null;
		if(!(queue instanceof ProcessingTaskQueue)) {
			throw new ClassCastException("ProcessingTask can only be used with ProcessingTaskQueue");
		}
		try {
			finish(data);
		} catch(Throwable t) {
			onError(t, queue);
		}
	}

	@Override
	public void onError(Throwable t, RunQueue queue) {
		data = null;
		if(!(queue instanceof ProcessingTaskQueue)) {
			throw new ClassCastException("ProcessingTask can only be used with ProcessingTaskQueue");
		}
		error(t);
	}
	
	@Override
	public final void run() throws TaskException {
		data = process();
	}
	
}
