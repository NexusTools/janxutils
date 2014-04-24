/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nexustools.utils;

import java.io.*;

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
	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		int r;
		byte[] b = new byte[8129];
		while((r = in.read(b))>-1){
			out.write(b,0,r);
		}
		out.flush();
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
}
