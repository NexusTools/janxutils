/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author katelyn
 */
public abstract class DecodedStreamProvider implements StreamProvider {

	@Override
	public final Stream open(String path) throws IOException {
		try {
			URI uri = new URI(null, null, path, null);
			path = uri.getPath();
		} catch (URISyntaxException ex) {
			throw new IOException(ex);
		}
		return openImpl(path);
	}
	
	public abstract Stream openImpl(String path) throws IOException;
	
}
