/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import nexustools.utils.StringUtils;

/**
 * A Temporary FileStream which deletes itself when 
 * collected by the Garbage Collector or on Shutdown
 * 
 * @author katelyn
 */
public class TemporaryFileStream extends FileStream {
	
	private static String baseTMPFolder = null;
	protected static synchronized String getTemporaryFileName(String prefix) {
		StringBuilder randomFilename = new StringBuilder();
		if(baseTMPFolder == null) {
			randomFilename.append(System.getProperty("java.io.tmpdir"));
			randomFilename.append(File.separator);
			randomFilename.append("janxutils");
			randomFilename.append(File.separator);
			baseTMPFolder = randomFilename.toString();
			File tmpFilePath = new File(baseTMPFolder);
			if(!tmpFilePath.exists() && !tmpFilePath.mkdirs())
				throw new RuntimeException("Cannot create temporary file directory: " + baseTMPFolder);
		} else 
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
		super(getTemporaryFileName(name), true);
		System.out.println("Created Temporary File: " + getPath());
		markDeleteOnExit();
	}

	public TemporaryFileStream() throws FileNotFoundException, IOException {
		this(null);
	}
	
}
