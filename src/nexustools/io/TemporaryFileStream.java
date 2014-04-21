/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author katelyn
 */
public class TemporaryFileStream extends FileStream {
	
	private static char letters[] = new char[]{
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
		'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
		'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
		'3', '4', '5', '6', '7', '8', '9', '-', '_'
	};
	protected static String getTemporaryFileName(String prefix) {
		StringBuilder randomFilename = new StringBuilder();
		randomFilename.append(System.getProperty("java.io.tmpdir"));
		randomFilename.append(File.separator);
		if(prefix != null) {
			randomFilename.append(prefix);
			randomFilename.append('-');
		}
		for(int i=0; i<16; i++)
			randomFilename.append(letters[(int)Math.floor(Math.random()*letters.length)]);
		
		randomFilename.append('.');
		randomFilename.append(System.currentTimeMillis());
		return randomFilename.toString();
	}

	public TemporaryFileStream(String name) throws FileNotFoundException, IOException {
		super(getTemporaryFileName(name), true);
		System.out.println("Created Temporary File: " + getFilePath());
		markDeleteOnExit();
	}

	public TemporaryFileStream() throws FileNotFoundException, IOException {
		this(null);
	}
	
}
