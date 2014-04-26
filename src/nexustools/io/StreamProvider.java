/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.net.URI;

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
	 * @param raw The raw URL String passed to Stream.open
	 * @param supportWriting
	 * @return
	 * @throws java.io.IOException
	 */
	public Stream open(String path, URI raw, boolean supportWriting) throws IOException;
	
}
