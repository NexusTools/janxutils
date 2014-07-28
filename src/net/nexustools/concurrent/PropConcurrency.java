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

import net.nexustools.runtime.RunQueue;

/**
 * A concurrent stage to act upon.
 * 
 * @author katelyn
 */
public abstract class PropConcurrency<A extends BaseAccessor> {
	
	protected final ReadWriteLock lock;
	protected final ConcurrentStage<A> stage;
	protected final RunQueue dispatchQueue;
	
	public PropConcurrency(ConcurrentStage stage, RunQueue runQueue) {
		this.stage = stage;
		if(stage instanceof ReadWriteLock)
			lock = (ReadWriteLock)stage;
		else
			lock = new ReadWriteLock();
		dispatchQueue = runQueue;
	}
	
	public PropConcurrency(RunQueue runQueue) {
		this(runQueue, runQueue);
	}
	
	protected abstract A directAccessor();
	
	public final void write(BaseWriter<A> actor) {
		lock.write(directAccessor(), actor);
	}

	public final <R> R read(BaseReader<R, A> reader) {
		return (R)lock.read(directAccessor(), reader);
	}
	
	public final void act(BaseActor actor) {
		stage.act(actor);
	}
	
}
