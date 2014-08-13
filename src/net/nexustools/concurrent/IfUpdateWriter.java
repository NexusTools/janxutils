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

import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 */
public abstract class IfUpdateWriter<A extends BaseAccessor> implements BaseWriter<A>, Testable<A> {

	@Override
	public final void write(A data, Lockable lock) {
		lock.lock();
		try {
			if(test(data)) {
				if(!lock.tryFastUpgrade()) {
					lock.upgrade();
					if(!test(data))
						return;
				}
				write(data);
				lock.downgrade();
				update();
			}
		} finally {
			lock.unlock();
		}
	}
	public abstract void write(A data);
	public abstract void update();
	
}
