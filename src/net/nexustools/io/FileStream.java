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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.channels.ByteChannel;
import java.util.Iterator;
import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.Creator;
import net.nexustools.utils.RefreshingCache;
import net.nexustools.utils.RefreshingCacheMap;
import net.nexustools.utils.log.Logger;

/**
 * A Stream which allows reading/writing using a {@link RandomAccessFile}.
 * 
 * @author katelyn
 */
public class FileStream extends Stream {
	
	private static final RefreshingCacheMap<String, FileStream> cache = new RefreshingCacheMap<String, FileStream>(new Creator<FileStream, String>() {
		public FileStream create(String filePath) {
			Logger.performance("Opening FileStream", filePath);
			return new FileStream(filePath);
		}
	});
	
	public static Stream instance(String filePath) {
		return cache.get(filePath);
	}
	
	private final String internalPath;
	private final RefreshingCache<File> internal = new RefreshingCache<File>(new Creator<File, Void>() {
		public File create(java.lang.Void using) {
			Logger.performance("Reading file information", internalPath);
			return new File(internalPath);
		}
	});
	
	protected FileStream(String path) {
		internalPath = path;
	}
	
	@Override
	public String scheme() {
		return "file";
	}
	
	@Override
	public String path() {
		return internalPath;
	}

	@Override
	public long size() {
		return internal.get().length();
	}

	@Override
	public boolean canWrite() {
		return internal.get().canWrite();
	}

	public void markDeleteOnExit() {
		internal.get().deleteOnExit();
	}
	
	@Override
	public boolean exists() {
		return internal.get().exists();
	}
	
	private final RefreshingCache<Iterable<String>> childCache = new RefreshingCache<Iterable<String>>(new Creator<Iterable<String>, Void>() {
		public Iterable<String> create(java.lang.Void using) {
			final String[] children = internal.get().list();
			
			if(children != null && children.length > 0)
				return new Iterable<String>() {
					public Iterator<String> iterator() {
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
					}
				};
			else
				return new Iterable<String>() {
					public Iterator<String> iterator() {
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
	});
	@Override
	public Iterable<String> children() {
		return childCache.get();
	}
	
	@Override
	public boolean isDirectory() {
		return internal.get().isDirectory();
	}
	
	private final RefreshingCache<String> mimeCache = new RefreshingCache<String>(new Creator<String, Void>() {
		public String create(java.lang.Void using) {
			Logger.performance("Detecting MimeType", FileStream.this);
			String type = detectedProbeContentType.read(new SoftWriteReader<String, PropAccessor<Creator<String, File>>>() {
				@Override
				public String soft(PropAccessor<Creator<String, File>> data) {
					return data.get().create(internal.get());
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
								create(internal.get()); // Test it once
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
						Logger.exception(Logger.Level.Gears, t);
					}

					return data.get().create(internal.get());
				}
			});
			if(type == null)
				type = FileStream.super.mimeType();
			return type;
		}
	});
	static final Prop<Creator<String, File>> detectedProbeContentType = new Prop<Creator<String, File>>();
	@Override
	public String mimeType() {
		return mimeCache.get();
	}

	@Override
	public boolean isHidden() {
		return internal.get().isHidden();
	}

	@Override
	public long lastModified() {
		return internal.get().lastModified();
	}

	@Override
	public boolean canRead() {
		return internal.get().canRead();
	}

	@Override
	public InputStream createInputStream(long p) throws IOException {
		return new FileInputStream(internalPath);
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		return new FileOutputStream(internalPath);
	}

	@Override
	public ByteChannel createChannel() throws UnsupportedOperationException, IOException {
		return RandomFileFactory.open(internalPath, true).getChannel();
	}
	
	public void delete() {
		internal.get().delete();
	}

	@Override
	public void clearCache() {
		super.clearCache();
		childCache.clear();
		mimeCache.clear();
		internal.clear();
	}
	
}
