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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.io.monitor.ChannelMonitor;
import net.nexustools.tasks.TaskSink;
import net.nexustools.utils.Creator;
import net.nexustools.utils.Handler;
import net.nexustools.utils.IOUtils;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.RefreshingCache;
import net.nexustools.utils.StringUtils;

/**
 *
 * @author katelyn
 */
public abstract class Stream implements Iterable<Stream> {
	
	public static final HashMap<String,String> mimeForExt = new HashMap<String,String>() {
		{
			put("txt", "text/plain");
			put("json", "text/json");
			put("xml", "text/xml");
			put("yml", "text/yml");
			
			put("css", "text/css");
			put("js", "application/javascript");
			put("html", "text/html");
			put("htm", "text/html");
			
			put("png", "image/png");
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("tiff", "image/tiff");
			put("gif", "image/gif");
			put("bmp", "image/bmp");
		}
	};
	
	/**
	 * Returns the NullStream instance
	 * 
	 * @see NullStream.instance
	 * @return
	 */
	public static final NullStream Null() {
		return NullStream.instance();
	}
	
	/**
	 * Returns the VoidStream instance
	 * 
	 * @see VoidStream.instance
	 * @return
	 */
	public static final VoidStream Void() {
		return VoidStream.instance();
	}
	
	private static final HashMap<String, StreamProvider> providers = new HashMap<String, StreamProvider>() {
		{
			put("memory", new StreamProvider() {
				@Override
				public String scheme() {
					return "memory";
				}
				@Override
				public Stream open(String path, URI raw) {
					if(path.length() > 0)
						return new MemoryStream(Integer.valueOf(path.split("@")[0]));
					else
						return new MemoryStream();
				}
			});
			put("null", new StreamProvider() {
				@Override
				public String scheme() {
					return "null";
				}
				@Override
				public Stream open(String path, URI raw) {
					return Null();
				}
			});
			put("tcp", new StreamProvider() {
				@Override
				public String scheme() {
					return "tcp";
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					return SocketStream.open(raw.getHost(), raw.getPort(), SocketStream.Type.TCP);
				}
			});
			put("udp", new StreamProvider() {
				@Override
				public String scheme() {
					return "udp";
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					return SocketStream.open(raw.getHost(), raw.getPort(), SocketStream.Type.UDP);
				}
			});
			put("unix", new StreamProvider() {
				@Override
				public String scheme() {
					return "unix";
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					return SocketStream.open(raw.getHost(), raw.getPort(), SocketStream.Type.Local);
				}
			});
			put("void", new StreamProvider() {
				@Override
				public String scheme() {
					return "void";
				}
				@Override
				public Stream open(String path, URI raw) {
					return Void();
				}
			});
			put("file", new StreamProvider() {
				@Override
				public String scheme() {
					return "file";
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					return FileStream.instance(path);
				}
			});
			/*put("substream", new StreamProvider() {
				final Pattern substreamPattern = Pattern.compile("^(\\d+)\\-(\\d+)@(.+)$");
				@Override
				public String scheme() {
					return "substream";
				}
				@Override
				public Stream open(String path, URI raw, boolean supportWriting) throws IOException {
					Matcher matcher = substreamPattern.matcher(path);
					if(matcher.matches())
						return Stream.open(matcher.group(3), supportWriting).createSubSectorStream(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)));
					else
						throw new IOException("Malformed substream:" + path);
				}
			});*/
			put("resource", new StreamProvider() {
				@Override
				public String scheme() {
					return "resource";
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					URL resource = Stream.class.getResource(path);
					if(resource == null)
						throw new IOException("No such resource found: " + path);
					try {
						return Stream.synthesize(resource.toExternalForm(), raw.toString(), "ResourceSynth");
					} catch (URISyntaxException ex) {
						throw new RuntimeException(ex);
					}
				}
			});
			put("input", new StreamProvider() {
				@Override
				public String scheme() {
					return "input";
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					return Stream.open(path);
				}
			});
		}
	};
	private final static StrongTypeList<StreamProvider> fallbackProviders = new StrongTypeList<StreamProvider>() {
		{
			push(new StreamProvider() {
				@Override
				public String scheme() {
					return null;
				}
				@Override
				public Stream open(String path, URI raw) throws IOException {
					return URLStream.getStream(raw);
				}
			});
		}
	};
	
	public static String uriForPath(String filePath) {
		URI uri = (new File(filePath)).toURI();
		return uri.toString();
	}
	
	/**
	 * Registers a new {@link StreamProvider}
	 * Stream providers provide streams by parsing URIs passed to
	 * @see Stream.open
	 * 
	 * @param provider
	 */
	public static void registerProvider(StreamProvider provider) {
		String protocol = provider.scheme();
		if(protocol == null)
			fallbackProviders.push(provider);
		else
			providers.put(protocol, provider);
	}

