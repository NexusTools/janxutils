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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.logic.IfUpdateWriter;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.Writer;

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
		write(new BaseWriter<ListAccessor<I>>() {
			public void write(ListAccessor<I> data, Lockable lock) {
				operation.run();
			}
		});
	}

	@Override
	public void write(final BaseWriter<ListAccessor<I>> actor) {
		super.write(new BaseWriter<ListAccessor<I>>() {
			public void write(ListAccessor<I> data, Lockable lock) {
				lock.lock(true);
				try {
					dirty = true;
					actor.write(data, lock);
				} finally {
					lock.unlock();
				}
			}
		});
	}

	@Override
	public <R> R read(BaseReader<R, ListAccessor<I>> reader) {
		write(new IfWriter<ListAccessor<I>>() {
			public boolean test(PropAccessor<I> against) {
				return dirty;
			}
			@Override
			public void write(ListAccessor<I> data) {
				Collections.sort(list, comparator);
				dirty = false;
			}
		});
		return super.read(reader);
	}
	
	public Comparator<I> getComparator() {
		return comparator;
	}
	
}