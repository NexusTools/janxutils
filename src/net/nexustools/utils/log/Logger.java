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

package net.nexustools.utils.log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.WeakHashMap;

import net.nexustools.Application;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.PropList;
import static net.nexustools.concurrent.ReadWriteLock.defaultPermitCount;
import net.nexustools.concurrent.logic.Writer;

/**
 *
 * @author katelyn
 */
public class Logger extends Thread {
	
	private static Logger logger = new Logger();
	private static final PropList<String> classesToSkip = new PropList();
	private static final PrintStream SystemOut = System.out;
	private static final PrintStream SystemErr = System.err;
	private static final Level minLevel;
	
	static {
		Level mLevel = Level.Gears;
		String strLevel = System.getProperty("logger");
		if(strLevel != null)
			for(Level level : Level.values())
				if(level.name().equalsIgnoreCase(strLevel)) {
					mLevel = level;
					break;
				}
		minLevel = mLevel;
		if(mLevel.code <= Level.Gears.code)
			logger.messageQueue.push(new Message(Logger.Level.Gears, Thread.currentThread().getName(), "ReadWriteLock", "Using", defaultPermitCount, "permits by Default"));
	}
	
	private static class Quote {
		public final Object obj;
		public Quote(Object obj) {
			this.obj = obj;
		}
		public String toString() {
			return "`" + obj + '`';
		}
	}
	
	public static Object wrapQuotes(Object obj) {
		if(obj instanceof Quote)
			return obj;
		return new Quote(obj);
	}
	
	/**
	 * Installs writers on System.out and System.err which attempt
	 * to emulate calls to log() when line downs are found, per thread.
	 * 
	 * {@code LoggerPrintStream}
	 */
	public static void installSystem() {
		//System.setOut(new LoggerPrintStream(Level.Debug));
		//System.setErr(new LoggerPrintStream(Level.Error));
	}
	
	public static void addSkippableClass(Class<?> clazz) {
		classesToSkip.push(clazz.getName());
	}
	
	static {
		addSkippableClass(Thread.class);
		addSkippableClass(Message.class);
		addSkippableClass(Logger.class);
		addSkippableClass(Level.class);
	}
	
	public static void log(final Message message) {
		if(message.level.code >= minLevel.code) {
			logger.messageQueue.write(new Writer<ListAccessor<Message>>() {
				@Override
				public void write(ListAccessor<Message> data) {
					data.push(message);
				}
			});
			logger.interrupt();
		}
	}
	
	public static void log(Level level, Object... message) {
		log(new Message(level, message));
	}

	public static void gears(Object... message) {
		log(Level.Gears, message);
	}

	public static void debug(Object... message) {
		log(Level.Debug, message);
	}

	public static void info(Object... message) {
		log(Level.Information, message);
	}

	public static void quote(Level level, Object message, Object quote) {
		log(level, message, wrapQuotes(quote));
	}

	public static void quote(Object message, Object quote) {
		quote(Level.Information, message, quote);
	}

	public static void warn(Object... message) {
		log(Level.Warning, message);
	}

	public static void error(Object... message) {
		log(Level.Error, message);
	}

	public static void exception(Throwable t) {
		exception(Level.Error, t);
	}

	public static void exception(Level level, Throwable t) {
		log(level, t);
	}
	
	public static enum Level {
		/**
		 * Internal messages about changes to the underlying implementations.
		 * Produces an immense amount of output, disabled by default.
		 */
		Gears("GEARS", (byte)-1),
		
		/**
		 * Messages about actions and state changes that may be helpful for debugging.
		 */
		Debug("DEBUG", (byte)0),

		Information("INFO ", (byte)1),
		Warning("WARN ", (byte)2),
		Error("ERROR", (byte)3),
		Fatal("FATAL", (byte)4);
		
		public final byte code;
		public final String title;
		Level(String title, byte level) {
			this.title = title;
			this.code = level;
		}
	}
	
	public static StackTraceElement getCallee() {
		return getCallee(1);
	}
	
