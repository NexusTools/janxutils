/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nexustools.io.data;

import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author katelyn
 */
public class MapAdaptor extends Adaptor<Map> {

	@Override
	public void write(Map target, DataOutputStream out) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void read(Map target, DataInputStream in) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Class<? extends Map> getType() {
		return Map.class;
	}

}
