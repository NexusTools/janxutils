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

/**
 *
 * @author katelyn
 */
public abstract class UpdateReader<A extends BaseAccessor> extends IfWriteReader<Boolean, A> {

	@Override
	public final boolean test(A against) {
		try {
			return needUpdate(against);
		} catch(NullPointerException ex) {
			return true;
		}
	}
	
	@Override
	public final Boolean def() {
		return false;
	}

	@Override
	public final Boolean read(A data) {
		update(data);
		return null;
	}

	public abstract void update(A data);
	public abstract boolean needUpdate(A against);
	
}
