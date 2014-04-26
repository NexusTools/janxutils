/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import nexustools.io.MemoryStream;
import nexustools.io.Stream;
import static nexustools.io.StreamUtils.DefaultMemoryMax;

/**
 *
 * @author katelyn
 */
public class StringUtils {
	
	/* Since Java 1.7, there is also java.nio.charset.StandardCharsets.UTF_8 etc.,
	* you might want to change this accordingly (or use those directly from the Methods)
	* instead of getting the charsets via these String Methods.
	*/
	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Charset UTF16 = Charset.forName("UTF-16");
	public static final Charset ASCII = Charset.forName("US-ASCII");
	
	public static String read(String url, Charset charset, short max) throws IOException {
		return read(Stream.openInputStream(url), charset, max);
	}
	
	public static String read(String url, Charset charset) throws IOException {
		return read(url, charset, DefaultMemoryMax);
	}
	
	public static String readUTF8(String url) throws IOException {
		return read(url, StringUtils.UTF8);
	}
	
	public static String readUTF8(String url, short max) throws IOException {
		return read(url, StringUtils.UTF8, max);
	}
	
	public static String readUTF16(String url) throws IOException {
		return read(url, StringUtils.UTF16);
	}
	
	public static String readUTF16(String url, short max) throws IOException {
		return read(url, StringUtils.UTF16, max);
	}
	
	public static String readASCII(String url) throws IOException {
		return read(url, StringUtils.ASCII);
	}
	
	public static String readASCII(String url, short max) throws IOException {
		return read(url, StringUtils.ASCII, max);
	}
	
	public static String read(InputStream inStream, Charset charset) throws IOException {
		return read(inStream, charset, DefaultMemoryMax);
	}
	
	public static String readUTF8(InputStream inStream) throws IOException {
		return read(inStream, StringUtils.UTF8);
	}
	
	public static String readUTF8(InputStream inStream, short max) throws IOException {
		return read(inStream, StringUtils.UTF8, max);
	}
	
	public static String readUTF16(InputStream inStream) throws IOException {
		return read(inStream, StringUtils.UTF16);
	}
	
	public static String readUTF16(InputStream inStream, short max) throws IOException {
		return read(inStream, StringUtils.UTF16, max);
	}
	
	public static String readASCII(InputStream inStream) throws IOException {
		return read(inStream, StringUtils.ASCII);
	}
	
	public static String readASCII(InputStream inStream, short max) throws IOException {
		return read(inStream, StringUtils.ASCII, max);
	}
	
	public static String read(InputStream inStream, Charset charset, short max) throws IOException {
		return new String((new MemoryStream(inStream, max)).toByteArray(), charset);
	}
	
	private final static char letters[] = new char[]{
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
		'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
		'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
		'3', '4', '5', '6', '7', '8', '9', '-', '_'
	};
	public static String randomString(int len) {
		char[] string = new char[len];
		for(int i=0; i<len; i++)
		/* You might want to consider using java.security.SecureRandom for this: final SecureRandom rand = new SecureRandom(); ... rand.nextDouble()
		* Or, instead of Math.random() (which is thread-safe and hence slower for high throughput), "new java.util.Random()"
		*/
			string[i] = letters[(int)(Math.random()*letters.length)]; // Math.floor not required
		return new String(string);
	}
	
}
