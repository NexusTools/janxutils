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

/**
 *
 * @author katelyn
 */
public interface AppDelegate {
	public static final long created = System.currentTimeMillis();
	
	public static enum Path {
		/**
		 * The current working directory.
		 */
		Working("run"),
		
		/**
		 * The applicable temporary directory.
		 * This may be system wide, or application specific.
		 */
		Temporary("temp"),
		
		/**
		 * The directory where this application is installed.
		 */
		Install("app"),
		
		/**
		 * The configuration directory for this application.
		 */
		Configuration("config"),
		
		/**
		 * The data storage directory for this application.
		 */
		Storage("store"),
		
		/**
		 * The shared data storage directory for this application
		 */
		Share("share"),
		
		/**
		 * The current user's home directory.
		 */
		UserHome("home"),
		
		/**
		 * The current user's documents directory.
		 */
		UserDocuments("docs"),
		
		/**
		 * The current user's pictures directory.
		 */
		UserPictures("pics"),
		
		/**
		 * The current user's music directory.
		 */
		UserMusic("music");
		
		public final String scheme;
		Path(String scheme) {
			this.scheme = scheme;
		}
	}
	
	public static enum Platform {
		Linux,
		Solaris,
		Windows,
		Apple,
		BSD,
		
		OtherUnix,
		Unknown
	}
	
	public static enum Device {
		Desktop,
		Tablet,
		Phone,
		
		Unknown
	}
	
	public abstract String name();
	public abstract String organization();
	public abstract String pathUri(Path path);
	public abstract boolean needsMainLoop();
	public abstract void mainLoop();
	
	public abstract Device device();
	public abstract Platform platform();
	
	public abstract Object deviceObject();
	public abstract Object platformObject();
	
}
