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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import net.nexustools.io.EfficientOutputStream;

/**
 *
 * @author katelyn
 */
public class LoggerPrintStream extends PrintStream {
	
	public static class ThreadOutputStream extends EfficientOutputStream {
		private final StringBuffer stringBuffer = new StringBuffer();
		private final Logger.Level level;
		
		public ThreadOutputStream(Logger.Level level) {
			this.level = level;
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			
		}
	}

	public LoggerPrintStream(Logger.Level level) {
		super((OutputStream)new ThreadOutputStream(level));
	}
	
}
