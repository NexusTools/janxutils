/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;

/**
 * Provides Streams
 * 
 * @author katelyn
 */
public interface StreamProvider {
	
	/**
	 * The protocol this is meant to open
	 * null indicates that this acts as a fallback
	 * 
	 * @return
	 */
	public String protocol();
	
	/**
	 * Opens a stream
	 * 
	 * @param path The path to try and open, if protocol() returned null this may be a full url
	 * @return
	 * @throws java.io.IOException
	 */
	public Stream open(String path) throws IOException;
	
}
