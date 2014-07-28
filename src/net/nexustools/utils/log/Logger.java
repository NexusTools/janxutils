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

import net.nexustools.concurrent.PropList;

/**
 *
 * @author katelyn
 */
public class Logger {
	
	private static final PropList<String> classesToSkip = new PropList();
	public static final long lifetime = System.currentTimeMillis();
	
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
		public final String section;
		public final String content;
		
		public Message(Level level, String thread, String section, String content) {
			this.level = level;
			this.thread = thread;
			this.section = section;
			this.content = content;
		}
		
		public Message(Level level, String section, String content) {
			this(level, Thread.currentThread().getName(), section, content);
		}
		
		public Message(Level level, String content) {
			this(level, getCallee().getClassName(), content);
		}
		
		public String uptime() {
			return "";
		}
	}
	
	private static String outputFormat = "[{{uptime}}] [{{thread}}] {{content}}";
	
}
