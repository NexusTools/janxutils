/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nexustools.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.net.ssl.HttpsURLConnection;
/**
 *
 * @author Luke
 */
public class FileUtils {

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
	
	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		int r;
		byte[] b = new byte[8129];
		while((r = in.read(b))>-1){
			out.write(b,0,r);
		}
		out.flush();
	}
}
