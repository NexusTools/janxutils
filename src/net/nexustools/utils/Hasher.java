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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.nexustools.io.StreamUtils;

/**
 *
 * @author katelyn
 */
public class Hasher {

	public static String getSHA256(String string) throws IOException {
		return getSHA256(new ByteArrayInputStream(string.getBytes(StringUtils.UTF8)));
	}

	public static String getSHA1(String string) throws IOException {
		return getSHA1(new ByteArrayInputStream(string.getBytes(StringUtils.UTF8)));
	}

	public static String getMD5(String string) throws IOException {
		return getMD5(new ByteArrayInputStream(string.getBytes(StringUtils.UTF8)));
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

	/**
	 * Reads an InputStream into a MessageDigest using the specified
	 * algorithm, and outputs a hexadecimal representation.
	 * 
	 * @param inStream InputStream to read
	 * @param algorithm Algorithm to use
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHash(InputStream inStream, String algorithm) throws IOException, NoSuchAlgorithmException {
		return IOUtils.bytesToHex(getRawHash(inStream, algorithm));
	}
	
	/**
	 * Reads an InputStream into a MessageDigest using the specified
	 * algorithm, and outputs the raw hash byte array.
	 * 
	 * @param inStream InputStream to read
	 * @param algorithm Algorithm to use
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("empty-statement")
	public static byte[] getRawHash(InputStream inStream, String algorithm) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		DigestInputStream in = new DigestInputStream(inStream, md);
		
		byte[] b = new byte[StreamUtils.DefaultBufferSize];
		while (in.read(b) > -1);
		return md.digest();
	}

}
