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
import java.util.Comparator;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.data.accessor.GenericListAccessor;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Testable;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public class SortedPropList<I> extends PropList<I> {
	
	private boolean dirty = false;
	private final Comparator<I> comparator;
	public SortedPropList(Comparator<I> comparator) {
		this.comparator = comparator;
	}
	
	public void dirtyOperation(final Runnable operation) {
		try {
			super.write(new BaseWriter<ListAccessor<I>>() {
				public void write(ListAccessor<I> data, Lockable lock) {
					lock.lock(true);
					try {
						dirty = true;
						operation.run();
					} finally {
						lock.unlock();
					}
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	@Override
	public void write(final BaseWriter<ListAccessor<I>> actor) {
		try {
			super.write(new BaseWriter<ListAccessor<I>>() {
				public void write(ListAccessor<I> data, Lockable lock) throws Throwable {
					lock.lock(true);
					try {
						dirty = true;
						actor.write(data, lock);
					} finally {
						lock.unlock();
					}
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	@Override
	public <R> R read(final BaseReader<R, ListAccessor<I>> reader) {
		try {
			return super.read(new BaseReader<R, ListAccessor<I>>() {
				public R read(ListAccessor<I> data, Lockable<ListAccessor<I>> lock) throws Throwable {
					lock.lock();
					try {
						if(lock.tryFastUpgradeTest(new Testable() {
							public boolean test(Object against) {
								return dirty;
							}
						})) {
							Logger.gears("List is dirty, sorting before read");
							data.sort(comparator);
							dirty = false;
						}
						
						return reader.read(data, lock);
					} finally {
						lock.unlock();
					}
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
	public Comparator<I> getComparator() {
		return comparator;
	}
	
}