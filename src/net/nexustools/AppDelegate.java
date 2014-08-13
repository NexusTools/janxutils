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

import java.io.File;
import java.util.HashMap;

import net.nexustools.io.Stream;

/**
 *
 * @author katelyn
 */
public class AppDelegate {
	
	public static enum Path {
		Working,
		Application,
		Configuration,
		Temporary,
		
		UserHome,
		UserDocuments,
		UserPictures,
		UserMusic
	}
	
	private final String name;
	private final String organization;
	private static AppDelegate current;
	private HashMap<Path, String> pathCache = new HashMap();
	protected AppDelegate(String name, String organization) {
		assert(current == null);
		current = this;
		
		this.name = name;
		this.organization = organization;
	}
	
	public static AppDelegate i() {
		if(current == null)
			current = new AppDelegate("Untitled Application", "JaNXUtils");
		return current;
	}
	
	public static AppDelegate current() {
		return current;
	}
	
	public static void init(String name, String organization) {
		new AppDelegate(name, organization);
	}
	
	public String pathUri(Path path) {
		String uri = pathCache.get(path);
		if(uri == null) {
			switch(path) {
				case Temporary:
					uri = Stream.uriForPath(System.getProperty("java.io.tmpdir"));
					break;
					
				case UserHome:
					uri = Stream.uriForPath(System.getProperty("user.home"));
					break;
					
				case Configuration:
				{
					String userHome = System.getProperty("user.home");
					if(!userHome.endsWith(File.separator))
						userHome += File.separator;
					
					boolean hasOrg = false;
					String configPath = userHome + "Library" + File.separator + "Application Support";
					if(!(new File(configPath)).isDirectory()) {
						configPath = userHome + "Application Data";
						if(!(new File(configPath)).isDirectory())
							configPath = userHome + ".config";
						else {
							configPath = userHome + "." + organization.toLowerCase();
							hasOrg = true;
						}
					}
					if(!hasOrg)
						configPath += File.separator + organization;
					configPath += File.separator + name;
					
					uri = Stream.uriForPath(configPath);
				}
				break;
					
				default:
					throw new UnsupportedOperationException();
			}
			pathCache.put(path, uri);
		}
		return uri;
	}
	
}
