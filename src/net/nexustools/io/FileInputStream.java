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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.OverlappingFileLockException;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class FileInputStream extends EfficientInputStream {
	
	private static final PropMap<String, WeakReference<RandomAccessFile>> cache = new PropMap();
	
	private final RandomAccessFile openImpl(final String path) throws IOException {
		try {
			return cache.read(new SoftWriteReader<RandomAccessFile, MapAccessor<String, WeakReference<RandomAccessFile>>>() {
				RandomAccessFile file;
				private int writable;
				@Override
				public boolean test(MapAccessor<String, WeakReference<RandomAccessFile>> against) {
					try {
						return (file = against.get(path).get()) == null;
					} catch(NullPointerException ex) {
						return true;
					}
				}
				@Override
				public RandomAccessFile read(MapAccessor<String, WeakReference<RandomAccessFile>> data) throws Throwable {
					RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
					int tries = 20;
					while(true)
						try {
							randomAccessFile.getChannel().lock(0L, Long.MAX_VALUE, true);
							break;
						} catch(OverlappingFileLockException ex) {
							if(tries-- < 1)
								throw new IOException(path + ": Could not get read lock after 20 tries.");
							Thread.sleep(50);
						}
					return randomAccessFile;
				}
				@Override
				public RandomAccessFile soft(MapAccessor<String, WeakReference<RandomAccessFile>> data) throws Throwable {
					return file;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
	}
	
	long pos = 0;
	long markPos = -1;
	RandomAccessFile file;
	public FileInputStream(File file) throws IOException {
		this(file.getAbsolutePath());
	}
	public FileInputStream(String file) throws IOException {
		this.file = openImpl(file);
	}

	@Override
	public int available() throws IOException {
		long available = file.length() - pos;
		if(available > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int)available;
	}

	@Override
	public void close() throws IOException {
		if(file == null)
			return;
		
		file.close();
		file = null;
	}

	@Override
	public synchronized void mark(int readlimit) {
		markPos = pos;
	}

	@Override
	public synchronized void reset() throws IOException {
		if(markPos < 0)
			throw new IOException("Mark was not called, cannot reset");
		pos = markPos;
		markPos = -1;
	}

	@Override
	public long skip(long n) throws IOException {
		pos += n;
		return n;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		file.seek(pos);
		int read = file.read(b, off, len);
		pos = file.getFilePointer();
		return read;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(file != null) {
			Logger.warn(file.toString() + " was not closed");
			close();
		}
	}
	
}
