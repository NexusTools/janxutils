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
import java.io.FileNotFoundException;
import java.io.IOException;
import net.nexustools.utils.StringUtils;
import net.nexustools.utils.log.Logger;

/**
 * A Temporary FileStream which deletes itself when 
 * collected by the Garbage Collector or on Shutdown
 * 
 * @author katelyn
 */
public class TemporaryFileStream extends FileStream {
	
	private static String baseTMPFolder = null;
	static {
		StringBuilder randomFilename = new StringBuilder();
		randomFilename.append(System.getProperty("java.io.tmpdir"));
		randomFilename.append(File.separator);
		randomFilename.append("janxutils");
		randomFilename.append(File.separator);
		baseTMPFolder = randomFilename.toString();
		File tmpFilePath = new File(baseTMPFolder);
		if(!tmpFilePath.exists() && !tmpFilePath.mkdirs())
			throw new RuntimeException("Cannot create temporary file directory: " + baseTMPFolder);
	}
	
	protected static synchronized String getTemporaryFileName(String prefix) {
		StringBuilder randomFilename = new StringBuilder();
		randomFilename.append(baseTMPFolder);
		if(prefix != null) {
			randomFilename.append(prefix);
			randomFilename.append('-');
		}
		randomFilename.append(StringUtils.randomString(16));
		randomFilename.append('.');
		randomFilename.append(System.currentTimeMillis());
		return randomFilename.toString();
	}

	public TemporaryFileStream(String name) throws FileNotFoundException, IOException {
		super(getTemporaryFileName(name));
		Logger.debug("Created Temporary File: " + path());
		markDeleteOnExit();
	}

	public TemporaryFileStream() throws FileNotFoundException, IOException {
		this(null);
	}
	
}
