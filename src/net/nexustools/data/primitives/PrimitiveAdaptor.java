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

package net.nexustools.data.primitives;

import java.io.IOException;
import net.nexustools.io.DataInputStream;
import net.nexustools.data.adaptor.Adaptor;

/**
 *
 * @author katelyn
 * 
 * @param <T> The primitive class this adaptor is used to read/write
 */
public abstract class PrimitiveAdaptor<T> extends Adaptor<T> {

	@Override
	public abstract T createInstance(DataInputStream in, Class<? extends T> target) throws IOException;

	@Override
	public final T readInstance(DataInputStream in, Class<? extends T> target) throws IOException {
		return createInstance(in, target);
	}

	@Override
	public final void read(T target, DataInputStream in) throws IOException {
		throw new UnsupportedOperationException("Cannot read primitives, use readInstance instead");
	}

}
