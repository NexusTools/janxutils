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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public class FileOutputStream extends EfficientOutputStream {
	
	long pos = 0;
	long markPos = -1;
	RandomAccessFile file;
	public FileOutputStream(File file) throws IOException {
		this(file.getAbsolutePath());
	}
	public FileOutputStream(String file) throws IOException {
		this.file = RandomFileFactory.open(file, true);
	}

	@Override
	public void close() throws IOException {
		if(file == null)
			return;
		
		RandomFileFactory.release(file);
		file = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(file != null) {
			Logger.warn(file.toString() + " was not closed");
			close();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		file.seek(pos);
		file.write(b, off, len);
		pos = file.getFilePointer();
	}
	
}
