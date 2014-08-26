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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.logging.Level;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.Creator;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.log.Logger;

/**
 * A Stream which allows reading/writing using a {@link RandomAccessFile}.
 * 
 * @author katelyn
 */
public class FileStream extends Stream {
	
	private static final WeakHashMap<String, FileStream> instanceCache = new WeakHashMap();
	private final Prop<RandomAccessFile> randomAccessFile = new Prop();
	private final File internal;
	
	protected FileStream(String path) throws FileNotFoundException, IOException {
		this.internal = new File(path);
	}
	
	protected final RandomAccessFile ensureOpen() throws FileNotFoundException, IOException {
		return ensureOpen(false);
	}
	protected final RandomAccessFile ensureOpen(final boolean writable) throws IOException {
		try {
			return randomAccessFile.read(new SoftWriteReader<RandomAccessFile, PropAccessor<RandomAccessFile>>() {
				@Override
				public RandomAccessFile soft(PropAccessor<RandomAccessFile> data) {
					return data.get();
				}
				@Override
				public RandomAccessFile read(PropAccessor<RandomAccessFile> data) throws IOException {
					if(writable) {
						String path = internal.getAbsolutePath();
						String parentPath = path.substring(0, path.lastIndexOf("/"));
						Logger.debug(parentPath);
						File parentFile = new File(parentPath);
						if(!parentFile.exists() && !parentFile.mkdirs())
							throw new IOException(toURL() + ": Unable to create directory structure");
					}
					RandomAccessFile randomFile;
					randomAccessFile.set(randomFile = new RandomAccessFile(internal, writable ? "rw" : "r"));
					randomAccessFile.get().getChannel().lock(0L, Long.MAX_VALUE, !writable);
					return randomFile;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
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
	
	public static synchronized Stream getStream(String filePath) throws FileNotFoundException, IOException {
		FileStream fileStream;
		fileStream = instanceCache.get(filePath);
		if(fileStream == null) {
			Logger.gears("Creating FileStream: " + filePath);
			fileStream = new FileStream(filePath);
			instanceCache.put(filePath, fileStream);
		}
		
		return fileStream;
	}

	@Override
	public long size() throws IOException {
		return ensureOpen().length();
	}

	@Override
	public boolean canWrite() {
		return internal.canWrite();
	}

	public void markDeleteOnExit() {
		internal.deleteOnExit();
	}
	
	public final boolean isOpen() {
		return randomAccessFile.isset();
	}
	
	public final void close() throws IOException {}
	
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
		String type = null;
		try {
			type = detectedProbeContentType.read(new SoftWriteReader<String, PropAccessor<Creator<String, File>>>() {
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
							public synchronized String create(File using) {
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
						Logger.exception(Logger.Level.Gears, t);
					}
					
					return data.get().create(internal);
				}
			});
		} catch (InvocationTargetException ex) {
			Logger.exception(Logger.Level.Gears, ex);
		}
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

	@Override
	public InputStream createInputStream(long p) throws IOException {
		return new FileInputStream(ensureOpen()) {
			boolean open = true;
			@Override
			public void close() throws IOException {
				if(open)
					open = false;
			}
			@Override
			public synchronized int available() throws IOException {
				if(!open)
					throw new IOException("Closed");
				
				return super.available();
			}
			@Override
			public synchronized int read(byte[] b, int off, int len) throws IOException {
				if(!open)
					throw new IOException("Closed");
				
				return super.read(b, off, len);
			}
		};
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
