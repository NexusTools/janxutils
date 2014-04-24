/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.data;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nexustools.io.DataInputStream;

/**
 *
 * @author katelyn
 */
public final class ClassDefinition {
	
	private static WeakHashMap<Class<?>, ClassDefinition> instances = new WeakHashMap();
	public static ClassDefinition getInstance(Class<?> type) {
		ClassDefinition def = instances.get(type);
		if(def == null) {
			def = new ClassDefinition(type);
			instances.put(type, def);
		}
		return def;
	}
	
	private static class Instruction {
		boolean publicFields = true;
		boolean protectedFields = false;
		boolean privateFields = false;
		boolean transientFields = false;
		boolean volatileFields = false;
		boolean staticFieldLayout = false;
		boolean autogenFieldIDs = false;
	}
	
	private final Class<?> type;
	private final ClassDefinition superDefinition;
	private final HashMap<String, FieldDefinition> fields = new HashMap();
	
	protected final ArrayList<FieldDefinition.Adaptor> staticFields = new ArrayList();
	protected final HashMap<String, FieldDefinition.Adaptor> mutableFields = new HashMap();
	protected final HashMap<Byte, FieldDefinition.Adaptor> fieldMap = new HashMap();
	private ClassDefinition(Class<?> type) {
		Class<?> superClazz = type.getSuperclass();
		if(superClazz != null && !superClazz.equals(Object.class))
			superDefinition = getInstance(superClazz);
		else
			superDefinition = null;
		this.type = type;
		process();
	}
	
	private void process() {
		if(superDefinition != null) {
			staticFields.addAll(superDefinition.staticFields);
			for(Entry<String, FieldDefinition.Adaptor> entry : superDefinition.mutableFields.entrySet())
				mutableFields.put(entry.getKey(), entry.getValue());
			for(Entry<Byte, FieldDefinition.Adaptor> entry : superDefinition.fieldMap.entrySet())
				fieldMap.put(entry.getKey(), entry.getValue());
		}

		ArrayList<Field> unknownFields = new ArrayList();
		for(Field field : type.getDeclaredFields()) {
			FieldStream fieldStream = field.getAnnotation(FieldStream.class);

			if(fieldStream != null) {
				FieldDefinition.Instruction fieldInstruction = new FieldDefinition.Instruction();

				fieldInstruction.staticField = fieldStream.staticField();
				fieldInstruction.mutableType = fieldStream.mutableType();
				try {
					fieldInstruction.mutableType = fieldInstruction.mutableType && Adaptor.isMutable(field.getType());
				} catch (AdaptorException ex) {}
				fieldInstruction.neverNull = fieldStream.neverNull();
				fieldInstruction.fieldID = fieldStream.fieldID();

				if(!registerField(field, fieldInstruction))
					throw new RuntimeException("Failed to register field with FieldStream annotation");
			} else {
				if(Modifier.isFinal(field.getModifiers()) ||
						Modifier.isStatic(field.getModifiers()))
					continue;

				unknownFields.add(field);
			}
		}

		if(unknownFields.size() > 0) {
			Instruction classInstruction = new Instruction();
			{
				ClassStream classStream = type.getAnnotation(ClassStream.class);
				if(classStream != null) {
					classInstruction.publicFields = classStream.publicFields();
					classInstruction.protectedFields = classStream.protectedFields();
					classInstruction.privateFields = classStream.privateFields();
					classInstruction.transientFields = classStream.transientFields();
					classInstruction.volatileFields = classStream.volatileFields();
					classInstruction.staticFieldLayout = classStream.staticFieldLayout();
					classInstruction.autogenFieldIDs = classStream.autogenFieldIDs();
				}
			}

			int fieldID = 1;
			for(Field field : unknownFields) {
				if(Modifier.isPublic(field.getModifiers()) &&
						!classInstruction.publicFields)
					continue;

				if(Modifier.isProtected(field.getModifiers()) &&
						!classInstruction.protectedFields)
					continue;

				if(Modifier.isPrivate(field.getModifiers()) &&
						!classInstruction.privateFields)
					continue;

				if(Modifier.isVolatile(field.getModifiers()) &&
						!classInstruction.volatileFields)
					continue;

				if(Modifier.isTransient(field.getModifiers()) &&
						!classInstruction.transientFields)
					continue;

				FieldDefinition.Instruction fieldInstruction = new FieldDefinition.Instruction();
				if(classInstruction.staticFieldLayout)
					fieldInstruction.staticField = true;
				else if(classInstruction.autogenFieldIDs) {
					while(fieldMap.containsKey((byte)fieldID))
						fieldID ++;

					if(fieldID > 255)
						throw new RuntimeException("More than 255 fields with IDs is not allowed");

					fieldInstruction.fieldID = (byte)fieldID;
				}

				try {
					fieldInstruction.mutableType = Adaptor.isMutable(field.getType());
				} catch (AdaptorException ex) {
					continue;
				}
				registerField(field, fieldInstruction);
			}
		}
	}
	
	protected final boolean registerField(Field field, FieldDefinition.Instruction fieldInstruction) {
		FieldDefinition fieldDefinition;
		try {
			fieldDefinition = new FieldDefinition(field, fieldInstruction);
		} catch (AdaptorException ex) {
			return false;
		}
		fields.put(field.getName(), fieldDefinition);
		if(fieldInstruction.staticField)
			staticFields.add(fieldDefinition.getAdaptor());
		else if(fieldInstruction.fieldID != 0)
			fieldMap.put((byte)fieldInstruction.fieldID, fieldDefinition.getAdaptor());
		else
			mutableFields.put(field.getName(), fieldDefinition.getAdaptor());
		return true;
	}
	
	public Set<String> fieldKeySet() {
		return fields.keySet();
	}
	
	public Collection<FieldDefinition> fields() {
		return fields.values();
	}
	
	public Class<?> getType() {
		return type;
	}
	
}
