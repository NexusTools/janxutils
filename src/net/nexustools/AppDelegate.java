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
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.io.Stream;
import static net.nexustools.io.Stream.bindSynthScheme;
import static net.nexustools.io.Stream.uriForPath;
import net.nexustools.runtime.RunQueue;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public abstract class AppDelegate<R extends RunQueue> {
	public static final long created = System.currentTimeMillis();
		
	protected static String str(long time, int len) {
		String val = String.valueOf(time);
		while(val.length() < len)
			val = '0' + val;

		return val;
	}
	
	public static String uptime() {
		return uptime(System.currentTimeMillis());
	}
		
	public static String uptime(long timestamp) {
		long millis = timestamp - AppDelegate.created;

		long seconds = millis / 1000;
		millis -= seconds*1000;

		long minutes = seconds / 60;
		seconds -= minutes*60;

		long hours = minutes / 60;
		minutes -= hours*60;

		StringBuilder builder = new StringBuilder();
		if(hours >= 24) {
			long days = hours / 24;
			hours -= days*24;

			if(days >= 365) {
				long years = days / 365;
				days -= years*365;
				builder.append(years);
				builder.append('.');
			}
			builder.append(str(days, 3));
			builder.append(' ');
		}

		builder.append(str(hours, 2));
		builder.append(':');
		builder.append(str(minutes, 2));
		builder.append(':');
		builder.append(str(seconds, 2));
		builder.append('.');
		builder.append(str(millis, 3));
		return builder.toString();
	}
	
	public static enum Path {
		Working("run"),
		Application("app"),
		Temporary("temp"),
		Configuration("config"),
		Storage("store"),
		
		UserHome("home"),
		UserDocuments("docs"),
		UserPictures("pics"),
		UserMusic("music");
		
		public final String scheme;
		Path(String scheme) {
			this.scheme = scheme;
		}
	}
	
	private final R runQueue;
	private final String name;
	private final String organization;
	private static final Prop<AppDelegate> current = new Prop();
	private final HashMap<Path, String> pathCache = new HashMap();
	public AppDelegate(String[] args) {
		this(args, defaultName(), defaultOrganization(), (R)RunQueue.current());
	}
	public AppDelegate(String[] args, R queue) {
		this(args, defaultName(), defaultOrganization(), queue);
	}
	protected AppDelegate(final String[] args, String name, String organization, R queue) {
		this.name = name;
		this.organization = organization;
		runQueue = queue;
		makeCurrent();
		
		Logger.installSystemIO();
		queue.push(new Runnable() {
			public void run() {
				launch(args);
			}
		});
	}
	
	public final R queue() {
		return runQueue;
	}
	
	public final void makeCurrent() {
		current.write(new Writer<PropAccessor<AppDelegate>>() {
			@Override
			public void write(PropAccessor<AppDelegate> data) {
				Logger.debug("Switching To", AppDelegate.this);
				
				data.set(AppDelegate.this);
				for(AppDelegate.Path path : AppDelegate.Path.values())
					try {
						bindSynthScheme(path.scheme, uriForPath(AppDelegate.i().pathUri(path)));
					} catch(Throwable t) {
						Logger.warn(path.scheme + "://", "is not supported by this application delegate.");
						Stream.remove(path.scheme);
					}
			}
		});
	}
	
	public static String defaultName() {
		return System.getProperty("appname", "Untitled Application");
	}
	
	public static String defaultOrganization() {
		return System.getProperty("apporg", "NexusTools");
	}
	
	public static AppDelegate i() {
		return current.read(new SoftWriteReader<AppDelegate, PropAccessor<AppDelegate>>() {
			@Override
			public AppDelegate soft(PropAccessor<AppDelegate> data) {
				return data.get();
			}
			@Override
			public AppDelegate read(PropAccessor<AppDelegate> data) {
				AppDelegate appDelegate;
				try {
					data.set(appDelegate = (AppDelegate) Class.forName(System.getProperty("appdelegate", AppDelegate.class.getName())).newInstance());
				} catch (ClassNotFoundException ex) {
					throw new RuntimeException(ex);
				} catch (InstantiationException ex) {
					throw new RuntimeException(ex);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				}
				return appDelegate;
			}
		});
	}
	
	public static AppDelegate current() {
		return current.get();
	}
	
	public final String pathUri(Path path) {
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
	
	public final String name() {
		return name;
	}

	public final String organization() {
		return organization;
	}
	
	protected abstract void launch(String[] args);
	
	@Override
	public String toString() {
        return getClass().getSimpleName() + "{name=" + name + ",org=" + organization + ",queue=" + queue() + "}";
	}
	
}
