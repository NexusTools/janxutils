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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import net.nexustools.data.adaptor.Adaptor;
import net.nexustools.data.adaptor.AdaptorException;
import net.nexustools.data.annote.ClassStream;
import net.nexustools.data.annote.FieldStream;
import net.nexustools.utils.log.Logger;

/**
 *
 * @author katelyn
 */
public final class ClassDefinition {
	
	private static final WeakHashMap<Class<?>, ClassDefinition> instances = new WeakHashMap();
	public static synchronized ClassDefinition load(Class<?> type) {
		ClassDefinition def = instances.get(type);
		if(def == null) {
			if(type.getName().startsWith("java.lang.reflect")) {
				Logger.error("Cannot handle", type.getName());
				throw new RuntimeException("java.lang.reflect.* not supported");
			}
			
			Logger.gears("Creating definition for", type.getName());
			def = new ClassDefinition(type);
			instances.put(type, def);
		}
		return def;
	}
	
	public static boolean hasAnnotation(Class<?> type, Class<? extends Annotation> annote) {
		return load(type).hasAnnotation(annote);
	}

	public static String shortNameFor(Class<? extends Object> type) {
		return load(type).shortName();
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
	private final String shortName;
	private final ClassDefinition superDefinition;
	private final HashMap<Class<? extends Annotation>, Annotation> annotations = new HashMap();
	private final HashMap<String, FieldDefinition> fields = new HashMap();
	
	protected final ArrayList<FieldDefinition.Adaptor> staticFields = new ArrayList();
	protected final HashMap<String, FieldDefinition.Adaptor> mutableFields = new HashMap();
	protected final HashMap<Byte, FieldDefinition.Adaptor> fieldMap = new HashMap();
	private ClassDefinition(Class<?> type) {
		Class<?> superClazz = type.getSuperclass();
		if(superClazz != null && !superClazz.equals(Object.class))
			superDefinition = load(superClazz);
		else
			superDefinition = null;
		this.type = type;
		process();
		
		
		String sName = type.getSimpleName();
		if(sName == null || sName.length() < 1)
			sName = type.getName();
		this.shortName = sName;
	}
	
	public String shortName() {
		return shortName;
	}
	
	public Map<Class<? extends Annotation>, Annotation> annotations() {
		return annotations;
	}
	
	public boolean hasAnnotation(Class<? extends Annotation> annote) {
		return annotations.containsKey(annote);
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
			annotations.putAll(superDefinition.annotations);
		}
		for(Annotation annote : type.getDeclaredAnnotations()) {
			if(annote.getClass().isInterface())
				annotations.put(annote.getClass(), annote); 
			else
				for(Class<?> tmpl : annote.getClass().getInterfaces())
					if(Annotation.class.isAssignableFrom(tmpl))
						annotations.put((Class<Annotation>)tmpl, annote);
		}

		ArrayList<Field> unknownFields = new ArrayList();
		for(Field field : type.getDeclaredFields()) {
			Logger.gears(field.getName(), field);
			FieldStream fieldStream = field.getAnnotation(FieldStream.class);

			try {
				if(fieldStream != null) {
					Logger.gears("Found FieldStream annotation", fieldStream);
					FieldDefinition.Instruction fieldInstruction = new FieldDefinition.Instruction();

					fieldInstruction.staticField = fieldStream.staticField();
					fieldInstruction.mutableType = fieldStream.mutableType();
					fieldInstruction.mutableType = fieldInstruction.mutableType && Adaptor.isMutable(field.getType());
					fieldInstruction.neverNull = fieldStream.neverNull();
					fieldInstruction.fieldID = fieldStream.fieldID();

					registerField(field, fieldInstruction);
				} else {
					if(Modifier.isFinal(field.getModifiers()) ||
							Modifier.isStatic(field.getModifiers()))
						continue;

					unknownFields.add(field);
				}
			} catch(AdaptorException ex) {
				Logger.exception(Logger.Level.Warning, ex);
			}
		}

		if(unknownFields.size() > 0) {
			Instruction classInstruction = new Instruction();
			{
				ClassStream classStream = type.getAnnotation(ClassStream.class);
				if(classStream != null) {
					Logger.gears("Found ClassStream annotation");
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
					registerField(field, fieldInstruction);
				} catch (AdaptorException ex) {
					Logger.exception(Logger.Level.Gears, ex);
					continue;
				}
			}
		}
		
		Logger.debug(this, "Loaded", staticFields.size(), "Static Fields", fieldMap.size(), "Mapped Fields", mutableFields.size(), "Mutable Fields", annotations.size(), "Annotations");
	}
	
	protected final void registerField(Field field, FieldDefinition.Instruction fieldInstruction) throws AdaptorException {
		FieldDefinition fieldDefinition = new FieldDefinition(field, fieldInstruction);
		
		fields.put(field.getName(), fieldDefinition);
		if(fieldInstruction.staticField)
			staticFields.add(fieldDefinition.getAdaptor());
		else if(fieldInstruction.fieldID != 0)
			fieldMap.put((byte)fieldInstruction.fieldID, fieldDefinition.getAdaptor());
		else
			mutableFields.put(field.getName(), fieldDefinition.getAdaptor());
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
