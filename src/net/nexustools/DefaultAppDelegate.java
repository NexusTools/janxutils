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
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.ThreadCondition;
import net.nexustools.io.Stream;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public abstract class DefaultAppDelegate<S extends TaskSink> implements AppDelegate {
	public final String name;
	protected final S taskSink;
	private final String organization;
	private final Prop<Runnable> mainLoop = new Prop();
	private final ThreadCondition hasMainLoop = new ThreadCondition();
	private final Platform platform;
	
	public DefaultAppDelegate(String[] args, S queue) {
		this(args, defaultName(), defaultOrganization(), queue);
	}
	protected DefaultAppDelegate(final String[] args, String name, String organization, S queue) {
		this.name = name;
		this.organization = organization;
		taskSink = queue;
		
		String OS = System.getProperty("os.name").toLowerCase();
		if(OS.contains("sunos"))
			platform = Platform.Solaris;
		else if(OS.contains("mac") || OS.contains("ios"))
			platform = Platform.Apple;
		else if(OS.contains("linux"))
			platform = Platform.Linux;
		else if(OS.contains("windows"))
			platform = Platform.Windows;
		else if(OS.contains("bsd"))
			platform = Platform.BSD;
		else if(OS.contains("nix") || OS.contains("nux") || OS.contains("aix"))
			platform = Platform.OtherUnix;
		else
			platform = Platform.Unknown;
		
		queue.push(new Runnable() {
			public void run() {
				Application.setDelegateIfNone(DefaultAppDelegate.this);
				Runnable main = null;
				try {
					main = launch(args);
					if(main == null) {
						Logger.warn("Launched but Missing MainLoop", DefaultAppDelegate.this);
						main = new Runnable() {
							public void run() {
								throw new UnsupportedOperationException("No main loop returned by " + getClass().getSimpleName() + ".launch");
							}
						};
					} else
						Logger.info("Launched", DefaultAppDelegate.this);
				} catch (final Throwable t) {
					main = new Runnable() {
						public void run() {}
					};
					throw NXUtils.wrapRuntime(t);
				} finally {
					assert(main != null);
					mainLoop.set(main);
					hasMainLoop.finish();
				}
			}
			@Override
			public String toString() {
				return name() + "LauncherTask";
			}
		});
	}

	public final void mainLoop() {
		Application.setDelegate(this);
		if(!mainLoop.isset()) {
			Logger.debug("Waiting on MainLoop", DefaultAppDelegate.this);
			hasMainLoop.waitForUninterruptibly();
		}
		
		Logger.debug("Entering MainLoop", DefaultAppDelegate.this);
		try {
			mainLoop.get().run();
		} finally {
			Logger.shutdownAndWait();
		}
	}
	
	public final S queue() {
		return taskSink;
	}
	
	public String pathUri(Path path) {
		switch(path) {
			case Working:
				return Stream.uriForPath(System.getProperty("user.dir"));
				
			case Temporary:
				String tempDir = System.getenv("TMPDIR");
				if(tempDir == null)
					tempDir = System.getProperty("java.io.tmpdir");
				return Stream.uriForPath(tempDir);

			case UserHome:
				String homeDir = System.getenv("HOME");
				if(homeDir == null)
					homeDir = Stream.uriForPath(System.getProperty("user.home"));
				return homeDir;

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
	
	protected abstract Runnable launch(String[] args) throws Throwable;
	
	public Device device() {
		return Device.Desktop;
	}
	public Platform platform() {
		return platform;
	}
	
	public Object deviceObject() {
		return device();
	}
	public Object platformObject() {
		return platform();
	}
	
	@Override
	public String toString() {
        return organization + '.' + name;
	}
	
}
