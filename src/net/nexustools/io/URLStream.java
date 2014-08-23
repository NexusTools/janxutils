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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.WeakHashMap;
import net.nexustools.io.SubStream.Range;
import net.nexustools.utils.Hasher;
import net.nexustools.utils.log.Logger;

/**
 * A Stream that provides reading/writing from a {@link URL}.
 * 
 * @author katelyn
 */
public class URLStream extends CachingStream {
	
	private static final WeakHashMap<URL, URLStream> instanceCache = new WeakHashMap();
	
	protected static synchronized Stream getInstance(URL url) throws IOException {
		URLStream urlStream = instanceCache.get(url);
		if(urlStream == null) {
			Logger.debug("Opening URLStream: " + url);
			urlStream = new URLStream(url);
			instanceCache.put(url, urlStream);
		}
		return urlStream;
	}
	
	public static Stream getStream(String url) throws IOException {
		return getStream(new URL(url));
	}
	
	public static Stream getStream(String url, Range range) throws IOException {
		return getStream(new URL(url), range);
	}
	
	public static Stream getStream(URL url, Range range) throws IOException {
		return getInstance(url).createSubSectorStream(range);
	}
	
	public static Stream getStream(URL url) throws IOException {
		return getInstance(url).createSubSectorStream();
	}
	
	public static Stream getStream(URI uri, Range range) throws IOException {
		return getStream(uri.toURL(), range);
	}
	
	public static Stream getStream(URI uri) throws IOException {
		return getStream(uri.toURL());
	}
	
	private static String cacheDir = null;
	protected static CacheInfo getCacheSet(URL url) throws IOException {
		URLConnection con = url.openConnection();
		CachingStream.CacheInfo cacheSet = null;
		while(true) {
			if(url.getUserInfo() != null || !(con instanceof HttpURLConnection))
				break;

			HttpURLConnection httpCon = (HttpURLConnection)con;
			if(cacheDir == null) {
				cacheDir = System.getProperty("user.home") + File.separator + ".cache" + File.separator + "url-streams" + File.separator;
				File cacheFile = new File(cacheDir);
				if(!cacheFile.exists() && !cacheFile.mkdirs())
					throw new RuntimeException("Failed to create cache directory: " + cacheDir);
			}
			String cachePath = cacheDir + url.getProtocol() + File.separator + url.getHost() + File.separator;
			{
				File cacheFile = new File(cachePath);
				if(!cacheFile.exists() && !cacheFile.mkdirs())
					throw new RuntimeException("Failed to create cache directory: " + cachePath);
			}
			URI uri;
			try {
				uri = url.toURI();
			} catch (URISyntaxException ex) {
				break;
			}
			{
				String cacheName = uri.getRawPath();
				if(url.getQuery() != null)
					cacheName += '?' + uri.getRawQuery();
				cachePath += Hasher.getSHA256(cacheName);
			}

			String eTag;
			String lastModified;
			httpCon.setUseCaches(false);
			FileStream cacheStream = null;
			if((new File(cachePath)).exists()) {
				cacheStream = new FileStream(cachePath + ".meta");
				DataInputStream dataInputStream = cacheStream.createDataInputStream();
				lastModified = dataInputStream.readUTF8();
				eTag = dataInputStream.readUTF8();

				if(lastModified != null)
					httpCon.setRequestProperty("If-Modified-Since", lastModified);
				if(eTag != null)
					httpCon.setRequestProperty("If-None-Match", eTag);

				httpCon.setReadTimeout(500);
				httpCon.setConnectTimeout(800);
				int response;
				try {
					httpCon.connect();
					response = httpCon.getResponseCode();
				} catch(IOException ex) {
					ex.printStackTrace(System.err);
					Logger.debug("Using cache...");
					response = 304; // Pretend it was cached if the connection fails
				}
				if(response == 304) {
					cacheSet = new CachingStream.CacheInfo(FileStream.getStream(cachePath));
					cacheSet.mimetype = dataInputStream.readUTF8();
					cacheSet.size = dataInputStream.readLong();
					return cacheSet;
				}
			} else {
				httpCon.setReadTimeout(1200);
				httpCon.setConnectTimeout(1600);
				httpCon.connect();
			}

			eTag = con.getHeaderField("ETag");
			lastModified = con.getHeaderField("Last-Modified");
			if(cacheStream != null)
				cacheStream.close();
			if(eTag != null || lastModified != null) {
				cacheStream = new FileStream(cachePath + ".meta", true);

				DataOutputStream dataOutputStream = cacheStream.createDataOutputStream();
				dataOutputStream.writeUTF8(lastModified);
				dataOutputStream.writeUTF8(eTag);
				
				dataOutputStream.writeUTF8(con.getContentType());
				dataOutputStream.writeLong(con.getContentLength());
				cacheStream.close();
				
				cacheStream = new FileStream(cachePath, true);
			} else
				break;

			cacheSet = new CachingStream.CacheInfo(cacheStream, con.getInputStream());
			break;
		}
		
		if(cacheSet == null)
			cacheSet = new CachingStream.CacheInfo(con.getInputStream());
		
		cacheSet.mimetype = con.getContentType();
		cacheSet.size = con.getContentLength();
		
		Logger.debug(cacheSet);
		return cacheSet;
	}

	private final URL url;
	protected URLStream(URL url) throws IOException {
		super(getCacheSet(url));
		this.url = url;
	}

	@Override
	public String scheme() {
		return url.getProtocol();
	}

	@Override
	public String path() {
		return url.getPath();
	}

	@Override
	public String toURL() {
		return url.toExternalForm();
	}
	
}
