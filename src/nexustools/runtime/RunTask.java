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

package nexustools.runtime;

/**
 *
 * @author katelyn
 */
public abstract class RunTask implements Runnable {
	
	/**
	 * Used during the run method to check whether or
	 * not this task has been cancelled.
	 * 
	 * @return 
	 */
	protected boolean isCancelled() {
		return false; // TODO: Implement
	}
	
}
