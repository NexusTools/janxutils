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

package net.nexustools.runtime;

import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.runtime.logic.Task;

/**
 *
 * @author kate
 */
public class FCFSTaskDelegator<F extends Task> implements FutureDelegator<F> {

	public F nextTask(ListAccessor<F> queue) {
		return queue.shift();
	}
	
}
