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

package nexustools.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author katelyn
 */
public class ConcurrentList<T> extends Accessor<List<T>> implements List<T> {
	
	private final ArrayList<T> list = new ArrayList();
	private final ReadWriteLock lock = new ReadWriteLock();

	@Override
	public int size() {
		try {
			lock.lock();
			return list.size();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		try {
			lock.lock();
			return list.isEmpty();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean contains(Object o) {
		try {
			lock.lock();
			return list.contains(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Iterator<T> iterator() {
		try {
			lock.lock();
			final Iterator<T> it = list.iterator();
			return new Iterator<T>() {

				@Override
				public boolean hasNext() {
					try {
						lock.lock();
						return it.hasNext();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public T next() {
					try {
						lock.lock();
						return it.next();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public void remove() {
					try {
						lock.lock(true);
						it.remove();
					} finally {
						lock.unlock();
					}
				}
			};
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Object[] toArray() {
		try {
			lock.lock();
			return list.toArray();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		try {
			lock.lock();
			return list.toArray(a);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean add(T e) {
		try {
			lock.lock(true);
			return list.add(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean remove(Object o) {
		try {
			lock.lock(true);
			return list.remove(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		try {
			lock.lock();
			return list.containsAll(c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		try {
			lock.lock(true);
			return list.addAll(c);
		} finally {
			lock.unlock();
		}
	
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		try {
			lock.lock(true);
			return list.addAll(index, c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		try {
			lock.lock(true);
			return list.removeAll(c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		try {
			lock.lock(true);
			return list.retainAll(c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		try {
			lock.lock(true);
			list.clear();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T get(int index) {
		try {
			lock.lock();
			return list.get(index);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T set(int index, T element) {
		try {
			lock.lock(true);
			return list.set(index, element);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void add(int index, T element) {
		try {
			lock.lock(true);
			list.add(index, element);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T remove(int index) {
		try {
			lock.lock(true);
			return list.remove(index);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int indexOf(Object o) {
		try {
			lock.lock();
			return list.indexOf(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int lastIndexOf(Object o) {
		try {
			lock.lock();
			return list.lastIndexOf(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ListIterator<T> listIterator() {
		try {
			lock.lock();
			final ListIterator<T> it = list.listIterator();
			return new ListIterator<T>() {

				@Override
				public boolean hasPrevious() {
					try {
						lock.lock();
						return it.hasPrevious();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public T previous() {
					try {
						lock.lock();
						return it.previous();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public int nextIndex() {
					try {
						lock.lock();
						return it.nextIndex();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public int previousIndex() {
					try {
						lock.lock();
						return it.previousIndex();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public void set(T e) {
					try {
						lock.lock(true);
						it.set(e);
					} finally {
						lock.unlock();
					}
				}

				@Override
				public void add(T e) {
					try {
						lock.lock(true);
						it.add(e);
					} finally {
						lock.unlock();
					}
				}

				@Override
				public boolean hasNext() {
					try {
						lock.lock();
						return it.hasNext();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public T next() {
					try {
						lock.lock(true);
						return it.next();
					} finally {
						lock.unlock();
					}
				}

				@Override
				public void remove() {
					try {
						lock.lock(true);
						it.remove();
					} finally {
						lock.unlock();
					}
				}
			};
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isset() {
		return !isEmpty();
	}

	@Override
	public List<T> internal() {
		return list;
	}

	@Override
	public List<T> internal(List<T> object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
