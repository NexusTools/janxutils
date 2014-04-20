/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author katelyn
 */
public class Hasher {
	
	public static String getSHA256(InputStream inStream) {
		return getHash(inStream, "SHA256");
	}
	
	public static String getSHA1(InputStream inStream) {
		return getHash(inStream, "SHA1");
	}
	
	public static String getMD5(InputStream inStream) {
		return getHash(inStream, "MD5");
	}

    public static String getHash(InputStream inStream, String type) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(type);
            DigestInputStream in = new DigestInputStream(inStream, md);
            int r;
            byte[] b = new byte[8129*2];
            while((r=in.read(b))>-1);
            
            b = md.digest();
            
            String ret = "";
            
            for(byte q : b)
                ret += Integer.toString((q & 0xff) + 0x100, 16).substring(1);
            
            return ret;
        } catch (IOException | NoSuchAlgorithmException ex) {}
        
        return null;
    }
	
}
