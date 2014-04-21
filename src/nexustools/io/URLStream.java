/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.WeakHashMap;
import nexustools.io.SubStream.Range;

/**
 *
 * @author katelyn
 */
public class URLStream extends LocalCacheStream {
	
	private static final WeakHashMap<URL, URLStream> instanceCache = new WeakHashMap();
	
	private final long size;
	protected static Stream getInstance(URL url) throws IOException {
		URLStream urlStream = instanceCache.get(url);
		if(urlStream == null) {
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

	private final String url;
	private final String mimetype;
	protected URLStream(URLConnection con, String url) throws IOException {
		super(con.getInputStream());
		size = con.getContentLengthLong();
		mimetype = con.getContentType();
		this.url = url;
	}
	
	protected URLStream(URL url) throws IOException {
		this(url.openConnection(), url.toExternalForm());
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public long size() throws IOException {
		if(size > 0)
			return size;
		return super.size();
	}

	@Override
	public String getMimeType() {
		if(mimetype != null)
			return mimetype;
		
		return super.getMimeType();
	}
	
}
