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

package net.nexustools.data.analyze;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import net.nexustools.data.Adaptor;
import net.nexustools.data.AdaptorException;
import net.nexustools.data.annote.ClassStream;
import net.nexustools.data.annote.FieldStream;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public final class ClassDefinition {
	
	private static final WeakHashMap<Class<?>, ClassDefinition> instances = new WeakHashMap();
	public static synchronized ClassDefinition getInstance(Class<?> type) {
		ClassDefinition def = instances.get(type);
		if(def == null) {
			Logger.gears("Creating definition for", type.getName());
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
	
	// TODO: Add revision capabilities
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

	public ArrayList<FieldDefinition.Adaptor> getStaticFields() {
		return staticFields;
	}

	public HashMap<String, FieldDefinition.Adaptor> getMutableFields() {
		return mutableFields;
	}

	public HashMap<Byte, FieldDefinition.Adaptor> getFieldMap() {
		return fieldMap;
	}
	
	private void process() {
		if(superDefinition != null) {
			Logger.gears("Attaching SuperDefinition", superDefinition);
			staticFields.addAll(superDefinition.staticFields);
			for(Entry<String, FieldDefinition.Adaptor> entry : superDefinition.mutableFields.entrySet())
				mutableFields.put(entry.getKey(), entry.getValue());
			for(Entry<Byte, FieldDefinition.Adaptor> entry : superDefinition.fieldMap.entrySet())
				fieldMap.put(entry.getKey(), entry.getValue());
		}

		Logger.gears("Processing Fields", this);
		ArrayList<Field> unknownFields = new ArrayList();
		for(Field field : type.getDeclaredFields()) {
			Logger.gears(field.getName(), field);
			FieldStream fieldStream = field.getAnnotation(FieldStream.class);

			if(fieldStream != null) {
				Logger.gears("Found fieldStreak annotation", fieldStream);
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

		Logger.gears("Processing Fields", this);
		if(unknownFields.size() > 0) {
			Instruction classInstruction = new Instruction();
			{
				ClassStream classStream = type.getAnnotation(ClassStream.class);
				if(classStream != null) {
					Logger.gears("Class has ClassStream");
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
						!classInstruction.publicFields) {
					Logger.gears("Skipping public", field.getName());
					continue;
				}

				if(Modifier.isProtected(field.getModifiers()) &&
						!classInstruction.protectedFields) {
					Logger.gears("Skipping protected", field.getName());
					continue;
				}

				if(Modifier.isPrivate(field.getModifiers()) &&
						!classInstruction.privateFields) {
					Logger.gears("Skipping private", field.getName());
					continue;
				}

				if(Modifier.isVolatile(field.getModifiers()) &&
						!classInstruction.volatileFields) {
					Logger.gears("Skipping volatile", field.getName());
					continue;
				}

				if(Modifier.isTransient(field.getModifiers()) &&
						!classInstruction.transientFields) {
					Logger.gears("Skipping transient", field.getName());
					continue;
				}

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
					Logger.exception(Logger.Level.Gears, ex);
					continue;
				}
				registerField(field, fieldInstruction);
			}
		}
		
		Logger.debug(this, "Loaded", staticFields.size(), "Static Fields", fieldMap.size(), "Mapped Fields", mutableFields.size(), "Mutable Fields");
	}
	
	protected final boolean registerField(Field field, FieldDefinition.Instruction fieldInstruction) {
		FieldDefinition fieldDefinition;
		try {
			fieldDefinition = new FieldDefinition(field, fieldInstruction);
		} catch (AdaptorException ex) {
			Logger.exception(Logger.Level.Gears, ex);
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

	@Override
	public String toString() {
		return "Definition(" + type.getName() + ")";
	}
	
}
