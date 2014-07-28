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
public class FakeLock extends Lockable {

	public final static FakeLock instance = new FakeLock();
	
	protected FakeLock(){}

	@Override
	public void lock(boolean exclusive) {}

	@Override
	public void upgrade() {}

	@Override
	public void downgrade() {}

	@Override
	public boolean tryFastUpgrade() {
		return true;
	}

	@Override
	public boolean tryLock(boolean write) {
		return true;
	}

	@Override
	public void unlock() {}

	@Override
	public void write(BaseAccessor data, BaseWriter writer) {
		writer.write(data, this);
	}

	public <R> R read(BaseAccessor data, BaseReader reader) {
		return (R)reader.read(data, this);
	}
	
}
