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
import net.nexustools.data.accessor.ListAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.utils.NXUtils;

/**
 *
 * @author katelyn
 * @param <I>
 */
public class PropList<I> extends DefaultReadWriteConcurrency<ListAccessor<I>> implements ListAccessor<I> {

	protected List<I> list;
	private final ListAccessor<I> directAccessor = new ListAccessor<I>() {
		public void push(I object) {
			list.add(object);
		}
		public void unshift(I object) {
			insert(object, 0);
		}
		public void insert(I object, int at) {
			list.add(at, object);
		}
		public void remove(I object) {
			list.remove(object);
		}
		public I remove(int at) {
			return list.remove(at);
		}
		public int indexOf(I object) {
			return indexOf(object, 0);
		}
		public int indexOf(I object, int from) {
			ListIterator<I> listIterator = list.listIterator(from);
			try {
				while(true) {
					int index = listIterator.nextIndex();
					if(listIterator.next().equals(object))
						return index;
				}
			} catch(NoSuchElementException ex) {
				return -1;
			}
		}
		public int lastIndexOf(I object, int from) {
			ListIterator<I> listIterator = list.listIterator(from);
			try {
				while(true) {
					int index = listIterator.previousIndex();
					if(index == -1 || listIterator.previous().equals(object))
						return index;
				}
			} catch(NoSuchElementException ex) {
				return -1;
			}
		}
		public int lastIndexOf(I object) {
			return lastIndexOf(object, list.size());
		}
		public int length() {
			return list.size();
		}
		public I shift() {
			try {
				return list.remove(0);
			} catch(IndexOutOfBoundsException ex) {
				return null;
			}
		}
		public I pop() {
			try {
				return list.remove(list.size()-1);
			} catch(IndexOutOfBoundsException ex) {
				return null;
			}
		}
		public boolean isTrue() {
			return list.size() > 0;
		}
		public boolean isset() {
			return true;
		}
		public void clear() {
			list.clear();
		}
		public Iterator<I> iterator() {
			return list.iterator();
		}
		public List<I> copy() {
			return new ArrayList(list);
		}
		public List<I> take() {
			try {
				return list;
			} finally {
				list = new ArrayList();
			}
		}
		public boolean unique(I object) {
			if(contains(object))
				return false;
			
			list.add(object);
			return true;
		}
		public boolean contains(I object) {
			return list.contains(object);
		}
		public I first() {
			return list.get(0);
		}
		public I get(int at) {
			return list.get(at);
		}
		public I last() {
			return list.get(list.size()-1);
		}

		public ListIterator<I> listIterator() {
			return list.listIterator();
		}

		public void pushAll(Iterable<I> objects) {
			if(objects instanceof List) {
				list.addAll((List)objects);
				return;
			}
			for(I obj : objects)
				push(obj);
		}

		public void unshiftAll(Iterable<I> objects) {
			List<I> current = list;
			list = new ArrayList<I>();
			if(objects instanceof List)
				list.addAll((List)objects);
			else
				for(I obj : objects)
					unshift(obj);
			list.addAll(current);
		}
	};
	public PropList(I... items) {
		this(Arrays.asList(items));
	}
	public PropList(Collection<I> items) {
		list = new ArrayList(items);
	}
	public PropList() {
		list = new ArrayList();
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
	public List<I> copy() {
		try {
			return read(new Reader<List<I>, ListAccessor<I>>() {
				@Override
				public List<I> read(ListAccessor<I> data) {
					return data.copy();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	public List<I> take() {
		try {
			return read(new WriteReader<List<I>, ListAccessor<I>>() {
				@Override
				public List<I> read(ListAccessor<I> data) {
					return data.take();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
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
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	
	public static interface PropIterator<I> {
		public void iterate(ListIterator<I> iterator, Lockable lock);
	}
	
}
