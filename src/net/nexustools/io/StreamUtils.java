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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import net.nexustools.concurrent.PropMap;
import net.nexustools.concurrent.logic.Reader;
import net.nexustools.concurrent.logic.SoftWriteReader;
import net.nexustools.concurrent.logic.WriteReader;
import net.nexustools.concurrent.logic.Writer;
import net.nexustools.data.accessor.MapAccessor;
import net.nexustools.data.buffer.TypeMap;
import net.nexustools.data.buffer.basic.SoftTypeList;
import net.nexustools.data.buffer.basic.StrongTypeList;
import net.nexustools.data.buffer.basic.StrongTypeMap;
import net.nexustools.utils.NXUtils;
import net.nexustools.utils.Processor;
import net.nexustools.utils.StringUtils;
import net.nexustools.utils.log.Logger;

/**
 * A convenience class 
 * 
 * @author katelyn
 */
public class StreamUtils {
	
	public static final short DefaultBufferSize = Short.valueOf(System.getProperty("stream.buffersize", "8192"));
	public static final short DefaultMaxCopySize = Short.valueOf(System.getProperty("stream.copysize", String.valueOf(Short.MAX_VALUE)));
	private static final PropMap<Integer, SoftTypeList<byte[]>> cache = new PropMap();
	
	public static byte[] nextCopyBuffer() {
		return nextBuffer(0);
	}
	public static byte[] nextBuffer(int size) {
		if(size < 1)
			size = DefaultBufferSize;
		
		final Integer desiredSize = size;
		try {
			return cache.read(new WriteReader<byte[], MapAccessor<Integer, SoftTypeList<byte[]>>>() {
				@Override
				public byte[] read(MapAccessor<Integer, SoftTypeList<byte[]>> data) throws Throwable {
					Logger.debug(data);
					byte[] next;
					try {
						next = data.get(desiredSize).shift();
						if(next == null)
							throw new NullPointerException();
						Logger.performance("Fetching cache", desiredSize, next);
					} catch(NullPointerException ex) {
						Logger.performance("Allocating", StringUtils.stringForSize(desiredSize), "byte[]");
						next = new byte[desiredSize];
					}
					Logger.performance("Using", desiredSize, next.length);
					return next;
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	public static void releaseBuffer(final byte[] buffer) {
		if(buffer == null)
			throw new NullPointerException();
		
		final Integer desiredSize = buffer.length;
		try {
			cache.write(new Writer<MapAccessor<Integer, SoftTypeList<byte[]>>>() {
				@Override
				public void write(MapAccessor<Integer, SoftTypeList<byte[]>> data) throws Throwable {
					Logger.performance("Pushing cache", desiredSize, buffer);
					SoftTypeList<byte[]> cache = data.get(desiredSize);
					if(cache == null)
						data.put(desiredSize, cache = new SoftTypeList<byte[]>());
					cache.push(buffer);
					Logger.debug(data);
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}
	public static void useCopyBuffer(Processor<byte[]> processor) throws IOException, InvocationTargetException {
		useBuffer(processor, 0);
	}
	public static void useBuffer(Processor<byte[]> processor, int size) throws IOException, InvocationTargetException {
		final byte[] buffer = nextBuffer(size);
		try {
			processor.process(buffer);
		} catch (IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw NXUtils.wrapInvocation(ex);
		} finally {
			releaseBuffer(buffer);
		}
	}
	
	public static void copy(InputStream inStream, OutputStream outStream) throws IOException {
		copy(inStream, outStream, DefaultBufferSize);
	}
	
	public static void copy(InputStream inStream, OutputStream outStream, int amount) throws IOException {
		copy(inStream, outStream, DefaultMaxCopySize, amount);
	}

	public static void copy(final InputStream inStream, final OutputStream outStream, short bufferSize,final int amount) throws IOException {
		try {
			useBuffer(new Processor<byte[]>() {
				public void process(byte[] buffer) throws Throwable {
					copy(inStream, outStream, buffer, amount);
				}
			}, bufferSize);
		} catch (InvocationTargetException ex) {
			throw NXUtils.unwrapRuntime(ex);
		}
	}

	public static void copy(InputStream inStream, OutputStream outStream, byte[] buffer, int amount) throws IOException {
		int copied;
		while((copied = inStream.read(buffer, 0, (int) Math.min(amount, buffer.length))) > 0) {
			amount -= copied;
			outStream.write(buffer, 0, copied);
			if(amount < 0)
				break;
		}
		if(amount > 0)
			throw new EOFException("Stream ended with " + amount + "bytes left");
	}
	
}
