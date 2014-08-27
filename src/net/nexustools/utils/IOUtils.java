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

package net.nexustools.utils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import net.nexustools.io.StreamUtils;

/**
 *
 * @author Luke
 */
public class IOUtils {

    public static void copyFile(File from, File to, String basedir) throws FileNotFoundException, IOException {
        if(from.isDirectory()){
            for(File er : from.listFiles()){
                copyFile(er, new File(to.toString() + er.toString().substring(0, basedir.length())), basedir);
            }
        } else if(!to.exists())
            copyStream(new FileInputStream(from), new FileOutputStream(to));
    }
    public static void copyFile(File from, File to) throws FileNotFoundException, IOException {
        if(!to.exists())
            copyStream(new FileInputStream(from), new FileOutputStream(to));
    }
	
	/**
	 * Reads an InputStream and writes it to an OutputStream
	 * 
	 * @param in InputStream to read
	 * @param out OutputStream to write
	 * @throws IOException
	 */
	public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
		try {
			StreamUtils.useCopyBuffer(new Processor<byte[]>() {
				public void process(byte[] b) throws Throwable {
					int r;
					while((r = in.read(b))>-1){
						out.write(b,0,r);
					}
				}
			});
		} catch (InvocationTargetException ex) {
			throw NXUtils.wrapRuntime(ex);
		} finally {
			out.flush();
		}
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
}
