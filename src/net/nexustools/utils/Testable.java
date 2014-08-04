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

package net.nexustools.utils;

/**
 *
 * @author katelyn
 */
public interface Testable<I> {
	
	public static final Testable TRUE = new Testable() {
		public boolean test(Object against) {
			return true;
		}
	};
	public static final Testable FALSE = new Testable() {
		public boolean test(Object against) {
			return false;
		}
	};
	
	public boolean test(I against);
	
}
