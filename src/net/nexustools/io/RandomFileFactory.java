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

package net.nexustools.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.RuntimeTargetException;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class RandomFileFactory {
	
	private static class Impl extends RandomAccessFile {

		private final String path;
		private boolean closed = false;
		private final boolean writeable;
		private final AtomicInteger ref = new AtomicInteger(1);
		public Impl(String path, boolean writeable) throws FileNotFoundException {
			super(path, writeable ? "rw" : "r");
			this.writeable = writeable;
			this.path = path;
		}

		private void addRef() {
			ref.incrementAndGet();
		}

		private void delRef() {
			ref.decrementAndGet();
		}

		@Override
		public void close() throws IOException {
			super.close();
			getChannel().close();
			closed = true;
		}

		private boolean isClosed() {
			return closed;
		}

		@Override
		public String toString() {
			return (writeable ? "rw" : "r") + path + "@" + ref.get();
		}
		
	}
	
	private static final PropMap<String, SoftReference<Impl>> cache = new PropMap();
	
	public static RandomAccessFile open(final String path, final boolean writeable) throws IOException {
		try {
			return cache.read(new SoftWriteReader<RandomAccessFile, MapAccessor<String, SoftReference<Impl>>>() {
				Impl file;
				@Override
				public boolean test(MapAccessor<String, SoftReference<Impl>> against) {
					try {
						return (file = against.get(path).get()) == null || file.isClosed() || (writeable && !file.writeable);
					} catch(NullPointerException ex) {
						return true;
					}
				}
				@Override
				public RandomAccessFile read(MapAccessor<String, SoftReference<Impl>> data) {
					try {
						if(file != null && file.ref.get() < 1)
							file.close();
						
						Impl impl = new Impl(path, writeable);
						int tries = 20;
						while(true)
							try {
								impl.getChannel().lock(0L, Long.MAX_VALUE, !writeable);
								break;
							} catch(OverlappingFileLockException ex) {
								if(tries == 20)
									Logger.performance("Waiting on lock", path);
								
								if(file != null && file.ref.get() < 1)
									file.close();
								
								if(tries-- < 1)
									throw new IOException(path + ": Could not get read lock after 20 tries.");
								try {
									Thread.sleep(200);
								} catch (InterruptedException ex1) {}
							}
						data.put(path, new SoftReference<Impl>(impl));
						return impl;
					} catch (IOException ex) {
						throw NXUtils.wrapRuntime(ex);
					}
				}
				@Override
				public RandomAccessFile soft(MapAccessor<String, SoftReference<Impl>> data) {
					file.addRef();
					return file;
				}
			});
		} catch (RuntimeTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
	}
	public static void release(RandomAccessFile randomAccessFile) {
		((Impl)randomAccessFile).delRef();
	}
	
}
