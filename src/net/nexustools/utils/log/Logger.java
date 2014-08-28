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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.nexustools.Application;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.PropMap;
import static net.nexustools.concurrent.ReadWriteLock.defaultPermitCount;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.ListAccessor;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.data.annote.ThreadUnsafe;
import net.nexustools.utils.Creator;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Testable;
import net.nexustools.utils.WeakArrayList;
import net.nexustools.utils.sort.DescLongTypeComparator;

/**
 *
 * @author katelyn
 */
public class Logger extends Thread {
	
	private static Creator<Object, Object> Identity = new Creator<Object, Object>() {
		public Object create(Object using) {
			return using;
		}
	};
	private static Creator<Object, Object> ThreadUnsafeCreator = new Creator<Object, Object>() {
		public Object create(Object using) {
			return new ThreadUnsafeWrapper(using);
		}
	};
	private static class Numeric implements Creator<Object, Object> {
		private final String name;
		public Numeric(String name) {
			this.name = name;
		}
		
		public Object create(final Object using) {
			return new Object() {
				@Override
				public String toString() {
					return name + '(' + using + ')';
				}
			};
		}
	}
	
	private static final Prop<Boolean> shutdown = new Prop(false);
	private static final PropMap<Class<?>, Creator<Object, Object>> threadUnsafe = new PropMap() {
		{
			put(Float.class, new Numeric("Float"));
			put(Double.class, new Numeric("Double"));
			
			put(Long.class, new Numeric("Long"));
			put(Short.class, new Numeric("Short"));
			put(Byte.class, new Numeric("Byte"));
			
			put(ArrayList.class, ThreadUnsafeCreator);
			put(WeakArrayList.class, ThreadUnsafeCreator);
			put(LinkedList.class, ThreadUnsafeCreator);
			
			put(EnumMap.class, ThreadUnsafeCreator);
			put(Hashtable.class, ThreadUnsafeCreator);
			put(WeakHashMap.class, ThreadUnsafeCreator);
			put(LinkedHashMap.class, ThreadUnsafeCreator);
			put(HashMap.class, ThreadUnsafeCreator);
			put(TreeMap.class, ThreadUnsafeCreator);
			
			put(Quote.class, Identity);
			put(Integer.class, Identity);
			put(String.class, Identity);
			put(Class.class, Identity);
		}
	};
	private static final PropList<String> classesToSkip = new PropList() {
		{
			push(Thread.class.getName());
			push(Message.class.getName());
			push(Logger.class.getName());
			push(Level.class.getName());
		}
	};
	private static final PrintStream SystemOut = System.out;
	private static final PrintStream SystemErr = System.err;
	private static final Level minLevel;
	
	static {
		Level mLevel = Level.Performance;
		String strLevel = System.getProperty("logger");
		if(strLevel != null)
			for(Level level : Level.values())
				if(level.name().equalsIgnoreCase(strLevel) ||
						level.title.equalsIgnoreCase(strLevel)) {
					mLevel = level;
					break;
				}
		minLevel = mLevel;
		
		Runtime.getRuntime().addShutdownHook(new Thread("Logger-Cleanup") {
			@Override
			public void run() {
				try {
					shutdown.write(new Writer<PropAccessor<Boolean>>() {
						@Override
						public void write(PropAccessor<Boolean> data) throws Throwable {
							info("Exit requested, shutting down");
							data.set(true);
						}
					});
				} catch (InvocationTargetException ex) {
					throw NXUtils.wrapRuntime(ex);
				}
				
				System.out.println("Waiting for Logger to Exit");
				while(logger.isAlive())
					try {
						logger.join(50);
						logger.interrupt();
						break;
					} catch (InterruptedException ex) {}
			}
		});
	}

	public static void init() {}
	
