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

import java.util.Collection;

/**
 *
 * @author katelyn
 * @param <T>
 */
public class Prop<T> extends PropConcurrency<PropAccessor<T>> implements PropAccessor<T> {

	private T value;
	private final PropAccessor<T> directAccessor = new PropAccessor<T>() {
		public T get() {
			return value;
		}
		public void set(T val) {
			value = val;
		}
		public boolean isset() {
			return value != null;
		}
		public void clear() {
			value = null;
		}
		public boolean isTrue() {
			return isTrueHeiristic(get());
		}
		public T take() {
			try {
				return value;
			} finally {
				value = null;
			}
		}
	};
	public static <T> boolean isTrueHeiristic(T value) {
		if(value == null)
			return false;

		if(value instanceof String)
			return ((String)value).length() > 0;
		if(value instanceof Boolean)
			return ((Boolean)value);
		if(value instanceof Double)
			return ((Double)value) != 0;
		if(value instanceof Float)
			return ((Float)value) != 0;
		if(value instanceof Byte)
			return ((Byte)value) != 0;
		if(value instanceof Short)
			return ((Short)value) != 0;
		if(value instanceof Integer)
			return ((Integer)value) != 0;
		if(value instanceof Long)
			return ((Long)value) != 0;
		if(value instanceof Collection)
			return ((Collection)value).size() > 0;

		return false;
	}
	public Prop() {}
	public Prop(T value) {
		this.value = value;
	}

	public boolean isset() {
		return read(new Reader<Boolean, PropAccessor<T>>() {
			@Override
			public Boolean read(PropAccessor<T> data) {
				return data.isset();
			}
		});
	}

	public void clear() {
		write(new Writer<PropAccessor<T>>() {
			@Override
			public void write(PropAccessor<T> data) {
				data.clear();
			}
		});
	}

	public T get() {
		return read(new Reader<T, PropAccessor<T>>() {
			@Override
			public T read(PropAccessor<T> data) {
				return data.get();
			}
		});
	}

	public void set(final T value) {
		write(new Writer<PropAccessor<T>>() {
			@Override
			public void write(PropAccessor<T> data) {
				data.set(value);
			}
		});
	}

	public boolean isTrue() {
		return isTrueHeiristic(get());
	}

	@Override
	protected PropAccessor<T> directAccessor() {
		return directAccessor;
	}

	public T take() {
		return read(new WriteReader<T, PropAccessor<T>>() {
			@Override
			public T read(PropAccessor<T> data) {
				return data.take();
			}
		});
	}
	
}
