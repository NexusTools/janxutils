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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.DataAccessor.Reference;
import net.nexustools.data.accessor.GenericListAccessor;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 * @param <I>
 */
public class PropList<I> extends DefaultReadWriteConcurrency<ListAccessor<I>> implements ListAccessor<I> {

	private final ListAccessor<I> directAccessor;
	public PropList(ListAccessor<I> accessor) {
		directAccessor = accessor;
	}
	public PropList(Reference reference, I... items) {
		switch(reference) {
			case Strong:
				directAccessor = new StrongTypeList<I>(items);
				return;
				
			case Weak:
				directAccessor = new StrongTypeList<I>(items);
				return;
				
			case Soft:
				directAccessor = new StrongTypeList<I>(items);
				return;
		}
		throw new UnsupportedOperationException();
	}
	public PropList(I... items) {
		this(new StrongTypeList<I>(items));
	}
	public PropList(Collection<I> items) {
		this(new GenericListAccessor(items));
	}
	public boolean isTrue() {
		return read(new Reader<Boolean, ListAccessor<I>>() {
			@Override
			public Boolean read(ListAccessor<I> data) {
				return data.isTrue();
			}
		});
	}
	public void clear() {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.clear();
			}
		});
	}
	// TODO: Make more effecient by checking if it exists before obtaining a write lock
	public boolean unique(final I object) {
		return read(new WriteReader<Boolean, ListAccessor<I>>() {
			@Override
			public Boolean read(ListAccessor<I> data) {
				return data.unique(object);
			}
		});
	}
	public void push(final I object) {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.push(object);
			}
		});
	}
	public void unshift(final I object) {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.unshift(object);
			}
		});
	}
	public void insert(final I object, final int at) {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.insert(object, at);
			}
		});
	}
	public void remove(final I object) {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.remove(object);
			}
		});
	}
	public I remove(final int at) {
		return read(new WriteReader<I, ListAccessor<I>>() {
			@Override
			public I read(ListAccessor<I> data) {
				return data.remove(at);
			}
		});
	}
	public int indexOf(final I object) {
		return read(new Reader<Integer, ListAccessor<I>>() {
			@Override
			public Integer read(ListAccessor<I> data) {
				return data.indexOf(object);
			}
		});
	}
	public int indexOf(final I object, final int from) {
		return read(new Reader<Integer, ListAccessor<I>>() {
			@Override
			public Integer read(ListAccessor<I> data) {
				return data.indexOf(object, from);
			}
		});
	}
	public int lastIndexOf(final I object, final int from) {
		return read(new Reader<Integer, ListAccessor<I>>() {
			@Override
			public Integer read(ListAccessor<I> data) {
				return data.lastIndexOf(object, from);
			}
		});
	}
	public int lastIndexOf(final I object) {
		return read(new Reader<Integer, ListAccessor<I>>() {
			@Override
			public Integer read(ListAccessor<I> data) {
				return data.lastIndexOf(object);
			}
		});
	}
	public int length() {
		return read(new Reader<Integer, ListAccessor<I>>() {
			@Override
			public Integer read(ListAccessor<I> data) {
				return data.length();
			}
		});
	}
	public I shift() {
		return read(new WriteReader<I, ListAccessor<I>>() {
			@Override
			public I read(ListAccessor<I> data) {
				return data.shift();
			}
		});
	}
	public I pop() {
		return read(new WriteReader<I, ListAccessor<I>>() {
			@Override
			public I read(ListAccessor<I> data) {
				return data.pop();
			}
		});
	}
	public Iterator<I> iterator() {
		return new Iterator<I>() {
			Iterator<I> copy = copy().iterator();
			public boolean hasNext() {
				return copy.hasNext();
			}
			public I next() {
				return copy.next();
			}
			public void remove() {
				throw new UnsupportedOperationException("PropList Iterators are not concurrent, use the iterate method for more complex iterations.");
			}
		};
	}

	@Override
	public ListAccessor<I> directAccessor() {
		return directAccessor;
	}
	
	public boolean contains(final I object) {
		return read(new Reader<Boolean, ListAccessor<I>>() {
			@Override
			public Boolean read(ListAccessor<I> data) {
				return data.contains(object);
			}
		});
	}

	public I first() {
		return read(new Reader<I, ListAccessor<I>>() {
			@Override
			public I read(ListAccessor<I> data) {
				return data.first();
			}
		});
	}

	public I get(final int at) {
		return read(new Reader<I, ListAccessor<I>>() {
			@Override
			public I read(ListAccessor<I> data) {
				return data.get(at);
			}
		});
	}

	public I last() {
		return read(new Reader<I, ListAccessor<I>>() {
			@Override
			public I read(ListAccessor<I> data) {
				return data.last();
			}
		});
	}
	
	public void iterate(final PropIterator<I> iterator) {
		write(new BaseWriter<ListAccessor<I>>() {
			public void write(ListAccessor<I> data, Lockable lock) {
				lock.lock();
				try {
					iterator.iterate(data.listIterator(), lock);
				} finally {
					lock.unlock();
				}
			}
		});
	}

	public ListIterator<I> listIterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void pushAll(final Iterable<I> objects) {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.pushAll(objects);
			}
		});
	}

	public void unshiftAll(final Iterable<I> objects) {
		write(new Writer<ListAccessor<I>>() {
			@Override
			public void write(ListAccessor<I> data) {
				data.unshiftAll(objects);
			}
		});
	}

	public ListIterator<I> listIterator(int where) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public List<I> toList() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void sort(Comparator<I> sortMethod) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public ListAccessor<I> take(final Testable<I> shouldTake) {
		return read(new WriteReader<ListAccessor<I>, ListAccessor<I>>() {
			@Override
			public ListAccessor<I> read(ListAccessor<I> data) {
				return data.take(shouldTake);
			}
		});
	}

	public ListAccessor<I> copy(final Testable<I> shouldCopy) {
		return read(new Reader<ListAccessor<I>, ListAccessor<I>>() {
			@Override
			public ListAccessor<I> read(ListAccessor<I> data) {
				return data.copy(shouldCopy);
			}
		});
	}

	public ListAccessor<I> copy() {
		return read(new Reader<ListAccessor<I>, ListAccessor<I>>() {
			@Override
			public ListAccessor<I> read(ListAccessor<I> data) {
				return data.copy();
			}
		});
	}

	public ListAccessor<I> take() {
		return read(new WriteReader<ListAccessor<I>, ListAccessor<I>>() {
			@Override
			public ListAccessor<I> read(ListAccessor<I> data) {
				return data.take();
			}
		});
	}

	public I[] toArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Object[] toObjectArray() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	public static interface PropIterator<I> {
		public void iterate(ListIterator<I> iterator, Lockable lock);
	}
	
}
