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
import java.util.WeakHashMap;
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
public class URLStream extends Stream {
	
	private static final WeakHashMap<String, URLStream> instanceCache = new WeakHashMap();
	private final URL internal;
	
	protected static URLConnection open(URL url, boolean head) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setDoOutput(!head);
		connection.setDoInput(false);
		Logger.debug("Opening URLConnection", connection);
		connection.connect();
		return connection;
	}
	
	protected URLStream(String path) throws MalformedURLException {
		this.internal = new URL(path);
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
	public long size() throws IOException {
		try {
			return detectedConnectionLength.read(new SoftWriteReader<Long, PropAccessor<Creator<Long, URLConnection>>>() {
				@Override
				public Long soft(PropAccessor<Creator<Long, URLConnection>> data) throws IOException {
					return data.get().create(open(internal, true));
				}
				@Override
				public Long read(PropAccessor<Creator<Long, URLConnection>> data) throws IOException {
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
					return creator.create(open(internal, true));
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapIOException(ex);
		}
	}

	@Override
	public boolean canWrite() {
		return false;
	}
	
	@Override
	public boolean exists() throws IOException {
		throw new IOException("Existance cannot be determined");
	}
	
	@Override
	public Iterable<String> children() throws IOException {
		throw new IOException("Children cannot be determined");
	}
	
	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public long lastModified() throws IOException {
		throw new IOException("Last modified cannot be determined.");
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public InputStream createInputStream(long p) throws IOException {
		InputStream inStream = open(internal, false).getInputStream();
		inStream.skip(p);
		return inStream;
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
