/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nexustools;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author katelyn
 */
public class Hasher {

	public static String getSHA256(String string) throws IOException {
		try {
			return getHash(new ByteArrayInputStream(string.getBytes(StringUtils.UTF8)), "SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String getSHA1(String string) throws IOException {
		try {
			return getHash(new ByteArrayInputStream(string.getBytes(StringUtils.UTF8)), "SHA1");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String getMD5(String string) throws IOException {
		try {
			return getHash(new ByteArrayInputStream(string.getBytes(StringUtils.UTF8)), "MD5");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String getSHA256(InputStream inStream) throws IOException {
		try {
			return getHash(inStream, "SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String getSHA1(InputStream inStream) throws IOException {
		try {
			return getHash(inStream, "SHA1");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String getMD5(InputStream inStream) throws IOException {
		try {
			return getHash(inStream, "MD5");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("empty-statement")
	public static String getHash(InputStream inStream, String type) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(type);
		DigestInputStream in = new DigestInputStream(inStream, md);
		int r;
		byte[] b = new byte[8129 * 2];
		while ((r = in.read(b)) > -1);

		b = md.digest();

		String ret = "";

		for (byte q : b) {
			ret += Integer.toString((q & 0xff) + 0x100, 16).substring(1);
		}

		return ret;
	}

}
