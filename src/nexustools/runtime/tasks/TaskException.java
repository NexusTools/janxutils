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

package nexustools.runtime.tasks;

import nexustools.runtime.RuntimeException;

/**
 *
 * @author katelyn
 */
public class TaskException extends RuntimeException {
	
	public TaskException(String title, String reason) {
		super(title, reason);
	}
	
	public TaskException(String title, Exception reason) {
		super(title, reason);
	}
	
}
