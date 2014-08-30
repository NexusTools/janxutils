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
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.data.annote.ThreadUnsafe;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Testable;

/**
 *
 * @author katelyn
 * @param <T>
 */
@ThreadUnsafe
public class Prop<T> extends AbstractProp<T> {

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
		public void set(T val, Testable<T> test) {
			try {
				if(test.test(val))
					value = val;
			} catch (Throwable ex) {
				throw NXUtils.wrapRuntime(ex);
			}
		}
		public boolean update(final T val) {
			if(value != val) {
				value = val;
				return true;
			}
			return false;
		}
	};
	public Prop() {}
	Prop(Lockable lock, T value) {
		super(lock, value);
	}
	public Prop(T value) {
		super(value);
	}

	@Override
	public PropAccessor<T> directAccessor() {
		return directAccessor;
	}

	@Override
	public String toString() {
		return read(new Reader<String, PropAccessor<T>>() {
			@Override
			public String read(PropAccessor<T> data) {
				return data.toString();
			}
		});
	}

}
