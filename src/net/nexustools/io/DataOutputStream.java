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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import net.nexustools.data.adaptor.Adaptor;
import net.nexustools.data.adaptor.AdaptorException;
import static net.nexustools.utils.StringUtils.ASCII;
import static net.nexustools.utils.StringUtils.UTF16;
import static net.nexustools.utils.StringUtils.UTF8;

/**
 *
 * @author katelyn
 */
public class DataOutputStream extends java.io.DataOutputStream {

	public DataOutputStream(OutputStream out) {
		super(out);
	}
	public DataOutputStream(Stream stream) throws IOException {
		this(stream.createOutputStream());
	}
	public DataOutputStream(String uri) throws IOException {
		this(Stream.open(uri));
	}

	public void writeObject(Object object, Class<?> type) throws IOException {
		try {
			Adaptor.resolveAdaptor(type).write(object, this);
		} catch (AdaptorException ex) {
			throw new IOException(ex);
		}
	}

	public void writeObject(Object object) throws IOException {
		writeObject(object, object.getClass());
	}

	public void writeMutableObject(Object object) throws IOException {
		try {
			Adaptor.resolveAndWriteMutable(object, this);
		} catch (AdaptorException ex) {
			throw new IOException(ex);
		} catch (UnsupportedOperationException ex) {
			throw new IOException(ex);
		}
	}
	
	public void writeString(String string, Charset charset) throws IOException {
		if(string == null) {
			writeShort(0);
			return;
		}
		
		byte[] stringBytes = string.getBytes(charset);
		writeShort(stringBytes.length);
		write(stringBytes);
	}
	
	public void writeASCII(String string) throws IOException {
		writeString(string, ASCII);
	}
	
	public void writeUTF16(String string) throws IOException {
		writeString(string, UTF16);
	}
	
	public void writeUTF8(String string) throws IOException {
		writeString(string, UTF8);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append('(');
		builder.append(out.toString());
		builder.append(')');

		return builder.toString();
	}
	
}
