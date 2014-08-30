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

package net.nexustools.data.impl;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.nexustools.data.adaptor.Adaptor;
import net.nexustools.data.adaptor.AdaptorException;
import net.nexustools.data.analyze.ClassDefinition;
import net.nexustools.data.analyze.FieldDefinition;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;

/**
 * This Adaptor uses reflection to determine the
 * way to write/read an object.
 * 
 * It is used when no other adaptors can be found.
 * 
 * It also reads the FieldStream and ClassStream annotations
 * when available to help it process the data correctly.
 * 
 * @author katelyn
 * 
 * @param <T>
 */
public class GenericAdaptor<T> extends Adaptor<T> {

	private final ClassDefinition definition;
	
	public GenericAdaptor(Class<? extends T> target) throws AdaptorException {
		this.definition = ClassDefinition.load(target);
	}

	@Override
	public Class<? extends T> getType() {
		return (Class<? extends T>) definition.getType();
	}

	@Override
	public void write(T target, DataOutputStream out) throws IOException {
		ClassDefinition targetDefinition = ClassDefinition.load(target.getClass());
		for(FieldDefinition.Adaptor fAdaptor : targetDefinition.getStaticFields()) {
			fAdaptor.write(target, out);
		}
		out.writeByte(targetDefinition.getFieldMap().size());
		for(Map.Entry<Byte, FieldDefinition.Adaptor> entry : targetDefinition.getFieldMap().entrySet()) {
			out.writeByte(entry.getKey());
			entry.getValue().write(target, out);
		}
		out.writeByte(targetDefinition.getMutableFields().size());
		for(Map.Entry<String, FieldDefinition.Adaptor> entry : targetDefinition.getMutableFields().entrySet()) {
			out.writeUTF8(entry.getKey());
			entry.getValue().write(target, out);
		}
	}

	@Override
	public void read(T target, DataInputStream in) throws IOException {
		ClassDefinition targetDefinition = ClassDefinition.load(target.getClass());
		for(FieldDefinition.Adaptor fAdaptor : targetDefinition.getStaticFields()) {
			fAdaptor.read(target, in);
		}
		
		int fieldsLeft = (int) (in.readByte() & 0xff);
		while(fieldsLeft-- > 0)
			targetDefinition.getFieldMap().get(in.readByte()).read(target, in);
		
		fieldsLeft = (int) (in.readByte() & 0xff);
		while(fieldsLeft-- > 0)
			targetDefinition.getMutableFields().get(in.readUTF8()).read(target, in);
	}

	/**
	 * Checks whether or not the underlying type
	 * actually has any field instructions
	 * 
	 * @throws nexustools.data.AdaptorException
	 */
	public void validate() throws AdaptorException {
		if(definition.getStaticFields().isEmpty() &&
				definition.getFieldMap().isEmpty() &&
				definition.getMutableFields().isEmpty())
			throw new AdaptorException(getType().getName() + " has no usable fields");
	}

	
}
