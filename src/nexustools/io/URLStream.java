/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.net.URL;
import java.util.WeakHashMap;
import nexustools.io.SubStream.Range;

/**
 *
 * @author katelyn
 */
public class URLStream extends InputAdaptorStream {
	
	private static final WeakHashMap<URL, URLStream> instanceCache = new WeakHashMap();
	
	protected static URLStream getInstance(URL url) throws IOException {
		URLStream urlStream = instanceCache.get(url);
		if(urlStream == null) {
			urlStream = new URLStream(url);
			instanceCache.put(url, urlStream);
		}
		return urlStream;
	}
	
	public static SubStream getStream(String url) throws IOException {
		return getStream(new URL(url));
	}
	
	public static SubStream getStream(String url, Range range) throws IOException {
		return getStream(new URL(url), range);
	}
	
	public static SubStream getStream(URL url, Range range) throws IOException {
		return getInstance(url).createSubSectorStream(range);
	}
	
	public static SubStream getStream(URL url) throws IOException {
		return getInstance(url).createSubSectorStream();
	}

	private final String url;
	protected URLStream(URL url) throws IOException {
		super(url.openStream());
		this.url = url.toString();
	}

	@Override
	public String getURL() {
		return url;
	}
	
}
