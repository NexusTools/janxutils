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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.WeakHashMap;
import net.nexustools.concurrent.Prop;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.utils.Creator;
import net.nexustools.utils.WeakArrayList;
import net.nexustools.utils.log.Logger;

/**
 * A Stream which allows reading/writing using a {@link RandomAccessFile}.
 * 
 * @author katelyn
 */
public class FileStream extends Stream {
	
	private static final WeakArrayList<FileStream> deleteOnExit = new WeakArrayList();
	private static final WeakHashMap<String, FileStream> instanceCache = new WeakHashMap();
	private RandomAccessFile randomAccessFile;
	private final boolean writable;
	private final File internal;
	
	public FileStream(String path, boolean writable) throws FileNotFoundException, IOException {
		this.internal = new File(path);
		this.writable = writable;
		//ensureOpen();
	}
	
	public FileStream(String path) throws FileNotFoundException, IOException {
		this(path, false);
	}
	
	protected final void ensureOpen() throws FileNotFoundException, IOException {
		if(randomAccessFile == null) {
			if(writable) {
				String path = internal.getAbsolutePath();
				String parentPath = path.substring(0, path.lastIndexOf("/"));
				Logger.debug(parentPath);
				File parentFile = new File(parentPath);
				if(!parentFile.exists() && !parentFile.mkdirs())
					throw new IOException(toURL() + ": Unable to create directory structure");
			}
			randomAccessFile = new RandomAccessFile(internal, writable ? "rw" : "r");
			randomAccessFile.getChannel().lock(0L, Long.MAX_VALUE, !writable);
		}
	}
	
	@Override
	public String scheme() {
		return "file";
	}
	
	@Override
	public String path() {
		return internal.getAbsolutePath();
	}
	
	public static Stream getStream(String filePath) throws IOException {
		return getStream(filePath, false);
	}
	
	public static synchronized Stream getStream(String filePath, boolean writeable) throws FileNotFoundException, IOException {
		FileStream fileStream;
		if(writeable)
			return new FileStream(filePath, true);
		else {
			fileStream = instanceCache.get(filePath);
			if(fileStream == null) {
				Logger.gears("Creating FileStream: " + filePath);
				fileStream = new FileStream(filePath);
				instanceCache.put(filePath, fileStream);
			}
		}
		
		return fileStream.createSubSectorStream();
	}

	@Override
	public void seek(long pos) throws IOException {
		ensureOpen();
		randomAccessFile.seek(pos);
	}

	@Override
	public long pos() throws IOException {
		ensureOpen();
		return randomAccessFile.getFilePointer();
	}

	@Override
	public long size() throws IOException {
		ensureOpen();
		return randomAccessFile.length();
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException {
		ensureOpen();
		return randomAccessFile.read(buffer, off, len);
	}

	@Override
	public void write(byte[] buffer, int off, int len) throws IOException {
		ensureOpen();
		randomAccessFile.write(buffer, off, len);
	}

	@Override
	public boolean canWrite() {
		return writable;
	}

	@Override
	public void flush() throws IOException {}

	private boolean markedForDeletion = false;
	private static Thread deleteOnExitThread;
	public void markDeleteOnExit() {
		if(!writable)
			throw new RuntimeException("It makes no sense to mark a read-only file as deleteOnExit...");
		
		if(markedForDeletion)
			return;
		
		markedForDeletion = true;
		deleteOnExit.add(this);
		if(deleteOnExitThread == null) {
			deleteOnExitThread = new Thread(new Runnable() {

				@Override
				public void run() {
					if(deleteOnExit.isEmpty())
						return;
					
					Logger.gears("Cleaning remaining deleteOnExit FileStreams...");
					for(FileStream fStream : deleteOnExit)
						try {
							fStream.deleteAsMarked();
						} catch (Throwable ex) {
							ex.printStackTrace(System.err);
						}
				}
				
			}, "deleteOnExitHandler");
			Runtime.getRuntime().addShutdownHook(deleteOnExitThread);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			if(markedForDeletion) {
				deleteAsMarked();
				deleteOnExit.remove(this);
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		} finally {
			super.finalize();
		}
	}
	
	public final boolean isOpen() {
		return randomAccessFile != null;
	}
	
	public final void close() throws IOException {
		if(randomAccessFile != null) {
			randomAccessFile.close();
			randomAccessFile = null;
		}
	}

	private void deleteAsMarked() throws IOException {
		close();
		internal.delete();
	}
	
	@Override
	public boolean exists() {
		return internal.exists();
	}
	
	@Override
	public Iterable<String> children() {
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				final String[] children = internal.list();
				
				if(children != null && children.length > 0)
					return new Iterator<String>() {
						int pos = -1;
						public boolean hasNext() {
							return pos + 1 < children.length;
						}
						public String next() {
							return children[++pos];
						}
						public void remove() {
							throw new UnsupportedOperationException("Not supported.");
						}
					};
				else
					return new Iterator<String>() {
						public boolean hasNext() {
							return false;
						}
						public String next() {
							throw new UnsupportedOperationException("Not supported.");
						}
						public void remove() {
							throw new UnsupportedOperationException("Not supported.");
						}
					};
			}
		};
	}
	
	@Override
	public boolean hasChildren() {
		return internal.isDirectory();
	}

	static final Prop<Creator<String, File>> detectedProbeContentType = new Prop<Creator<String, File>>();
	@Override
	public String mimeType() {
		String type = detectedProbeContentType.read(new SoftWriteReader<String, PropAccessor<Creator<String, File>>>() {
			@Override
			public String soft(PropAccessor<Creator<String, File>> data) {
				return data.get().create(internal);
			}
			@Override
			public String read(PropAccessor<Creator<String, File>> data) {
				ClassLoader cl = ClassLoader.getSystemClassLoader();
				try {
					final Class<?> pathClass = cl.loadClass("java.nio.file.Path");
					final Class<?> pathsClass = cl.loadClass("java.nio.file.Paths");
					final Class<?> filesClass = cl.loadClass("java.nio.file.Files");
					
					data.set(new Creator<String, File>() {
						final Method pathsGet = pathsClass.getMethod("get", URI.class);
						final Method probeContentType = filesClass.getMethod("probeContentType", pathClass);
						{
							create(internal); // Test it once
							Logger.debug("Detected Java 7 APIs", pathsGet, probeContentType);
						}
						public String create(File using) {
							try {
								return (String) probeContentType.invoke(null, pathsGet.invoke(null, using.toURI()));
							} catch (IllegalAccessException ex) {
								return null;
							} catch (IllegalArgumentException ex) {
								return null;
							} catch (InvocationTargetException ex) {
								return null;
							}
						}
					});
				} catch(Throwable t) {
					data.set(new Creator<String, File>() {
						public String create(File using) {
							return null;
						}
					});
					Logger.exception(Logger.Level.Debug, t);
				}
				
				return data.get().create(internal);
			}
		});
		if(type != null)
			return type;
		
		return super.mimeType();
	}

	@Override
	public boolean isHidden() {
		return internal.isHidden();
	}

	@Override
	public long lastModified() {
		return internal.lastModified();
	}

	@Override
	public boolean canRead() {
		return internal.canRead();
	}
	
}