	/**
	 * Parse and attempt to return a new Stream
	 * using a registered {@link StreamProvider}.
	 * 
	 * This method is also capable of unwrapping {@link InputStream}
	 * and {@link DataInputStream}s returned by a Stream to return
	 * the original Stream
	 * 
	 * @param uri URI String to parse
	 * @return A Stream compatible with the URL String given
	 * @throws IOException
	 */
	public static Stream open(String uri) throws IOException {
		try {
			return open(new URI(uri));
		} catch(URISyntaxException ex) {
			return FileStream.instance(uri);
		}
	}

	/**
	 * Parse and attempt to return a new Stream
	 * using a registered {@link StreamProvider}.
	 * 
	 * This method is also capable of unwrapping {@link InputStream}
	 * and {@link DataInputStream}s returned by a Stream to return
	 * the original Stream
	 * 
	 * @param uri URI to open
	 * @param supportWriting Whether or not the Stream needs to support writing
	 * @return A Stream compatible with the URL String given
	 * @throws IOException
	 */
	public static Stream open(URI uri) throws IOException {
		if(uri.getScheme() != null) {
			{
				StreamProvider provider = providers.get(uri.getScheme());
				if(provider != null)
					return provider.open(uri.getPath(), uri);
			}
			
			for(StreamProvider provider : fallbackProviders)
				try {
					return provider.open(uri.getPath(), uri);
				} catch(UnsupportedOperationException ex) {} // Ignore incompatible streams
			
			throw new IOException("No handler found for URI: " + uri.toString());
		} else
			return FileStream.instance(uri.getPath());
	}
	
	public static Stream open(File file) throws IOException {
		return FileStream.instance(file.getAbsolutePath());
	}
	
	/**
	 * Synthesizes a new Stream by pretending to be another type of Stream
	 * This method is primarily useful for aliases such as resource:
	 * 
	 * @param effectiveURL URL to open and use as the underlying Stream
	 * @param reportedURL URL to report the Synthesized Stream is using
	 * @param name Name of the Synthesized Stream
	 * @return
	 * @throws IOException
	 */
	protected static Stream synthesize(String effectiveURL, final String reportedURL, final String name) throws IOException, URISyntaxException {
		Stream stream = open(effectiveURL).effectiveStream();
		final URI reportedURI = new URI(reportedURL);
		return new SubStream(stream) {
			@Override
			public String scheme() {
				return reportedURI.getScheme();
			}
			@Override
			public String path() {
				return reportedURI.getPath();
			}
			@Override
			public String toURL() {
				return reportedURL;
			}
			@Override
			public String toString() {
				return stringForStream(this, Stream.class.getName() + "$" + name);
			}
		};
	}

	/**
	 * Opens a Stream and creates a new InputStream
	 * 
	 * @throws java.net.URISyntaxException
	 * @see Stream.open
	 * @param uri URI String to open
	 * @return
	 * @throws IOException
	 */
	public static InputStream openInputStream(String uri) throws IOException, URISyntaxException {
		return open(uri).createInputStream();
	}

	public static OutputStream openOutputStream(String uri) throws IOException, URISyntaxException {
		return open(uri).createOutputStream();
	}

	/**
	 * Copy one stream to another.
	 * 
	 * @param from Stream to copy from
	 * @param to Stream to copy to
	 * @throws IOException
	 * @throws java.net.URISyntaxException
	 */
	public static void copy(String from, String to) throws IOException, URISyntaxException {
		copy(Stream.open(from), Stream.open(to));
	}

	/**
	 * Copy one stream to another.
	 * 
	 * @param from Stream to copy from
	 * @param to Stream to copy to
	 * @throws IOException
	 */
	public static void copy(Stream from, Stream to) throws IOException {
		IOUtils.copyStream(from.createInputStream(),
							to.createOutputStream());
	}