	private static Object wrap(Object obj) {
		if(obj == null)
			return obj;
		
		if(obj instanceof Throwable)
			obj = NXUtils.unwrapTarget((Throwable)obj);
		
		final Class<?> clazz = obj.getClass();
		try {
			return threadUnsafe.read(new SoftWriteReader<Creator<Object, Object>, MapAccessor<Class<?>, Creator<Object, Object>>>() {
						@Override
						public boolean test(MapAccessor<Class<?>, Creator<Object, Object>> against) {
							return !against.has(clazz);
						}
						@Override
						public Creator<Object, Object> soft(MapAccessor<Class<?>, Creator<Object, Object>> data) throws Throwable {
							return data.get(clazz);
						}
						@Override
						public Creator<Object, Object> read(MapAccessor<Class<?>, Creator<Object, Object>> data) throws Throwable {
							Boolean isThreadUnsafe = false;
							Class<?> testClass = clazz;
							
							do {
								for(Annotation annote : testClass.getDeclaredAnnotations()) {
									if(annote instanceof ThreadUnsafe) {
										isThreadUnsafe = true;
										break;
									}
								}
							} while(!isThreadUnsafe && (testClass = testClass.getSuperclass()) != Object.class);
							
							Creator<Object, Object> creator;
							if(isThreadUnsafe) {
								Logger.performance(clazz, "is thread unsafe\nWill be processed on calling thread instead of Logger thread");
								creator = ThreadUnsafeCreator;
							} else
								creator = Identity;
							data.put(clazz, creator);
							return creator;
							
						}
					}).create(obj);
		} catch (InvocationTargetException ex) {
			warn(ex);
		}
		
		return obj;
	}
	private static class Quote {
		public final Object obj;
		public Quote(Object obj) {
			this.obj = wrap(obj);
		}
		public String toString() {
			return "`" + obj + '`';
		}
	}
	private static class ThreadUnsafeWrapper {
		public final Object obj;
		public final String toStringSnapshot;
		public ThreadUnsafeWrapper(Object obj) {
			toStringSnapshot = obj.toString();
			this.obj = obj;
		}
		public String toString() {
			return toStringSnapshot;
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
	
	public static void log(final Message message) {
		if(message.level.code >= minLevel.code && !shutdown.get()) {
			try {
				message.finish();
				logger.messageQueue.write(new Writer<ListAccessor<Message>>() {
					@Override
					public void write(ListAccessor<Message> data) {
						data.push(message);
						logger.interrupt();
					}
				});
			} catch (InvocationTargetException ex) {
				throw NXUtils.wrapRuntime(ex);
			}
			logger.interrupt();
		}
	}
	
	public static void log(Level level, Object... message) {
		log(new Message(level, message));
	}

	public static void gears(Object... message) {
		log(Level.Gears, message);
	}

	public static void performance(Object... message) {
		log(Level.Performance, message);
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
		Gears("GEARS", (byte)-2),
		
		/**
		 * Messages about performance.
		 */
		Performance("PEFRM", (byte)-1),
		
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
			return "$Error" + t.getClass();
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
		
		public void finish() {
			for(int i=0; i<content.length; i++) 
				this.content[i] = wrap(this.content[i]);
		}
		
	}
	
	protected static class Fitter {
		float len = 0;
		static final boolean enabled = System.getProperty("loggerwhitespace", "enabled").equalsIgnoreCase("enabled");
		public String expand(String string) {
			if(string.length() > len)
				len = string.length();
			else {
				int ceilLen = (int)Math.ceil(len);
				int rem = ceilLen - string.length();
				boolean front = false;
				while(rem-- > 0) {
					if(front = !front)
						string += ' ';
					else
						string = ' ' + string;
				}
				if(ceilLen > 1)
					len -= 0.4;
			}
			return string;
		}
	}
	
	private static final Logger logger = new Logger();
	private PropList<Message> messageQueue = new PropList<Message>();
	protected Logger() {
		super("Logger");
		setPriority(MIN_PRIORITY);
		setDaemon(true);
		start();
	}

	boolean running;
	long sleepTime;
	@Override
	public void run() {
		if(minLevel.code <= Level.Gears.code)
			messageQueue.push(new Message(Logger.Level.Gears, Thread.currentThread().getName(), "ReadWriteLock", "Using", defaultPermitCount, "permits by Default"));
		
		final WeakHashMap<String, String> classNames = new WeakHashMap();
		final Fitter threadName = new Fitter();
		final Fitter className = new Fitter();
		ListAccessor<Message> readyMessages;;
		
		sleepTime = 200 ;
		while(running = !shutdown.get() || messageQueue.isTrue()) {
			final long after = System.currentTimeMillis() - Short.valueOf(System.getProperty("loggerdelay", "120"));
			if(running) {
				readyMessages = messageQueue.take(new Testable<Message>() {
					public boolean test(Message against) {
						long until = after - against.timestamp;
						if(until > 0)
							sleepTime = Math.min(sleepTime, until);
						else
							return true;
						return false;
					}
				});
			} else
				readyMessages = messageQueue.take();
				
			if(readyMessages.length() > 0) {
				readyMessages.sort(new DescLongTypeComparator<Message>() {
					@Override
					public long value(Message o) {
						return o.timestamp;
					}
				});
				for(Message message : readyMessages) {
					PrintStream stream;
					try {
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
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
			} else
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ex) {}
		}
		SystemOut.println("Logger exited");
	}
	
}
