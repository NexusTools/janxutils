/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nexustools.io;

import java.net.SocketAddress;

/**
 *
 * @author katelyn
 */
public class LocalSocketAddress extends SocketAddress {
	
	public final String path;
	public LocalSocketAddress(String path) {
		this.path = path;
	}
	
}
