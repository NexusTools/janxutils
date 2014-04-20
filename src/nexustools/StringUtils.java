/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools;

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
	
}