	public static StackTraceElement getCallee(int skip) {
		StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stackElements) {
			if(classesToSkip.contains(element.getClassName()))
				continue;
			if(skip > 0) {
				skip--;
				continue;
			}
			
			return element;
		}
		
		return null;
	}
	
	public static String getCalleeName() {
		return getCalleeName(1);
	}
	
	public static String getCalleeName(int skip) {
		try {
			return getCallee(skip).getClassName();
		} catch(NullPointerException t) {
			return "$Unknown";
		} catch(Throwable t) {
			return "$Error$" + t.getClass();
		}
	}
	
	public static class Message {
		public final long timestamp = System.currentTimeMillis();
		
		public final Level level;
		public final String thread;
		public final String className;
		public final Object[] content;
		
		public Message(Level level, String thread, String className, Object... content) {
			this.level = level;
			this.thread = thread;
			this.className = className;
			this.content = content;
		}
		
		public Message(Level level, String className, Object... content) {
			this(level, Thread.currentThread().getName(), className, content);
		}
		
		public Message(Level level, Object... content) {
			this(level, getCalleeName(0), content);
		}
		
	}
	
	protected static class Expander {
		int len = 0;
		static final boolean enabled = System.getProperty("loggerwhitespace", "enabled").equalsIgnoreCase("enabled");
		public String expand(String string) {
			if(string.length() > len)
				len = string.length();
			else {
				int rem = len - string.length();
				boolean front = false;
				while(rem-- > 0) {
					if(front = !front)
						string += ' ';
					else
						string = ' ' + string;
				}
			}
			return string;
		}
	}
	
	private PropList<Message> messageQueue = new PropList<Message>();
	protected Logger() {
		super("Logger");
		setPriority(MIN_PRIORITY);
		setDaemon(true);
		start();
	}

	@Override
	public void run() {
		final WeakHashMap<String, String> classNames = new WeakHashMap();
		final ArrayList<Message> messages = new ArrayList();
		final Expander threadName = new Expander();
		final Expander className = new Expander();
		
		while(true) {
			messages.addAll(messageQueue.take());
			if(messages.size() > 0) {
				ArrayList<Message> readyMessages = new ArrayList();
				Iterator<Message> it = messages.iterator();
				long before = System.currentTimeMillis() - Short.valueOf(System.getProperty("loggerdelay", "120"));
				while(it.hasNext()) {
					Message next = it.next();
					if(next.timestamp < before) {
						readyMessages.add(next);
						it.remove();
					}
				}
				Collections.sort(readyMessages, new Comparator<Message>() {
					public int compare(Message o1, Message o2) {
						return (int)(o1.timestamp - o2.timestamp);
					}
				});
			
				if(readyMessages.size() > 0) {
					for(Message message : readyMessages) {
						PrintStream stream;
						switch(message.level) {
							case Error:
							case Fatal:
								stream = SystemErr;
								break;

							default:
								stream = SystemOut;
								break;
						}
						stream.print(Application.uptime(message.timestamp));
						stream.print(" [");
						stream.print(message.level.title);
						stream.print("] [");
						stream.print(threadName.expand(message.thread));
						stream.print("] [");

						String goodClassName = classNames.get(message.className);
						if(goodClassName == null) {
							int lastPeriod = message.className.lastIndexOf(".");
							goodClassName = lastPeriod > -1 ? message.className.substring(lastPeriod+1) : message.className;
							classNames.put(message.className, goodClassName);
						}
						stream.print(className.expand(goodClassName));

						stream.print("] ");
						boolean addTab = false;
						for(Object msg : message.content) {
							if(addTab)
								stream.print(' ');
							else
								addTab = true;
							
							if(msg instanceof Throwable)
								((Throwable)msg).printStackTrace(stream);
							else if(msg == null)
								stream.print("$NULL$");
							else
								stream.print(msg.toString());
						}

						stream.println();
						stream.flush();
					}
				} else
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {}
			} else
				try {
					Thread.sleep(5 * 60 * 60);
				} catch (InterruptedException ex) {}
		}
	}
	
}
