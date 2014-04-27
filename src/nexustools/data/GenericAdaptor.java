/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.data;

import java.io.IOException;
import java.util.Map;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;

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
	
	public GenericAdaptor(Class<? extends T> target) {
		this.definition = ClassDefinition.getInstance(target);
	}

	@Override
	public Class<? extends T> getType() {
		return (Class<? extends T>) definition.getType();
	}

	@Override
	public void write(T target, DataOutputStream out) throws IOException {
		ClassDefinition targetDefinition = ClassDefinition.getInstance(target.getClass());
		for(FieldDefinition.Adaptor fAdaptor : targetDefinition.staticFields) {
			fAdaptor.write(target, out);
		}
		out.writeByte(targetDefinition.fieldMap.size());
		for(Map.Entry<Byte, FieldDefinition.Adaptor> entry : targetDefinition.fieldMap.entrySet()) {
			out.writeByte(entry.getKey());
			entry.getValue().write(target, out);
		}
		out.writeByte(targetDefinition.mutableFields.size());
		for(Map.Entry<String, FieldDefinition.Adaptor> entry : targetDefinition.mutableFields.entrySet()) {
			out.writeUTF8(entry.getKey());
			entry.getValue().write(target, out);
		}
	}

	@Override
	public void read(T target, DataInputStream in) throws IOException {
		ClassDefinition targetDefinition = ClassDefinition.getInstance(target.getClass());
		for(FieldDefinition.Adaptor fAdaptor : targetDefinition.staticFields) {
			fAdaptor.read(target, in);
		}
		
		int fieldsLeft = (int) (in.readByte() & 0xff);
		while(fieldsLeft-- > 0)
			targetDefinition.fieldMap.get(in.readByte()).read(target, in);
		
		fieldsLeft = (int) (in.readByte() & 0xff);
		while(fieldsLeft-- > 0)
			targetDefinition.mutableFields.get(in.readUTF8()).read(target, in);
	}

	/**
	 * Checks whether or not the underlying type
	 * actually has any field instructions
	 * 
	 * @throws nexustools.data.AdaptorException
	 */
	public void validate() throws AdaptorException {
		if(definition.staticFields.isEmpty() &&
				definition.fieldMap.isEmpty() &&
				definition.mutableFields.isEmpty())
			throw new AdaptorException(getType().getName() + " has no usable fields");
	}

	
}
