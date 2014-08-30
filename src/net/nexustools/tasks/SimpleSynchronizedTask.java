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

/**
 *
 * @author katelyn
 */
public abstract class SimpleSynchronizedTask extends SynchronizedTask {

	public SimpleSynchronizedTask() {}

	@Override
	protected void aboutToExecute() {}

	@Override
	protected void onFailure(Throwable reason) {}

	@Override
	protected void onComplete() {}

	@Override
	protected void onSuccess() {}

	@Override
	protected void onCancel() {}
	
}
