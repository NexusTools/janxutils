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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import net.nexustools.io.MemoryStream;
import net.nexustools.io.Stream;
import static net.nexustools.io.StreamUtils.DefaultBufferSize;

/**
 *
 * @author katelyn
 */
public class StringUtils {
	
	public static final Charset UTF8;
	public static final Charset UTF16;
	public static final Charset ASCII;
	
	static {
		Charset utf8;
		Charset utf16;
		Charset ascii;
		try {
			utf8 = java.nio.charset.StandardCharsets.UTF_8;
			utf16 = java.nio.charset.StandardCharsets.UTF_16;
			ascii = java.nio.charset.StandardCharsets.US_ASCII;
		} catch (Throwable t) {
			utf8 = Charset.forName("UTF-8");
			utf16 = Charset.forName("UTF-16");
			ascii = Charset.forName("US-ASCII");
		}
		UTF8 = utf8;
		UTF16 = utf16;
		ASCII = ascii;
	}
	
	public static String read(String url, Charset charset, short max) throws IOException, URISyntaxException {
		return read(Stream.openInputStream(url), charset, max);
	}
	
	public static String read(String url, Charset charset) throws IOException, URISyntaxException {
		return read(url, charset, DefaultBufferSize);
	}
	
	public static String readUTF8(String url) throws IOException, URISyntaxException {
		return read(url, StringUtils.UTF8);
	}
	
	public static String readUTF8(String url, short max) throws IOException, URISyntaxException {
		return read(url, StringUtils.UTF8, max);
	}
	
	public static String readUTF16(String url) throws IOException, URISyntaxException {
		return read(url, StringUtils.UTF16);
	}
	
	public static String readUTF16(String url, short max) throws IOException, URISyntaxException {
		return read(url, StringUtils.UTF16, max);
	}
	
	public static String readASCII(String url) throws IOException, URISyntaxException {
		return read(url, StringUtils.ASCII);
	}
	
	public static String readASCII(String url, short max) throws IOException, URISyntaxException {
		return read(url, StringUtils.ASCII, max);
	}
	
	public static String read(InputStream inStream, Charset charset) throws IOException {
		return read(inStream, charset, DefaultBufferSize);
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
		SecureRandom secureRandom = new SecureRandom();
		for(int i=0; i<len; i++)
			string[i] = letters[(int)(secureRandom.nextDouble()*letters.length)];
		return new String(string);
	}

	public static String stringForException(Throwable ex) {
		StringWriter errors = new StringWriter();
		PrintWriter errorWriter = new PrintWriter(errors);
		ex.printStackTrace(errorWriter);
		errorWriter.flush();

		return errors.toString();
	}
	
	public static String stringForSize(long bytes) {
		return stringForSize((double)bytes);
	}
	
	public static String stringForSize(double bytes) {
		String suffix = "b";
		if(bytes >= 1024) {
			bytes /= 1024;
			suffix = "kb";
		}
		if(bytes >= 1024) {
			bytes /= 1024;
			suffix = "mb";
		}
		if(bytes >= 1024) {
			bytes /= 1024;
			suffix = "gb";
		}
		if(bytes >= 1024) {
			bytes /= 1024;
			suffix = "tb";
		}
		if(bytes >= 1024) {
			bytes /= 1024;
			suffix = "pb";
		}
		
		return Math.ceil(bytes*10)/10 + suffix;
	}
	
}
