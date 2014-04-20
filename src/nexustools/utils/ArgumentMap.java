/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author katelyn
 */
public class ArgumentMap extends HashMap<String, ArrayList<String>> {
	
	private String mainKey;
	
	public ArgumentMap(String mainKey, String[] args) {
		this.mainKey = mainKey;
		process(args);
	}
	
	public ArgumentMap(String mainKey, Collection<String> args) {
		this.mainKey = mainKey;
		process(args.toArray(new String[args.size()]));
	}
	
	protected ArrayList<String> init(String key) {
		ArrayList<String> values = get(key);
		if(values == null) {
			values = new ArrayList();
			put(key, values);
		}
		
		return values;
	}
	
	protected void add(String key, String val) {
		init(key).add(val);
	}
	
	public boolean hasArgumentValue(String key) {
		String arg = getArgumentValue(key);
		return arg != null;
	}
	
	public boolean hasArgumentValue(Collection<String> keys) {
		for(String key : keys)
			if(hasArgumentValue(key))
				return true;
		
		return false;
	}
	
	public String getArgumentValue(String key) {
		ArrayList<String> values = get(key);
		if(values == null || values.size() < 1)
			return null;
		
		return values.get(0);
	}
	
	public String getArgumentValue(Collection<String> keys) {
		for(String key : keys) {
			String arg = getArgumentValue(key);
			if(arg != null)
				return arg;
		}
		return null;
	}
	
	protected final void process(String[] args) {
		String key = null;
		for(String arg : args) {
			if(arg.startsWith("-")) {
				if(key != null)
					init(key);
				
				arg = arg.substring(arg.startsWith("--") ? 2 : 1);
				key = arg;
			} else if(key != null) {
				add(key, arg);
				key = null;
			} else
				add(mainKey, arg); // Would in most cases be the main game asset bundle
		}
		if(key != null)
			init(key);
	}
	
}
