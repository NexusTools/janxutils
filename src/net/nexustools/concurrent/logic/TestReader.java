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

package net.nexustools.concurrent.logic;

import net.nexustools.concurrent.BaseAccessor;

/**
 *
 * @author katelyn
 */
public abstract class TestReader<A extends BaseAccessor> extends IfReader<Boolean, A> {
	@Override
	protected Boolean def() {
		return false;
	}
	
	public abstract void readV(A data);

	@Override
	public final Boolean read(A data) {
		readV(data);
		return true;
	}
	
}
