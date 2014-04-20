/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data.primitives;

import nexustools.io.DataInputStream;
import java.io.IOException;
import nexustools.io.data.Adaptor;

/**
 *
 * @author katelyn
 * 
 * @param <T> The primitive class this adaptor is used to read/write
 */
public abstract class PrimitiveAdaptor<T> extends Adaptor<T> {

	@Override
	public abstract T createInstance(DataInputStream in) throws IOException;

	@Override
	public final T readInstance(DataInputStream in) throws IOException {
		return createInstance(in);
	}

	@Override
	public final void read(T target, DataInputStream in) throws IOException {
		throw new UnsupportedOperationException("Cannot read primitives, use readInstance instead");
	}

}
