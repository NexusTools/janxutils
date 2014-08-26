/*
 * janxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * janxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with janxutils.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.nexustools.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author katelyn
 */
public class ArgumentMap extends HashMap<String, ArrayList<String>> {
	
	private final String mainKey;
	
	public ArgumentMap() {
		mainKey = "";
	}
	
	public ArgumentMap(String[] args) {
		this("", args);
	}
	
	public ArgumentMap(Collection<String> args) {
		this("", args);
	}
	
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
		return getArgumentValue(key, null);
	}
	
	public String getArgumentValue(String key, String def) {
		ArrayList<String> values = get(key);
		if(values == null || values.size() < 1)
			return def;
		
		return values.get(0);
	}
	
	public String getArgumentValue(Collection<String> keys) {
		return getArgumentValue(keys, null);
	}
	
	public String getArgumentValue(Collection<String> keys, String def) {
		for(String key : keys) {
			String arg = getArgumentValue(key);
			if(arg != null)
				return arg;
		}
		return def;
	}
	
	public void putArgumentValue(String key, String value) {
		ArrayList<String> values = new ArrayList();
		values.add(value);
		put(key, values);
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

	public void readUrl(String arguments) throws UnsupportedEncodingException {
		if(arguments.length() < 1)
			return;
		
		int nextPos;
		int lastPos = 0;
		do {
			String chunk;
			nextPos = arguments.indexOf("&", lastPos);
			if(nextPos > -1)
				chunk = arguments.substring(lastPos, (lastPos = nextPos+1)-1);
			else
				chunk = arguments.substring(lastPos);

			String key, val;
			int equalPos = chunk.indexOf("=");
			if(equalPos > -1) {
				key = chunk.substring(0, equalPos);
				val = chunk.substring(equalPos+1);
			} else {
				key = chunk.substring(0);
				val = null;
			}
			key = URLDecoder.decode(key, "UTF-8");
			if(val != null)
				add(key, URLDecoder.decode(val, "UTF-8"));
			else
				init(key);
		} while(nextPos > 0);
	}
	
}
