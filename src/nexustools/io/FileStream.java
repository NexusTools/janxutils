/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nexustools.utils.WeakArrayList;

/**
 * A Stream which allows reading/writing using a {@link RandomAccessFile}.
 * 
 * @author katelyn
 */
public class FileStream extends Stream {
	
	private static final WeakArrayList<FileStream> deleteOnExit = new WeakArrayList();
	private static final WeakHashMap<String, FileStream> instanceCache = new WeakHashMap();
	private final RandomAccessFile randomAccessFile;
	private final String path;
	private boolean writable;
	private long pos;
	
	public FileStream(String path, boolean writable) throws FileNotFoundException, IOException {
		randomAccessFile = new RandomAccessFile(path, writable ? "rw" : "r");
		randomAccessFile.getChannel().lock(0L, Long.MAX_VALUE, !writable);
		this.writable = writable;
		this.path = path;
	}
	
	public FileStream(String path) throws FileNotFoundException, IOException {
		this(path, false);
	}
	
	public String getFilePath() {
		return path;
	}
	
	public static Stream getStream(String filePath) throws IOException {
		return getStream(filePath, false);
	}
	
	public static Stream getStream(String filePath, boolean writeable) throws FileNotFoundException, IOException {
		FileStream fileStream;
		if(writeable)
			return new FileStream(filePath, true);
		else {
			fileStream = instanceCache.get(filePath);
			if(fileStream == null) {
				fileStream = new FileStream(filePath);
				instanceCache.put(filePath, fileStream);
			}
		}
		
		return fileStream.createSubSectorStream();
	}

	@Override
	public void seek(long pos) throws IOException {
		randomAccessFile.seek(pos);
	}

	@Override
	public long pos() {
		return pos;
	}

	@Override
	public long size() throws IOException {
		return randomAccessFile.length();
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException {
		int read = randomAccessFile.read(buffer, off, len);
		if(read > 0)
			pos += read;
		return read;
	}

	@Override
	public void write(byte[] buffer, int off, int len) throws IOException {
		randomAccessFile.write(buffer, off, len);
		pos += len;
	}

	@Override
	public boolean canWrite() {
		return writable;
	}

	@Override
	public void flush() throws IOException {}

	@Override
	public String getURL() {
		return "file:" + path;
	}

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
					System.out.println("Cleaning remaining deleteOnExit FileStreams...");
					for(FileStream fStream : deleteOnExit)
						try {
							fStream.delete();
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
			if(markedForDeletion)
				delete();
		} catch (IOException ex) {
			Logger.getLogger(FileStream.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			super.finalize();
		}
	}
	
	public void close() throws IOException {
		randomAccessFile.close();
	}

	private void delete() throws IOException {
		close();
		(new File(path)).delete();
	}

	@Override
	public String getMimeType() {
		try {
			Method method = File.class.getMethod("probeContentType", String.class);
			String type = (String)method.invoke(null, getFilePath());
			if(type != null)
				return type;
		} catch(Throwable t) {}
		return super.getMimeType(); //To change body of generated methods, choose Tools | Templates.
	}
	
}
