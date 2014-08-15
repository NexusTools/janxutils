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
import java.util.Collections;
import java.util.List;
import net.nexustools.AppDelegate;
import net.nexustools.concurrent.ListAccessor;
import net.nexustools.concurrent.PropList;
import net.nexustools.concurrent.Writer;

/**
 *
 * @author katelyn
 */
public class Logger extends Thread {
	
	private static Logger logger = new Logger();
	private static final PropList<String> classesToSkip = new PropList();
	private static final PrintStream SystemOut = System.out;
	private static final PrintStream SystemErr = System.err;
	
	static {
		addSkippableClass(Message.class);
		addSkippableClass(Logger.class);
		addSkippableClass(Level.class);
	}
	
	/**
	 * Installs writers on System.out and System.err which attempt
	 * to emulate calls to log() when line downs are found, per thread.
	 * 
	 * {@code LoggerPrintStream}
	 */
	public static void installSystemIO() {
		System.setOut(new LoggerPrintStream(Level.Debug));
		System.setErr(new LoggerPrintStream(Level.Error));
	}
	
	public static void addSkippableClass(Class<?> clazz) {
		classesToSkip.push(clazz.getName());
	}
	
	public static void log(final Message message) {
		logger.messageQueue.write(new Writer<ListAccessor<Message>>() {
			@Override
			public void write(ListAccessor<Message> data) {
				data.push(message);
				logger.interrupt();
			}
		});
	}
	
	public static void log(Level level, Object... message) {
		log(new Message(level, message));
	}

	public static void debug(Object... message) {
		log(Level.Debug, message);
	}

	public static void warn(Object... message) {
		log(Level.Warning, message);
	}

	public static void error(Object... message) {
		log(Level.Warning, message);
	}
	
	public static enum Level {
		Debug,
		Warning,
		Error,
		Fatal
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
			this(level, getCallee().getClassName(), content);
		}
		
	}
	
	private static String outputFormat = "[{{uptime}}] [{{thread}}] {{content}}";
	
	private PropList<Message> messageQueue = new PropList<Message>();
	protected Logger() {
		start();
	}

	@Override
	public void run() {
		while(true) {
			List<Message> messages = messageQueue.take();
			if(messages.size() > 0) {
				Collections.reverse(messages);
				for(Message message : messages) {
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
					stream.print("[");
					stream.print(AppDelegate.uptime(message.timestamp));
					stream.print("] [");
					stream.print(message.thread);
					stream.print("] [");
					int lastPeriod = message.className.lastIndexOf(".");
					stream.print(lastPeriod > -1 ? message.className.substring(lastPeriod+1) : message.className);
					stream.print("] [");
					stream.print(message.level);
					stream.print("] ");
					boolean addTab = false;
					for(Object msg : message.content) {
						if(addTab)
							stream.print(' ');
						else
							addTab = true;
						
						stream.print(msg.toString());
					}
					
					stream.println();
					stream.flush();
				}
			} else
				try {
					Thread.sleep(5 * 60 * 60);
				} catch (InterruptedException ex) {}
		}
	}
	
}
