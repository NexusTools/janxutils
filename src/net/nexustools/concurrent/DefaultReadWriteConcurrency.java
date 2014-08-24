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
import net.nexustools.data.accessor.BaseAccessor;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;

/**
 * A concurrent stage to act upon.
 * 
 * @author katelyn
 */
public abstract class DefaultReadWriteConcurrency<A extends BaseAccessor> implements ReadWriteConcurrency<A> {
	
	final Lockable<A> lock;
	
	protected DefaultReadWriteConcurrency() {
		this(new ReadWriteLock());
	}
	protected DefaultReadWriteConcurrency(Lockable lock) {
		this.lock = lock;
	}
	
	public void write(BaseWriter<A> actor) throws InvocationTargetException {
		lock.write(directAccessor(), actor);
	}

	public <R> R read(BaseReader<R, A> reader) throws InvocationTargetException {
		return (R)lock.read(directAccessor(), reader);
	}

	public Lockable<A> lockable() {
		return lock;
	}
	
}
