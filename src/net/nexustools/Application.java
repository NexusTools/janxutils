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

import java.util.HashMap;
import net.nexustools.AppDelegate.Path;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropAccessor;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.io.Stream;
import static net.nexustools.io.Stream.bindSynthScheme;
import static net.nexustools.io.Stream.uriForPath;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public class Application {
	public static final long created = System.currentTimeMillis();
	private static final Prop<AppDelegate> delegate = new Prop();
		
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
		long millis = timestamp - created;

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
	
	public static String name() {
		return delegate.get().name();
	}
	
	public static String organization() {
		return delegate.get().organization();
	}
	
	public static String defaultName() {
		return System.getProperty("appname", "Untitled Application");
	}
	
	public static String defaultOrganization() {
		return System.getProperty("apporg", "NexusTools");
	}
	
	public static void setDelegate(final AppDelegate app) {
		delegate.write(new Writer<PropAccessor<AppDelegate>>() {
			@Override
			public void write(PropAccessor<AppDelegate> data) {
				Logger.quote(data.isset() ? "Switching To" : "Spawned AppDelegate", app);
				
				data.set(app);
				for(Path path : Path.values())
					try {
						bindSynthScheme(path.scheme, uriForPath(app.pathUri(path)));
					} catch(Throwable t) {
						Logger.warn(path.scheme + "://", "is not supported by this application delegate.");
						Stream.remove(path.scheme);
					}
			}
		});
	}
	
}
