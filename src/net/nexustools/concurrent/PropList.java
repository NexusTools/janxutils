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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author katelyn
 * @param <I>
 */
public class PropList<I> extends DefaultReadWriteConcurrency<ListAccessor<I>> implements ListAccessor<I> {

	private List<I> list;
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
			while(true) {
				int index = listIterator.nextIndex();
				if(index == -1 || listIterator.next().equals(object))
					return index;
			}
		}
		public int lastIndexOf(I object, int from) {
			ListIterator<I> listIterator = list.listIterator(from);
			while(true) {
				int index = listIterator.previousIndex();
				if(index == -1 || listIterator.previous().equals(object))
					return index;
			}
		}
		public int lastIndexOf(I object) {
			return lastIndexOf(object, list.size());
		}
		public int length() {
			return list.size();
		}
		public I shift() {
			return list.remove(0);
		}
		public I pop() {
			return list.remove(list.size()-1);
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
	public boolean isset() {
		return read(new Reader<Boolean, ListAccessor<I>>() {
			@Override
			public Boolean read(ListAccessor<I> data) {
				return data.isset();
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
	
}
