/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nexustools.utils.IOUtils;

/**
 *
 * @author katelyn
 */
public abstract class Stream {
	
	/**
	 * Returns the NullStream instance
	 * 
	 * @see NullStream.getInstance
	 * @return
	 */
	public static final NullStream Null() {
		return NullStream.getInstance();
	}
	
	private static final HashMap<String, StreamProvider> providers = new HashMap() {
		{
			put("memory", new StreamProvider() {

				@Override
				public String protocol() {
					return "memory";
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) {
					if(path.length() > 0)
						return new MemoryStream(Integer.valueOf(path.split("@")[0]));
					else
						return new MemoryStream();
				}
			});
			put("null", new StreamProvider() {

				@Override
				public String protocol() {
					return "null";
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) {
					return Null();
				}
			});
			put("file", new StreamProvider() {

				@Override
				public String protocol() {
					return "file";
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) throws IOException {
					return FileStream.getStream(path, supportWriting);
				}
				
			});
			put("substream", new StreamProvider() {

				final Pattern substreamPattern = Pattern.compile("^(\\d+)\\-(\\d+)@(.+)$");
				
				@Override
				public String protocol() {
					return "substream";
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) throws IOException {
					Matcher matcher = substreamPattern.matcher(path);
					if(matcher.matches())
						return Stream.open(matcher.group(3), supportWriting).createSubSectorStream(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)));
					else
						throw new IOException("Malformed substream:" + path);
				}
			});
			put("resource", new StreamProvider() {

				@Override
				public String protocol() {
					return "resource";
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) throws IOException {
					if(supportWriting)
						throw new UnsupportedOperationException("Resources do not support writing.");
					
					URL resource = Stream.class.getResource(path);
					if(resource == null)
						throw new IOException("No such resource found: " + path);
					
					return Stream.synthesize(resource.toExternalForm(), "resource:" + raw, "ResourceSynth");
				}
				
			});
			put("input", new StreamProvider() {
				
				@Override
				public String protocol() {
					return "input";
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) throws IOException {
					if(supportWriting)
						throw new UnsupportedOperationException("Input Streams do not support writing by their nature.");
					return Stream.open(path);
				}
			});
		}
	};
	private final static ArrayList<StreamProvider> fallbackProviders = new ArrayList() {
		{
			add(new StreamProvider() {

				@Override
				public String protocol() {
					return null;
				}

				@Override
				public Stream open(String path, String raw, boolean supportWriting) throws IOException {
					if(supportWriting)
						throw new UnsupportedOperationException("URLStreams do not support writing yet.");
					return URLStream.getStream(raw);
				}
				
			});
		}
	};
	
	static {
		for(StreamProvider provider : ServiceLoader.load(StreamProvider.class))
			registerProvider(provider);
	}
	
	/**
	 * Registers a new {@link StreamProvider}
	 * Stream providers provide streams by parsing URIs passed to
	 * @see Stream.open
	 * 
	 * @param provider
	 */
	public static void registerProvider(StreamProvider provider) {
		String protocol = provider.protocol();
		if(protocol == null)
			fallbackProviders.add(provider);
		else
			providers.put(protocol, provider);
	}

	private static final Pattern urlPattern = Pattern.compile("^(\\w+):(.+)$");
	private static final Pattern wrapperPattern = Pattern.compile("^([^\\(]+)\\((.+)\\)$");

	/**
	 * Parse and attempt to return a new Stream
	 * using a registered {@link StreamProvider}.
	 * 
	 * This method is also capable of unwrapping {@link InputStream}
	 * and {@link DataInputStream}s returned by a Stream to return
	 * the original Stream
	 * 
	 * @param url URL String to parse
	 * @return A Stream compatible with the URL String given
	 * @throws IOException
	 */
	public static Stream open(String url) throws IOException {
		return open(url, false);
	}

	/**
	 * Parse and attempt to return a new Stream
	 * using a registered {@link StreamProvider}.
	 * 
	 * This method is also capable of unwrapping {@link InputStream}
	 * and {@link DataInputStream}s returned by a Stream to return
	 * the original Stream
	 * 
	 * @param url URL String to parse
	 * @param supportWriting Whether or not the Stream needs to support writing
	 * @return A Stream compatible with the URL String given
	 * @throws IOException
	 */
	public static Stream open(String url, boolean supportWriting) throws IOException {
		Matcher matcher;
		while(true) {
			matcher = wrapperPattern.matcher(url);
			if(matcher.matches())
				url = matcher.group(2);
			else
				break;
		}
		
		System.out.println("Opening Stream: " + url);
		matcher = urlPattern.matcher(url);
		if(matcher.matches()) {
			String path = URLDecoder.decode(matcher.group(2), "UTF-8");//(new URI(null, null, matcher.group(2), null)).getPath();
			{
				StreamProvider provider = providers.get(matcher.group(1));
				if(provider != null)
					return provider.open(path, url, supportWriting);
			}
			
			for(StreamProvider provider : fallbackProviders)
				try {
					return provider.open(path, url, supportWriting);
				} catch(UnsupportedOperationException ex) {} // Ignore incompatible streams
			
			throw new IOException("No handler found for URL: " + url);
		} else
			return FileStream.getStream(url);
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
	protected static Stream synthesize(String effectiveURL, final String reportedURL, final String name) throws IOException {
		Stream stream = open(effectiveURL);
		while(stream instanceof SubStream)
			stream = ((SubStream)stream).getEffectiveStream();
		
		return new SubStream(stream) {

			@Override
			public String getURL() {
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
	 * @see Stream.open
	 * @param url URL String to open
	 * @return
	 * @throws IOException
	 */
	public static InputStream openInputStream(String url) throws IOException {
		return open(url).createInputStream();
	}

	/**
	 * Opens a Stream and returns a new {@link DataInputStream}
	 * 
	 * @param url URL String to open
	 * @return
	 * @throws IOException
	 */
	public static DataInputStream openDataInputStream(String url) throws IOException {
		return open(url).createDataInputStream();
	}

	public static OutputStream openOutputStream(String url) throws IOException {
		return open(url).createOutputStream();
	}

	public static DataOutputStream openDataOutputStream(String url) throws IOException {
		return open(url).createDataOutputStream();
	}

	/**
	 * Copy one stream to another.
	 * 
	 * @param from Stream to copy from
	 * @param to Stream to copy to
	 * @throws IOException
	 */
	public static void copy(String from, String to) throws IOException {
		copy(Stream.open(from), Stream.open(to, true));
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
	
	/**
	 * Read bytes from this Stream
	 * 
	 * @param buffer Buffer to read into
	 * @param off Offset to read into
	 * @param len Length of data to try and read
	 * @return Number of bytes read
	 * @throws IOException
	 */
	public abstract int read(byte[] buffer, int off, int len) throws IOException;

	/**
	 * Write bytes to this Stream
	 * 
	 * @param buffer Buffer to write data from
	 * @param off Offset to write from
	 * @param len Length of data to write
	 * @throws IOException
	 */
	public abstract void write(byte[] buffer, int off, int len) throws IOException;

	/**
	 * Indicates whether or not this Stream supports writing
	 * 
	 * @return
	 */
	public abstract boolean canWrite(); // Not all sector streams even support writing

	/**
	 * Flush data to the underlying Stream.
	 * 
	 * This method is rarely implemented and would
	 * only be useful when this Stream uses a buffering mechanism.
	 * 
	 * @throws IOException
	 */
	public abstract void flush() throws IOException;
	
	/**
	 * Position of this Stream within its content.
	 * 
	 * SubStreams can be used to read from the same Stream
	 * at multiple locations without requiring multiple FDs.
	 * 
	 * @see Stream.createSubSectorStream
	 * @return
	 */
	public abstract long pos();

	/**
	 * Seeks this Stream to another position within its content.
	 * 
	 * @param pos
	 * @throws IOException
	 */
	public abstract void seek(long pos) throws IOException;
	
	/**
	 * Returns the Size of the content in this Stream.
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract long size() throws IOException;

	/**
	 * Returns the remaining number of bytes from the current
	 * location to the end of this contnet.
	 * 
	 * Not all Streams know how much content they content
	 * and so this value may change overtime.
	 * 
	 * @return
	 * @throws IOException
	 */
	public long remaining() throws IOException {
		return size() - pos();
	}

	/**
	 * Checks whether or not this Stream is at the end.
	 * 
	 * This method may not be accurate if the implementation
	 * doesn't know the total size of its content.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean atEnd() throws IOException {
		return remaining() <= 0;
	}

	/**
	 * Creates a new {@link SubStream} using the given Range
	 * 
	 * @param range
	 * @return
	 */
	public SubStream createSubSectorStream(SubStream.Range range) {
		return new SubStream(this, range);
	}

	/**
	 * Creates a new {@link SubStream} using the given Range
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public final SubStream createSubSectorStream(long start, long end) {
		return createSubSectorStream(SubStream.createRange(start, end));
	}

	/**
	 * Creates a new {@link SubStream} using the given Range
	 * 
	 * @return
	 */
	public final SubStream createSubSectorStream() {
		return createSubSectorStream(SubStream.getFullRange());
	}

	/**
	 * Creates a new {@link InputStream} for this Stream.
	 * 
	 * Each InputStream uses its own SubStream and so
	 * you can have multiple InputStreams from the same
	 * Stream independent of each other.
	 * 
	 * @return
	 * @throws IOException
	 */
	public final InputStream createInputStream() throws IOException {
		final SubStream subStream = createSubSectorStream();
		return new EfficientInputStream() {
			
			private long marked = -1;

			@Override
			public synchronized void mark(int readlimit) {
				marked = subStream.pos();
			}

			@Override
			public synchronized void reset() throws IOException {
				if(marked > 0) {
					subStream.seek(marked);
					marked = -1;
				} else
					throw new IOException("");
			}

			@Override
			public long skip(long n) throws IOException {
				long lastRemaining = subStream.remaining();
				subStream.seek(subStream.pos() + n);
				return lastRemaining - subStream.remaining();
			}

			@Override
			public boolean markSupported() {
				return true;
			}

			@Override
			public int available() throws IOException {
				return (int)Math.min(subStream.remaining(), Integer.MAX_VALUE);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return subStream.read(b, off, len);
			}
			
			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append(getClass().getName());
				builder.append('(');
				builder.append(getURL());
				builder.append(')');

				return builder.toString();
			}
			
		};
	}
	
	/**
	 * Creates a new {@link DataInputStream} for this Stream.
	 * 
	 * Each DataInputStream uses its own SubStream and so
	 * you can have multiple InputStreams from the same
	 * Stream independent of each other.
	 * @return
	 * @throws IOException
	 */
	public final DataInputStream createDataInputStream() throws IOException {
		return new DataInputStream(createInputStream());
	}
	
	/**
	 * Creates a new {@link OutputStream} for this Stream.
	 * 
	 * Each OutputStream uses its own SubStream and so
	 * you can have multiple InputStreams from the same
	 * Stream independent of each other.
	 * 
	 * @return
	 * @throws IOException
	 */
	public final OutputStream createOutputStream() throws IOException {
		if(!canWrite())
			throw new IOException("This Sector's stream does not support writing...");
		
		final SubStream subStream = createSubSectorStream();
		return new EfficientOutputStream() {

			@Override
			public void flush() throws IOException {
				subStream.flush();
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				subStream.write(b, off, len);
			}
			
			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append(getClass().getName());
				builder.append('(');
				builder.append(getURL());
				builder.append(')');

				return builder.toString();
			}
			
		};
	}
	
	/**
	 * Creates a new {@link DataOutputStream} for this Stream.
	 * 
	 * Each DataOutputStream uses its own SubStream and so
	 * you can have multiple InputStreams from the same
	 * Stream independent of each other.
	 * 
	 * @return
	 * @throws IOException
	 */
	public final DataOutputStream createDataOutputStream() throws IOException {
		return new DataOutputStream(createOutputStream());
	}
	
	public void loadObject(Object target) throws IOException {
		createDataInputStream().readObject(target);
	}
	
	public Object loadMutableObject() throws IOException {
		return createDataInputStream().readMutableObject();
	}
	
	public void storeObject(Object target) throws IOException {
		createDataOutputStream().writeObject(target);
	}
	
	public void storeMutableObject(Object target) throws IOException {
		createDataOutputStream().writeMutableObject(target);
	}

	/**
	 * Return a URL String for this Stream
	 * 
	 * @see Stream.open
	 * @return
	 */
	public String getURL() {
		try {
			return (new URI(getScheme(), null, getPath(), null)).toString();
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public abstract String getScheme();
	public abstract String getPath();
	
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
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append('(');
		builder.append(stream.getURL());
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
	public final String getExtension() {
		String url = getURL();
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
	public String getMimeType() {
		String extension = getExtension();
		if(extension != null)
			switch(extension) {
				case "txt":
					return "text/plain";
					
				case "html":
					return "text/html";

				case "png":
					return "image/png";

				case "jpg":
				case "jpeg":
					return "image/jpeg";

				case "gif":
					return "image/gif";

				case "tiff":
					return "image/tiff";

				case "bmp":
					return "image/bmp";
					
			}
		
		return "application/octet-stream";
	}
	
}
