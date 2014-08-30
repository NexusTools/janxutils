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

package net.nexustools.tasks;

import java.lang.reflect.InvocationTargetException;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.SoftMapReader;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Pair;

/**
 *
 * @author katelyn
 */
public class RunTask extends EventableSynchronizedTask {
	
	private static final PropMap<Runnable, RunTask> uniqueWraps = new PropMap(PropMap.Type.WeakHashMap);
	public static RunTask unique(final Runnable run) {
		return uniqueWraps.read(new SoftMapReader<Runnable, RunTask>(run) {
			@Override
			protected RunTask create(Runnable key) {
				return new RunTask(key);
			}
		});
	}
	
	protected final Runnable block;
	public RunTask(Runnable block) {
		this.block = block;
	}
	
	public Runnable block() {
		return block;
	}

	@Override
	protected void execute() {
		block.run();
	}

	@Override
	protected void aboutToExecute() {
		// TODO: Add EventDispatcher
	}

	@Override
	protected void onFailure(Throwable reason) {
		// TODO: Add EventDispatcher
	}

	@Override
	protected void onComplete() {
		// TODO: Add EventDispatcher
	}

	@Override
	protected void onSuccess() {
		// TODO: Add EventDispatcher
	}

	@Override
	protected void onCancel() {
		// TODO: Add EventDispatcher
	}

	@Override
	public String toString() {
		return NXUtils.toString(this, new Pair("block", block));
	}
	
}
