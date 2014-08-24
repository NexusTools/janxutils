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
import net.nexustools.data.accessor.BaseAccessor;
import net.nexustools.concurrent.logic.BaseReader;
import net.nexustools.concurrent.logic.BaseWriter;

/**
 *
 * @author katelyn
 */
public interface ConcurrentStage<A extends BaseAccessor> {
	
	public void write(A data, BaseWriter<A> actor) throws InvocationTargetException;
	public <R> R read(A data, BaseReader<R, A> reader) throws InvocationTargetException;
	
}
