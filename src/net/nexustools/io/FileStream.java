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
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import net.nexustools.utils.WeakArrayList;

/**
 * A Stream which allows reading/writing using a {@link RandomAccessFile}.
 * 
 * @author katelyn
 */
public class FileStream extends Stream {
	
	private static final WeakArrayList<FileStream> deleteOnExit = new WeakArrayList();
	private static final WeakHashMap<String, FileStream> instanceCache = new WeakHashMap();
	private RandomAccessFile randomAccessFile;
	private final boolean writable;
	private final String path;
	
	public FileStream(String path, boolean writable) throws FileNotFoundException, IOException {
		this.writable = writable;
		this.path = path;
		//ensureOpen();
	}
	
	public FileStream(String path) throws FileNotFoundException, IOException {
		this(path, false);
	}
	
	protected final void ensureOpen() throws FileNotFoundException, IOException {
		if(randomAccessFile == null) {
			if(writable) {
				String parentPath = path.substring(0, path.lastIndexOf("/"));
				System.out.println(parentPath);
				File parentFile = new File(parentPath);
				if(!parentFile.exists() && !parentFile.mkdirs())
					throw new IOException(getURL() + ": Unable to create directory structure");
			}
			randomAccessFile = new RandomAccessFile(path, writable ? "rw" : "r");
			randomAccessFile.getChannel().lock(0L, Long.MAX_VALUE, !writable);
		}
	}
	
	@Override
	public String getScheme() {
		return "file";
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	public static Stream getStream(String filePath) throws IOException {
		return getStream(filePath, false);
	}
	
	public static synchronized Stream getStream(String filePath, boolean writeable) throws FileNotFoundException, IOException {
		FileStream fileStream;
		if(writeable)
			return new FileStream(filePath, true);
		else {
			fileStream = instanceCache.get(filePath);
			if(fileStream == null) {
				System.out.println("Creating FileStream: " + filePath);
				fileStream = new FileStream(filePath);
				instanceCache.put(filePath, fileStream);
			}
		}
		
		return fileStream.createSubSectorStream();
	}

	@Override
	public void seek(long pos) throws IOException {
		ensureOpen();
		randomAccessFile.seek(pos);
	}

	@Override
	public long pos() throws IOException {
		ensureOpen();
		return randomAccessFile.getFilePointer();
	}

	@Override
	public long size() throws IOException {
		ensureOpen();
		return randomAccessFile.length();
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException {
		ensureOpen();
		return randomAccessFile.read(buffer, off, len);
	}

	@Override
	public void write(byte[] buffer, int off, int len) throws IOException {
		ensureOpen();
		randomAccessFile.write(buffer, off, len);
	}

	@Override
	public boolean canWrite() {
		return writable;
	}

	@Override
	public void flush() throws IOException {}

	private boolean markedForDeletion = false;
	private static Thread deleteOnExitThread;
	public void markDeleteOnExit() {
		if(!writable)
			throw new RuntimeException("It makes no sense to mark a read-only file as deleteOnExit...");
		
		if(markedForDeletion)
			return;
		
		markedForDeletion = true;
		deleteOnExit.add(this);
		if(deleteOnExitThread == null) {
			deleteOnExitThread = new Thread(new Runnable() {

				@Override
				public void run() {
					if(deleteOnExit.isEmpty())
						return;
					
					System.out.println("Cleaning remaining deleteOnExit FileStreams...");
					for(FileStream fStream : deleteOnExit)
						try {
							fStream.deleteAsMarked();
						} catch (Throwable ex) {
							ex.printStackTrace(System.err);
						}
				}
				
			}, "deleteOnExitHandler");
			Runtime.getRuntime().addShutdownHook(deleteOnExitThread);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			if(markedForDeletion) {
				deleteAsMarked();
				deleteOnExit.remove(this);
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		} finally {
			super.finalize();
		}
	}
	
	public final boolean isOpen() {
		return randomAccessFile != null;
	}
	
	public final void close() throws IOException {
		if(randomAccessFile != null) {
			randomAccessFile.close();
			randomAccessFile = null;
		}
	}

	private void deleteAsMarked() throws IOException {
		close();
		(new File(path)).delete();
	}

	@Override
	public String getMimeType() {
		try {
			Method method = File.class.getMethod("probeContentType", String.class);
			String type = (String)method.invoke(null, getPath());
			if(type != null)
				return type;
		} catch(Throwable t) {}
		return super.getMimeType(); //To change body of generated methods, choose Tools | Templates.
	}
	
}