	public static void bindSynthScheme(final String scheme, String path) {
		final String uriForPath = path.endsWith("/") ? path : path + "/";
		providers.put(scheme, new StreamProvider() {
			@Override
			public String scheme() {
				return scheme;
			}
			@Override
			public Stream open(String path, URI raw) throws IOException {
				try {
					return Stream.synthesize(uriForPath + URLEncoder.encode(path.substring(1), "UTF-8").replace("+", "%20").replace("%2F", "/"), raw.toString(), "Alias[" + scheme + "]");
				} catch (URISyntaxException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}

	public static void remove(String scheme) {
		providers.remove(scheme);
	}

	/**
	 * Indicates whether or not this Stream supports writing
	 * 
	 * @return
	 */
	public abstract boolean canWrite(); 
	
	public abstract boolean canRead();
	
	/**
	 * Returns the Size of the content in this Stream.
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract long size() throws UnsupportedOperationException;
	
	public long created() throws UnsupportedOperationException {
		return 0;
	}
	public long lastModified() throws UnsupportedOperationException {
		return 0;
	}
	
	public Map<String,String> properties() {
		return new HashMap();
	}
	
	public boolean isHidden() throws UnsupportedOperationException{
		return true;
	}
	public boolean exists() throws UnsupportedOperationException{
		return true;
	}
	
	private final RefreshingCache<String> sizeStrCache = new RefreshingCache<String>(new Creator<String, Void>() {
		public String create(java.lang.Void using) {
			StringBuilder builder = new StringBuilder();
			try {
				builder.append(StringUtils.stringForSize(size()));
			} catch(UnsupportedOperationException ex) {}
			if(isDirectory()) {
				int folderCount = 0;
				int fileCount = 0;

				for(Stream child : Stream.this) {
					if(child.isDirectory())
						folderCount ++;
					else
						fileCount ++;
				}
				if(fileCount > 0) {
					if(builder.length() > 0)
						builder.append(", ");
					builder.append(fileCount);
					builder.append(" files");
				}
				if(folderCount > 0) {
					if(builder.length() > 0)
						builder.append(", ");
					builder.append(folderCount);
					builder.append(" folders");
				}
				if(builder.length() < 1)
					builder.append("Empty");
			}

			if(builder.length() < 1)
				builder.append("Cannot determine size");

			return builder.toString();
		}
	});
	public String sizeStr() throws IOException{
		if(!canRead())
			throw new IOException("Permission Denied");
		
		return sizeStrCache.get();
	}
	
	public final void read(StreamReader<InputStream> reader) throws IOException {
		InputStream in = createInputStream();
		try {
			reader.read(in);
		} finally {
			in.close();
		}
	}
	
	public final void readData(StreamReader<? super DataInputStream> reader) throws IOException {
		DataInputStream in = createDataInputStream();
		try {
			reader.read(in);
		} finally {
			in.close();
		}
	}
	
	public final void readNonBlocking(final NBStreamProcessor reader) throws IOException, UnsupportedOperationException {
		try {
			reader.register((SelectableChannel)createChannel());
		} catch(Throwable t) {
			// TODO: Create fallback Threaded implementation
			throw new UnsupportedOperationException(t);
		}
	}
	
	public final void write(StreamWriter<OutputStream> writer) throws IOException {
		OutputStream out = createOutputStream();
		writer.write(out);
		out.close();
	}
	
	public final void writeData(StreamWriter<DataOutputStream> writer) throws IOException {
		DataOutputStream out = createDataOutputStream();
		writer.write(out);
		out.close();
	}
	
	public final DataInputStream createDataInputStream(long pos) throws IOException{
		return new DataInputStream(createInputStream(pos));
	}
	public final DataInputStream createDataInputStream() throws IOException{
		return createDataInputStream(0);
	}
	public final DataOutputStream createDataOutputStream(long pos) throws IOException{
		return new DataOutputStream(createOutputStream(pos));
	}
	public final DataOutputStream createDataOutputStream() throws IOException{
		return createDataOutputStream(0);
	}
	
	public final Appendable createAppendable() throws IOException{
		return new Appendable() {
			final OutputStream oStream = createOutputStream();
			public Appendable append(CharSequence cs) throws IOException {
				return append(cs, 0, cs.length());
			}
			public Appendable append(CharSequence cs, int offset, int len) throws IOException {
				byte[] buff = new byte[len];
				for(int i=0; i<len; i++)
					buff[i] = (byte)cs.charAt(offset+i);
				oStream.write(buff);
				return this;
			}
			public Appendable append(char c) throws IOException {
				oStream.write(c);
				return this;
			}
		};
	}
	public final Appendable createAppendable(final Charset charset) throws IOException{
		return new Appendable() {
			final OutputStream oStream = createOutputStream();
			public Appendable append(CharSequence cs) throws IOException {
				return append(cs, 0, cs.length());
			}
			public Appendable append(CharSequence cs, int offset, int len) throws IOException {
				char[] buff = new char[len];
				for(int i=0; i<len; i++)
					buff[i] = cs.charAt(offset+i);
				oStream.write(new String(buff).getBytes(charset));
				return this;
			}
			public Appendable append(char c) throws IOException {
				oStream.write(c);
				return this;
			}
		};
	}

	public final InputStream createInputStream() throws IOException{
		return createInputStream(0);
	}
	public final OutputStream createOutputStream() throws IOException{
		return createOutputStream(-1);
	}
	public abstract InputStream createInputStream(long pos) throws IOException;
	public abstract OutputStream createOutputStream(long pos) throws IOException;
	
	public void read(Handler<InputStream> readProcessor) throws IOException {
		InputStream in = createInputStream();
		try {
		 	readProcessor.handle(in);
		} catch (Throwable ex) {
			throw NXUtils.unwrapIOException(ex);
		} finally {
			in.close();
		}
	}
	
	public void write(Handler<OutputStream> writeProcessor) throws IOException {
		OutputStream out = createOutputStream();
		try {
		 	writeProcessor.handle(out);
		} catch (Throwable ex) {
			throw NXUtils.unwrapIOException(ex);
		} finally {
			out.close();
		}
	}
	
	
	/**
	 * Returns the Effective Stream.
	 * 
	 * Subclasses can re-implement this to point to the real Stream they are
	 * reading from, when the Stream itself isn't really what matters.
	 * 
	 * @return
	 */
	public Stream effectiveStream() {
		return this;
	}

	/**
	 * Return a URL String for this Stream
	 * 
	 * @see Stream.open
	 * @return
	 */
	public String toURL() {
		return toURI().toString();
	}

	/**
	 * Return a URL String for this Stream
	 * 
	 * @see Stream.open
	 * @return
	 */
	public URI toURI() {
		try {
			return new URI(scheme(), null, path(), null);
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public abstract String scheme();
	public abstract String path();
	
	/**
	 * Provides the default toString implementation for Streams
	 * 
	 * @param stream
	 * @return
	 */
	protected static String stringForStream(Stream stream) {
		return stringForStream(stream, stream.getClass().getName());
	}
	
	/**
	 * Provides the default toString implementation for Streams
	 * 
	 * @param stream
	 * @param name
	 * @return
	 */
	protected static String stringForStream(Stream stream, String name) {
		Map<String,String> properties = stream.properties();
		return stringForStream(stream, name, properties);
	}
	
	/**
	 * Provides the default toString implementation for Streams
	 * 
	 * @param stream
	 * @param name
	 * @return
	 */
	protected static String stringForStream(Stream stream, String name, Map<String,String> properties) {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append('(');
		builder.append(stream.toURL());
		for(Map.Entry<String,String> entry : properties.entrySet()) {
			builder.append(',');
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(entry.getValue());
		}
		builder.append(')');
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return stringForStream(this);
	}
	
	/**
	 * Parses the extension from this Stream's URL String
	 * 
	 * @return
	 */
	public final String extension() {
		String url = toURL();
		int lastPos = url.lastIndexOf('.');
		if(lastPos > -1)
			return url.substring(lastPos+1);
		return null;
	}
	
	/**
	 * Returns the MimeType for the content of this Stream,
	 * if it can't be determined, null is returned.
	 * 
	 * @return MimeType or null
	 */
	public String mimeType() {
		String extension = extension();
		if(extension != null) {
			String mime = mimeForExt.get(extension);
			if(mime != null)
				return mime;
		}
		
		return "application/octet-stream";
	}
	
	public Stream clone() throws CloneNotSupportedException {
		try {
			Stream stream;
			try {
				stream = Stream.open(toString());
			} catch(IOException t) {
				stream = Stream.open(toURL());
			} catch(RuntimeException t) {
				stream = Stream.open(toURL());
			}
			return stream;
		}catch (IOException ex) {
			throw new CloneNotSupportedException("Cannot clone `" + toString() + "`.");
		}
	}

	public Iterator<Stream> iterator() {
		Iterator<String> chIT = null;
		try {
			chIT = children().iterator();
		} catch(IOException ex) {}
		
		if(chIT != null && chIT.hasNext()) {
			final Iterator<String> it = chIT;
			return new Iterator<Stream>() {
				Stream next;
				String parentURL;
				{
					parentURL = toURL();
					if(!parentURL.endsWith("/"))
						parentURL += "/";
				}
				public boolean hasNext() {
					while(next == null && it.hasNext()) {
						try {
							next = Stream.open(parentURL + it.next());
						} catch(Exception ex) {}
					}
					return next != null;
				}
				public Stream next() {
					try {
						return next;
					} finally {
						next = null;
					}
				}
				public void remove() {
					throw new UnsupportedOperationException("Not supported.");
				}
			};
		} else
			return new Iterator<Stream>() {
				public boolean hasNext() {
					return false;
				}
				public Stream next() {
					throw new UnsupportedOperationException(toString() + " has no children");
				}
				public void remove() {
					throw new UnsupportedOperationException("Not supported.");
				}
			};
	}
	
	public abstract ByteChannel createChannel() throws UnsupportedOperationException, IOException;
	
	public Iterable<String> children() throws IOException {
		throw new IOException(toURL() + " has no children");
	}
	
	public void clearCache() {
		sizeStrCache.clear();
	}
	
	public boolean isDirectory() {
		return false;
	}
	
	public void close() {}
	
}
