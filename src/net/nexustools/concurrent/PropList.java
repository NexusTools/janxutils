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

import net.nexustools.data.accessor.ListAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;

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
	public List<I> copy() {
		return read(new Reader<List<I>, ListAccessor<I>>() {
			@Override
			public List<I> read(ListAccessor<I> data) {
				return data.copy();
			}
		});
	}
	public List<I> take() {
		return read(new WriteReader<List<I>, ListAccessor<I>>() {
			@Override
			public List<I> read(ListAccessor<I> data) {
				return data.take();
			}
		});
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
	
	public static interface PropIterator<I> {
		public void iterate(ListIterator<I> iterator, Lockable lock);
	}
	
}
