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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.concurrent.logic.BaseWriter;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.UpdateReader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 * @param <T>
 */
public abstract class AbstractProp<T> extends DefaultReadWriteConcurrency<PropAccessor<T>> implements PropAccessor<T> {

	protected T value;
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

		return true;
	}
	public AbstractProp() {}
	AbstractProp(Lockable lock, T value) {
		super(lock);
		this.value = value;
	}
	public AbstractProp(T value) {
		this.value = value;
	}

	public boolean isset() {
		try {
			return read(new Reader<Boolean, PropAccessor<T>>() {
				@Override
				public Boolean read(PropAccessor<T> data) {
					return data.isset();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public void clear() {
		try {
			write(new Writer<PropAccessor<T>>() {
				@Override
				public void write(PropAccessor<T> data) {
					data.clear();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public T get() {
		try {
			return read(new Reader<T, PropAccessor<T>>() {
				@Override
				public T read(PropAccessor<T> data) {
					return data.get();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public void set(final T value) {
		try {
			write(new Writer<PropAccessor<T>>() {
				@Override
				public void write(PropAccessor<T> data) {
					data.set(value);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public boolean isTrue() {
		try {
			return read(new Reader<Boolean, PropAccessor<T>>() {
				@Override
				public Boolean read(PropAccessor<T> data) {
					return data.isTrue();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	@Override
	public abstract PropAccessor<T> directAccessor();

	public T take() {
		try {
			return read(new WriteReader<T, PropAccessor<T>>() {
				@Override
				public T read(PropAccessor<T> data) {
					return data.take();
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
	public boolean update(final T newValue) {
		try {
			return read(new UpdateReader<PropAccessor<T>>() {
				@Override
				public void update(PropAccessor<T> data) {
					data.set(newValue);
				}
				@Override
				public boolean needUpdate(PropAccessor<T> against) {
					return !newValue.equals(against.get());
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}

	public void set(final T value, final Testable<T> test) {
		try {
			write(new BaseWriter<PropAccessor<T>>() {
				public void write(PropAccessor<T> data, Lockable lock) throws Throwable {
					lock.lock();
					try {
						if(lock.fastUpgradeTest(test))
							data.set(value);
					} finally {
						lock.unlock();
					}
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		}
	}
	
}
