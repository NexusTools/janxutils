/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author katelyn
 */
public abstract class Stream {
	
	public static final NullStream Null() {
		return NullStream.getInstance();
	}
	
	private static final HashMap<String, StreamProvider> protocols = new HashMap() {
		{
			put("memory", new StreamProvider() {

				@Override
				public String protocol() {
					return "memory";
				}

				@Override
				public Stream open(String path) {
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
				public Stream open(String path) {
					return Null();
				}
			});
			put("file", new DecodedStreamProvider() {

				@Override
				public String protocol() {
					return "file";
				}

				@Override
				public Stream openImpl(String path, String raw) throws IOException {
					return FileStream.getStream(path);
				}
				
			});
			put("substream", new StreamProvider() {

				final Pattern substreamPattern = Pattern.compile("^(\\d+)\\-(\\d+)@(.+)$");
				
				@Override
				public String protocol() {
					return "substream";
				}

				@Override
				public Stream open(String path) throws IOException {
					Matcher matcher = substreamPattern.matcher(path);
					if(matcher.matches())
						return Stream.open(matcher.group(3)).createSubSectorStream(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)));
					else
						throw new IOException("Malformed substream:" + path);
				}
			});
			put("resource", new DecodedStreamProvider() {

				@Override
				public String protocol() {
					return "resource";
				}

				@Override
				public Stream openImpl(String path, String raw) throws IOException {
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
				public Stream open(String path) throws IOException {
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
				public Stream open(String path) throws IOException {
					return URLStream.getStream(path);
				}
				
			});
		}
	};
	
	static {
		for(StreamProvider provider : ServiceLoader.load(StreamProvider.class))
			registerProvider(provider);
	}
	
	public static void registerProvider(StreamProvider provider) {
		String protocol = provider.protocol();
		if(protocol == null)
			fallbackProviders.add(provider);
		else
			protocols.put(protocol, provider);
	}

	private static final Pattern urlPattern = Pattern.compile("^(\\w+):(.+)$");
	private static final Pattern wrapperPattern = Pattern.compile("^([^\\(]+)\\((.+)\\)$");
	public static Stream open(String url) throws IOException {
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
			{
				StreamProvider provider = protocols.get(matcher.group(1));
				if(provider != null)
					return provider.open(matcher.group(2));
			}
			
			for(StreamProvider provider : fallbackProviders)
				try {
					return provider.open(url);
				} catch(IOException ex) {}
			
			throw new IOException("No handler found for URL: " + url);
		} else
			return FileStream.getStream(url);
	}
	
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

	public static InputStream openInputStream(String url) throws IOException {
		return open(url).createInputStream();
	}

	public static DataInputStream openDataInputStream(String url) throws IOException {
		return open(url).createDataInputStream();
	}

	public static OutputStream openOutputStream(String url) throws IOException {
		return open(url).createOutputStream();
	}

	public static DataOutputStream openDataOutputStream(String url) throws IOException {
		return open(url).createDataOutputStream();
	}
	
	public abstract int read(byte[] buffer, int off, int len) throws IOException;
	public abstract void write(byte[] buffer, int off, int len) throws IOException;
	public abstract boolean canWrite(); // Not all sector streams even support writing
	public abstract void flush() throws IOException;
	
	public abstract long pos();
	public abstract void seek(long pos) throws IOException;
	
	public abstract long size() throws IOException;
	public long remaining() throws IOException {
		return size() - pos();
	}
	public boolean atEnd() throws IOException {
		return remaining() <= 0;
	}

	public SubStream createSubSectorStream(SubStream.Range range) {
		return new SubStream(this, range);
	}
	public final SubStream createSubSectorStream(long start, long end) {
		return createSubSectorStream(SubStream.createRange(start, end));
	}
	public final SubStream createSubSectorStream() {
		return createSubSectorStream(SubStream.getFullRange());
	}

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
	
	public final DataInputStream createDataInputStream() throws IOException {
		return new DataInputStream(createInputStream());
	}
	
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

	public abstract String getURL();
	
	protected static String stringForStream(Stream stream) {
		return stringForStream(stream, stream.getClass().getName());
	}
	
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
	
	public String getExtension() {
		String url = getURL();
		int lastPos = url.lastIndexOf('.');
		if(lastPos > -1)
			return url.substring(lastPos+1);
		return null;
	}
	
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
