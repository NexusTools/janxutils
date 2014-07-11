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

package nexustools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import nexustools.io.Stream;
import nexustools.utils.args.ArgumentConverter;
import nexustools.utils.args.ArgumentParserException;
import nexustools.utils.args.CommonConversions;
import nexustools.utils.args.NoSuchArgumentException;

/**
 *
 * @author katelyn
 */
public class ArgumentParser {
	
	private final String appName;
	private final Version version;
	private final String description;
	private final HashMap<String,String[]> parsedArguments = new HashMap();
	
	public ArgumentParser(String appName, Collection<String> args) {
		this(appName,(String)null, args);
	}
	
	public ArgumentParser(String appName, Version version, Collection<String> args) {
		this(appName, (String)null, version, args);
	}
	
	public ArgumentParser(String appName, String desc, Version version, InputStream args) throws IOException {
		this(appName, desc, version, parse(args));
	}
	
	public ArgumentParser(String appName, String desc, InputStream args) throws IOException {
		this(appName, desc, null, args);
	}
	
	public ArgumentParser(String appName, String desc, Stream args) throws IOException {
		this(appName, desc, args.createInputStream());
	}
	
	public ArgumentParser(String appName, String[] args) {
		this(appName, (String)null, args);
	}
	
	public ArgumentParser(String appName, String desc, String[] args) {
		this(appName, desc, null, args);
	}
	
	public ArgumentParser(String appName, String desc, Collection<String> args) {
		this(appName, desc, null, args);
	}
	
	public ArgumentParser(String appName, String desc, Version version, Collection<String> args) {
		this(appName, desc, version, args.toArray(new String[args.size()]));
	}
	
	public ArgumentParser(String appName, String desc, Version version, String[] args) {
		this.appName = appName;
		this.description = desc;
		this.version = version;
		process(args);
	}
	
	protected final static Collection<String> parse(InputStream iStream) {
		ArrayList<String> args = new ArrayList();
		// TODO: Implement
		return args;
	}

	protected final void process(String[] args) {
		
	}
	
	public <T> T take(String m, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		return take(new String[]{m}, null, testAgainst);
	}
	
	public boolean take(String m) throws ArgumentParserException {
		try {
			take(new String[]{m}, null, CommonConversions.MustBeEmpty);
		} catch(NoSuchArgumentException noSuchArgument) {
			return false;
		}
		return true;
	}
	
	public <T> T take(String m, String description, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		return take(new String[]{m}, description, testAgainst);
	}
	
	public void take(String m, String description) throws ArgumentParserException {
		take(new String[]{m}, description, CommonConversions.MustBeEmpty);
	}
	
	public <T> T take(String[] m, String description, ArgumentConverter<T> testAgainst) throws NoSuchArgumentException, ArgumentParserException {
		String[] args = null;
		for(String o : m) {
			args = parsedArguments.remove(o);
			if(args != null)
				break;
		}
		if(args == null)
			throw new NoSuchArgumentException(m[0] + " is required");
		
		try {
			testAgainst.test(args);
		} catch (ArgumentParserException argumentParserException) {
			String longestArgumentName = null;
			for(String o : m) {
				if(longestArgumentName == null || o.length() > longestArgumentName.length())
					longestArgumentName = o;
			}
			throw new ArgumentParserException(((longestArgumentName.length() > 1) ? "--" : "-") + longestArgumentName + ": " + argumentParserException.getMessage());
		}
		
		return null;
	}
	
	public void take(String[] m, String description) throws ArgumentParserException {
		take(m, description, CommonConversions.MustBeEmpty);
	}
	
	public <T> T take(Collection<String> m, String description, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		return take(m.toArray(new String[m.size()]), description, testAgainst);
	}
	
	public void take(Collection<String> m, String description) throws ArgumentParserException {
		take(m.toArray(new String[m.size()]), description, CommonConversions.MustBeEmpty);
	}
	
	public <T> T take(String m, T def, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		return take(m, def, null, testAgainst);
	}
	
	public <T> T take(String m, T def, String description, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		return take(new String[]{m}, def, description, testAgainst);
	}
	
	public <T> T take(String[] m, T def, String description, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		try {
			return take(m, description, testAgainst);
		} catch(NoSuchArgumentException noSuchArgument) {
			return def;
		}
	}
	public <T> T take(Collection<String> m, T def, String description, ArgumentConverter<T> testAgainst) throws ArgumentParserException {
		return take(m.toArray(new String[m.size()]), def, description, testAgainst);
	}
	
	public final boolean validate() {
		return validate(System.err);
	}
	
	public final boolean validate(PrintStream errStream) {
		if(parsedArguments.size() > 0) {
			// TODO: Dump unknown argument error
			showHelp(errStream);
			return false;
		}
		return true;
	}
	
	public final void showHelp(PrintStream stream) {
		
	}
	
	public final void showHelp() {
		showHelp(System.err);
	}
	
	public final void showError(PrintStream errStream, Exception argumentParserException) {
		errStream.print("Error: ");
		errStream.println(argumentParserException.getMessage());
		errStream.println();
		showHelp(errStream);
	}
	
	public final void showError(Exception argumentParserException) {
		showError(System.err, argumentParserException);
	}
	
}
