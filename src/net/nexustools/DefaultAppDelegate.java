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

import static net.nexustools.Application.defaultName;
import static net.nexustools.Application.defaultOrganization;
import net.nexustools.io.Stream;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public abstract class DefaultAppDelegate<R extends RunQueue> implements AppDelegate {
	public final String name;
	protected final R runQueue;
	private final String organization;
	public DefaultAppDelegate(String[] args) {
		this(args, defaultName(), defaultOrganization());
	}
	public DefaultAppDelegate(String[] args, String name, String organization) {
		this(args, name, organization, (R)RunQueue.current());
	}
	public DefaultAppDelegate(String[] args, R queue) {
		this(args, defaultName(), defaultOrganization(), queue);
	}
	protected DefaultAppDelegate(final String[] args, String name, String organization, R queue) {
		this.name = name;
		this.organization = organization;
		runQueue = queue;
		install();
		
		Logger.installSystem();
		queue.push(new Runnable() {
			public void run() {
				launch(args);
			}
		});
	}
	
	public final R queue() {
		return runQueue;
	}
	
	public final void install() {
		Application.setDelegate(this);
	}
	
	public String pathUri(Path path) {
		switch(path) {
			case Temporary:
				String tempDir = System.getenv("TMPDIR");
				if(tempDir == null)
					tempDir = System.getProperty("java.io.tmpdir");
				return Stream.uriForPath(tempDir);

			case UserHome:
				return Stream.uriForPath(System.getProperty("user.home"));

			case Configuration:
			{
				String userHome = System.getenv("HOME");
				if(userHome == null)
					userHome = System.getProperty("user.home");
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

				return Stream.uriForPath(configPath);
			}
		}
		throw new UnsupportedOperationException();
	}
	
	public final String name() {
		return name;
	}

	public final String organization() {
		return organization;
	}
	
	protected abstract void launch(String[] args);
	
	@Override
	public String toString() {
        return organization + '.' + name;
	}
	
}
