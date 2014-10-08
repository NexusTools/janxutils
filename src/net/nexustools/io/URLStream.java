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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ByteChannel;
import java.util.WeakHashMap;
import java.util.logging.Level;

import net.nexustools.concurrent.Prop;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.accessor.PropAccessor;
import net.nexustools.utils.Creator;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.RuntimeTargetException;
import net.nexustools.utils.log.Logger;

/**
 * A Stream which allows reading/writing using a {@link RandomAccessFile}.
 * 
 * @author katelyn
 */
public class URLStream extends Stream {

	private static final WeakHashMap<String, URLStream> instanceCache = new WeakHashMap();
	private static final PropMap<String, URLConnection> connectionCache = new PropMap();
	private final URL internal;
	private final String uri;

	protected URLConnection open() throws IOException {
		return open(false);
	}
	private URLConnection open0() throws IOException {
		URLConnection connection = internal.openConnection();
		connection.setUseCaches(true);
		connection.setDoOutput(false);
		connection.setDoInput(true);
		Logger.debug("Opening URLConnection", connection);
		connection.connect();
		return connection;
	}
	protected URLConnection open(final boolean take) throws IOException {
		try {
			if(take) {
				URLConnection connection = connectionCache.take(uri);
				if(connection == null)
					connection = open0();
				return connection;
			} else
				return connectionCache.read(new SoftWriteReader<URLConnection, MapAccessor<String,URLConnection>>() {
					public boolean test(MapAccessor<String,URLConnection> against) {
						return !against.has(uri);
					};
					public URLConnection read(MapAccessor<String,URLConnection> data) {
						URLConnection connection;
						try {
							connection = open0();
						} catch (IOException ex) {
							throw NXUtils.wrapRuntime(ex);
						}
						data.put(uri, connection);
						return connection;
					};
					public URLConnection soft(MapAccessor<String,URLConnection> data) {
						return data.get(uri);
					};
				});
		} catch(RuntimeTargetException ex) {
			throw NXUtils.unwrapTarget(IOException.class, ex);
		}		
	}
	
	protected URLStream(String uri) throws MalformedURLException {
		this.internal = new URL(uri);
		this.uri = uri;
	}
	
	@Override
	public String scheme() {
		return internal.getProtocol();
	}
	
	@Override
	public String path() {
		return internal.getPath();
	}
	
	public static Stream getStream(URI url) throws MalformedURLException {
		return getStream(url.toString());
	}
	public static synchronized Stream getStream(String url) throws MalformedURLException {
		URLStream fileStream;
		fileStream = instanceCache.get(url);
		if(fileStream == null) {
			fileStream = new URLStream(url);
			instanceCache.put(url, fileStream);
		}
		
		return fileStream;
	}

	static final Prop<Creator<Long, URLConnection>> detectedConnectionLength = new Prop<Creator<Long, URLConnection>>();
	@Override
	public long size() {
		try {
			long size = detectedConnectionLength.read(new SoftWriteReader<Long, PropAccessor<Creator<Long, URLConnection>>>() {
				@Override
				public Long soft(PropAccessor<Creator<Long, URLConnection>> data) {
					try {
						return data.get().create(open());
					} catch (IOException ex) {
						throw NXUtils.wrapRuntime(ex);
					}
				}
				@Override
				public Long read(PropAccessor<Creator<Long, URLConnection>> data) {
					Creator<Long, URLConnection> creator;
					try {
						creator = new Creator<Long, URLConnection>() {
							final Method getContentLengthLong = URLConnection.class.getDeclaredMethod("getContentLengthLong");
							{
								Logger.debug("Detected Java 7 APIs", getContentLengthLong);
							}
							public Long create(URLConnection using) {
								try {
									return (Long)getContentLengthLong.invoke(using);
								} catch (IllegalAccessException ex) {
									throw NXUtils.wrapRuntime(ex);
								} catch (IllegalArgumentException ex) {
									throw NXUtils.wrapRuntime(ex);
								} catch (InvocationTargetException ex) {
									throw NXUtils.wrapRuntime(ex);
								}
							}
						};
					} catch(Throwable t) {
						Logger.exception(Logger.Level.Gears, t);
						creator = new Creator<Long, URLConnection>() {
							public Long create(URLConnection using) {
								return (long)using.getContentLength();
							}
						};
					}
					try {
						return creator.create(open());
					} catch (IOException ex) {
						throw NXUtils.wrapRuntime(ex);
					}
				}
			});
			if(size > -1)
				return size;
			throw new UnsupportedOperationException("Server not sending size");
		} catch (RuntimeTargetException ex) {
			throw NXUtils.unwrapOperationException(ex);
		}
	}

	@Override
	public boolean canWrite() {
		return false;
	}
	
	@Override
	public boolean exists() {
		try {
			open();
		} catch(FileNotFoundException t) {
			return false;
		} catch (IOException ex) {
			throw new UnsupportedOperationException(ex);
		}
		return true;
	}
	
	@Override
	public Iterable<String> children() throws IOException {
		throw new IOException("Children cannot be determined");
	}
	
	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String mimeType() {
		String type = null;
		try {
			type = open().getContentType();
		} catch (IOException ex) {}
		if(type == null)
			type = super.mimeType();
		return type;
	}

	@Override
	public long lastModified() {
		long lastModified;
		try {
			lastModified = open().getLastModified();
		} catch (IOException ex) {
			throw new UnsupportedOperationException(ex);
		}
		if(lastModified > 0)
			return lastModified;
		throw new UnsupportedOperationException("Server is not sending a modification time");
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public InputStream createInputStream(long p) throws IOException {
		InputStream inStream = open(true).getInputStream();
		inStream.skip(p);
		return inStream;
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ByteChannel createChannel() throws UnsupportedOperationException, IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
