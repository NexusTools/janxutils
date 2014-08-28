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
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author katelyn
 */
public final class VoidStream extends Stream {
	
	private static final VoidStream instance = new VoidStream();
	
	public static final VoidStream instance() {
		return instance;
	}
	
	protected VoidStream() {}

	@Override
	public String scheme() {
		return "void";
	}

	@Override
	public String path() {
		return "";
	}

	@Override
	public final boolean canWrite() {
		return true;
	}

	@Override
	public boolean canRead() {
		return false;
	}

	@Override
	public final long size() {
		return 0;
	}
	
	@Override
	public String toURL() {
		return "void:";
	}

	@Override
	public InputStream createInputStream(long pos) throws IOException {
		throw new IOException("VoidStream cannot be read from.");
	}

	@Override
	public OutputStream createOutputStream(long pos) throws IOException {
		return EfficientOutputStream.Void;
	}
	
}
