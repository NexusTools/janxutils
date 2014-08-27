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
		try {
			return read(new Reader<Boolean, ListAccessor<I>>() {
				@Override
				public Boolean read(ListAccessor<I> data) {
					return data.isTrue();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public void clear() {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.clear();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	// TODO: Make more effecient by checking if it exists before obtaining a write lock
	public boolean unique(final I object) {
		try {
			return read(new WriteReader<Boolean, ListAccessor<I>>() {
				@Override
				public Boolean read(ListAccessor<I> data) {
					return data.unique(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public void push(final I object) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.push(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public void unshift(final I object) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.unshift(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public void insert(final I object, final int at) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.insert(object, at);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public void remove(final I object) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.remove(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public I remove(final int at) {
		try {
			return read(new WriteReader<I, ListAccessor<I>>() {
				@Override
				public I read(ListAccessor<I> data) {
					return data.remove(at);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public int indexOf(final I object) {
		try {
			return read(new Reader<Integer, ListAccessor<I>>() {
				@Override
				public Integer read(ListAccessor<I> data) {
					return data.indexOf(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public int indexOf(final I object, final int from) {
		try {
			return read(new Reader<Integer, ListAccessor<I>>() {
				@Override
				public Integer read(ListAccessor<I> data) {
					return data.indexOf(object, from);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public int lastIndexOf(final I object, final int from) {
		try {
			return read(new Reader<Integer, ListAccessor<I>>() {
				@Override
				public Integer read(ListAccessor<I> data) {
					return data.lastIndexOf(object, from);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public int lastIndexOf(final I object) {
		try {
			return read(new Reader<Integer, ListAccessor<I>>() {
				@Override
				public Integer read(ListAccessor<I> data) {
					return data.lastIndexOf(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public int length() {
		try {
			return read(new Reader<Integer, ListAccessor<I>>() {
				@Override
				public Integer read(ListAccessor<I> data) {
					return data.length();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public I shift() {
		try {
			return read(new WriteReader<I, ListAccessor<I>>() {
				@Override
				public I read(ListAccessor<I> data) {
					return data.shift();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	public I pop() {
		try {
			return read(new WriteReader<I, ListAccessor<I>>() {
				@Override
				public I read(ListAccessor<I> data) {
					return data.pop();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
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
		try {
			return read(new Reader<Boolean, ListAccessor<I>>() {
				@Override
				public Boolean read(ListAccessor<I> data) {
					return data.contains(object);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public I first() {
		try {
			return read(new Reader<I, ListAccessor<I>>() {
				@Override
				public I read(ListAccessor<I> data) {
					return data.first();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public I get(final int at) {
		try {
			return read(new Reader<I, ListAccessor<I>>() {
				@Override
				public I read(ListAccessor<I> data) {
					return data.get(at);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public I last() {
		try {
			return read(new Reader<I, ListAccessor<I>>() {
				@Override
				public I read(ListAccessor<I> data) {
					return data.last();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public void iterate(final PropIterator<I> iterator) {
		try {
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
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public ListIterator<I> listIterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void pushAll(final Iterable<I> objects) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.pushAll(objects);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public void unshiftAll(final Iterable<I> objects) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) {
					data.unshiftAll(objects);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public ListIterator<I> listIterator(int where) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public List<I> toList() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void sort(Comparator<I> sortMethod) {
		try {
			write(new Writer<ListAccessor<I>>() {
				@Override
				public void write(ListAccessor<I> data) throws Throwable {
					throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public ListAccessor<I> take(final Testable<I> shouldTake) {
		try {
			return read(new WriteReader<ListAccessor<I>, ListAccessor<I>>() {
				@Override
				public ListAccessor<I> read(ListAccessor<I> data) throws Throwable {
					return data.take(shouldTake);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public ListAccessor<I> copy(final Testable<I> shouldCopy) {
		try {
			return read(new Reader<ListAccessor<I>, ListAccessor<I>>() {
				@Override
				public ListAccessor<I> read(ListAccessor<I> data) throws Throwable {
					return data.copy(shouldCopy);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public ListAccessor<I> copy() {
		try {
			return read(new Reader<ListAccessor<I>, ListAccessor<I>>() {
				@Override
				public ListAccessor<I> read(ListAccessor<I> data) throws Throwable {
					return data.copy();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public ListAccessor<I> take() {
		try {
			return read(new WriteReader<ListAccessor<I>, ListAccessor<I>>() {
				@Override
				public ListAccessor<I> read(ListAccessor<I> data) throws Throwable {
					return data.take();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
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
