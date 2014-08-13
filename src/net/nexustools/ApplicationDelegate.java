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

package net.nexustools;

import net.nexustools.io.Stream;

/**
 *
 * @author katelyn
 */
public class ApplicationDelegate {
	
	private final String name;
	private final String organization;
	public static ApplicationDelegate current;
	private ApplicationDelegate(String name, String organization) {
		this.name = name;
		this.organization = organization;
	
	}
	
	public static void init(String name, String organization) {
		Stream.initAppAliases(name, organization);
		current = new ApplicationDelegate(name, organization);
	}
	
}
