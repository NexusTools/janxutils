/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.data;

/**
 *
 * @author katelyn
 */
public class AdaptorException extends Exception {

	public AdaptorException(String string, java.lang.Exception ex) {
		super(string, ex);
	}

	AdaptorException(String string) {
		super(string);
	}
	
}
