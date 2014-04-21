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
	public final Stream open(String raw) throws IOException {
		String path;
		try {
			URI uri = new URI(null, null, raw, null);
			path = uri.getPath();
		} catch (URISyntaxException ex) {
			throw new IOException(ex);
		}
		return openImpl(path, raw);
	}
	
	public abstract Stream openImpl(String path, String raw) throws IOException;
	
}
