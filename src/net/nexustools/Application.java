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
import net.nexustools.AppDelegate.Path;
import net.nexustools.concurrent.Prop;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.concurrent.logic.IfWriter;
import net.nexustools.concurrent.logic.Writer;
import static net.nexustools.io.Stream.bindSynthScheme;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author kate
 */
public class Application {
	private static final Prop<AppDelegate> delegate = new Prop();
	private static long offset = 0;
	
	static {
		Logger.installSystem();
	}
		
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
		long millis = timestamp - (AppDelegate.created + offset);
		if(millis < 0) {
			offset += millis;
			millis = 0;
		}

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
	
	private static void setDelegate0(PropAccessor<AppDelegate> data, AppDelegate app) {
		Logger.quote("Starting AppDelegate", app);

		data.set(app);
		for(Path path : Path.values())
			try {
				String pathUri = app.pathUri(path);
				bindSynthScheme(path.scheme, pathUri);
				Logger.debug(path.scheme + "://", "bound to", pathUri);
			} catch(Throwable t) {
				Logger.warn(path.scheme + "://", "is not supported by this AppDelegate.");
				Stream.remove(path.scheme);
			}
	}
	
	public static void setDelegateIfNone(final AppDelegate app) {
		delegate.write(new IfWriter<PropAccessor<AppDelegate>>() {
			@Override
			public boolean test(PropAccessor<AppDelegate> against) {
				return !against.isset();
			}
			@Override
			public void write(PropAccessor<AppDelegate> data) {
				setDelegate0(data, app);
			}
		});
	}
	
	public static void setDelegate(final AppDelegate app) {
		delegate.write(new IfWriter<PropAccessor<AppDelegate>>() {
			@Override
			public boolean test(PropAccessor<AppDelegate> against) {
				return app != against.get();
			}
			@Override
			public void write(PropAccessor<AppDelegate> data) {
				setDelegate0(data, app);
			}
		});
	}
	
}
