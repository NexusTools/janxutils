/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
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
	
	protected static class ClassStreamInstructions {
		protected static class FieldInstruction {
			
			boolean staticField = false;
			boolean mutableType = true;
			boolean neverNull = false;
			byte fieldID = 0;
			
		}
		
		protected static class ClassInstruction {
			
			boolean publicFields = true;
			boolean protectedFields = false;
			boolean privateFields = false;
			boolean transientFields = false;
			boolean volatileFields = false;
			boolean staticFieldLayout = false;
			boolean autogenFieldIDs = false;
			
		}
		
		public static abstract class FieldAdaptor {
			
			public final Field field;
			protected FieldAdaptor(Field field) {
				this.field = field;
				this.field.setAccessible(true);
			}
			
			public abstract void write(Object target, DataOutputStream out) throws IOException;
			public abstract void read(Object target, DataInputStream in) throws IOException;
			
			@Override
			public String toString() {
				return "FieldAdaptor@" + field.getDeclaringClass().getName() + ":" + field.getName();
			}
			
		}
		
		private final ClassStreamInstructions superInstructions;
		protected ClassStreamInstructions(Class<?> clazz) {
			System.out.println("Parsing instructions for: " + clazz.getName());
			Class<?> superClazz = clazz.getSuperclass();
			if(superClazz != null && superClazz != Object.class) {
				superInstructions = lookupStreamable(superClazz);
				staticFields.addAll(superInstructions.staticFields);
				for(Map.Entry<String, FieldAdaptor> entry : otherFields.entrySet())
					otherFields.put(entry.getKey(), entry.getValue());
				for(Map.Entry<Byte, FieldAdaptor> entry : fieldMap.entrySet())
					fieldMap.put(entry.getKey(), entry.getValue());
			} else
				superInstructions = null;
			
			ArrayList<Field> unknownFields = new ArrayList();
			for(Field field : clazz.getDeclaredFields()) {
				FieldStream fieldStream = field.getAnnotation(FieldStream.class);

				if(fieldStream != null) {
					FieldInstruction fieldInstruction = new FieldInstruction();

					fieldInstruction.staticField = fieldStream.staticField();
					fieldInstruction.mutableType = fieldStream.mutableType();
					try {
						fieldInstruction.mutableType = fieldInstruction.mutableType && Adaptor.isMutable(field.getType());
					} catch (AdaptorException ex) {}
					fieldInstruction.neverNull = fieldStream.neverNull();
					fieldInstruction.fieldID = fieldStream.fieldID();

					registerField(field, fieldInstruction);
				} else {
					if(Modifier.isFinal(field.getModifiers()) ||
							Modifier.isStatic(field.getModifiers()))
						continue;
					
					System.out.println("Added " + field.getName() + " to Process Queue");
					unknownFields.add(field);
				}
			}
			
			
			if(unknownFields.size() > 0) {
				System.out.println(unknownFields.size() + " other fields to process");
				
				ClassInstruction classInstruction = new ClassInstruction();
				{
					ClassStream classStream = clazz.getAnnotation(ClassStream.class);
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
					
					FieldInstruction fieldInstruction = new FieldInstruction();
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
			
			System.out.println(staticFields);
			System.out.println(this.otherFields);
			System.out.println(fieldMap);
		}
		
		protected final void registerField(Field field, FieldInstruction fieldInstruction) {
			System.out.println("Registering Field: " + field.getName());
			
			FieldAdaptor fieldAdaptor;
			final Adaptor adaptor;
			try {
				adaptor = Adaptor.resolveAdaptor(field.getType());
			} catch (AdaptorException ex) {
				return;
			}
			if(fieldInstruction.mutableType)
				fieldAdaptor = new FieldAdaptor(field) {

					@Override
					public void read(Object target, DataInputStream in) throws IOException {
						try {
							field.set(target, in.readMutableObject());
						} catch (IllegalArgumentException | IllegalAccessException ex) {
							throw new IOException(ex);
						}
					}

					@Override
					public void write(Object target, DataOutputStream out) throws IOException {
						try {
							out.writeMutableObject(field.get(target));
						} catch (IllegalArgumentException | IllegalAccessException ex) {
							throw new IOException(ex);
						}
					}
					
				};
			else {
				if(fieldInstruction.neverNull)
					fieldAdaptor = new FieldAdaptor(field) {

						@Override
						public void read(Object target, DataInputStream in) throws IOException {
							try {
								Object value;
								if(adaptor.isPrimitive())
									value = adaptor.readInstance(in);
								else {
									value = field.get(target);
									adaptor.read(value, in);
								}
								field.set(target, value);
							} catch (IllegalArgumentException | IllegalAccessException ex) {
								throw new IOException();
							}
						}

						@Override
						public void write(Object target, DataOutputStream out) throws IOException {
							try {
								adaptor.write(field.get(target), out);
							} catch (IllegalArgumentException | IllegalAccessException ex) {
								throw new IOException(ex);
							}
						}

					};
				else
					fieldAdaptor = new FieldAdaptor(field) {

						@Override
						public void read(Object target, DataInputStream in) throws IOException {
							try {
								Object value;
								if(!in.readBoolean())
									value = null;
								else if(adaptor.isPrimitive())
									value = adaptor.readInstance(in);
								else {
									value = field.get(target);
									adaptor.read(value, in);
								}

								field.set(target, value);
							} catch (IllegalArgumentException | IllegalAccessException ex) {
								throw new IOException();
							}
						}

						@Override
						public void write(Object target, DataOutputStream out) throws IOException {
							try {
								Object value = field.get(target);
								if(value == null) {
									out.writeBoolean(false);
									return;
								} else
									out.writeBoolean(true);

								adaptor.write(field.get(target), out);
							} catch (IllegalArgumentException | IllegalAccessException ex) {
								throw new IOException(ex);
							}
						}
					};
			}
			
			if(fieldInstruction.staticField)
				staticFields.add(fieldAdaptor);
			else if(fieldInstruction.fieldID != 0)
				fieldMap.put((byte)fieldInstruction.fieldID, fieldAdaptor);
			else
				otherFields.put(fieldAdaptor.field.getName(), fieldAdaptor);
		}
		
		public final ArrayList<FieldAdaptor> staticFields = new ArrayList();
		public final HashMap<String, FieldAdaptor> otherFields = new HashMap();
		public final HashMap<Byte, FieldAdaptor> fieldMap = new HashMap();
	}

	private final Class<?> target;
	private final ClassStreamInstructions typeInstructions;
	
	public GenericAdaptor(Class<?> target) {
		this.typeInstructions = lookupStreamable(target);
		this.target = target;
	}

	@Override
	public Class<?> getType() {
		return target;
	}

	@Override
	public void write(T target, DataOutputStream out) throws IOException {
		ClassStreamInstructions instructions = lookupStreamable(target.getClass());
		for(ClassStreamInstructions.FieldAdaptor fAdaptor : instructions.staticFields) {
			fAdaptor.write(target, out);
		}
		out.writeByte(instructions.fieldMap.size());
		for(Map.Entry<Byte, ClassStreamInstructions.FieldAdaptor> entry : instructions.fieldMap.entrySet()) {
			out.writeByte(entry.getKey());
			entry.getValue().write(target, out);
		}
		out.writeByte(instructions.otherFields.size());
		for(Map.Entry<String, ClassStreamInstructions.FieldAdaptor> entry : instructions.otherFields.entrySet()) {
			out.writeUTF8(entry.getKey());
			entry.getValue().write(target, out);
		}
	}

	@Override
	public void read(T target, DataInputStream in) throws IOException {
		ClassStreamInstructions instructions = lookupStreamable(target.getClass());
		for(ClassStreamInstructions.FieldAdaptor fAdaptor : instructions.staticFields) {
			fAdaptor.read(target, in);
		}
		
		int fieldsLeft = (int) (in.readByte() & 0xff);
		while(fieldsLeft-- > 0)
			instructions.fieldMap.get(in.readByte()).read(target, in);
		
		fieldsLeft = (int) (in.readByte() & 0xff);
		while(fieldsLeft-- > 0)
			instructions.otherFields.get(in.readUTF8()).read(target, in);
	}
	
	private static WeakHashMap<Class<?>, ClassStreamInstructions> streamableFieldCache = new WeakHashMap();
	private static ClassStreamInstructions lookupStreamable(Class<?> target) {
		ClassStreamInstructions cache = streamableFieldCache.get(target);
		if(cache == null) {
			cache = new ClassStreamInstructions(target);
			streamableFieldCache.put(target, cache);
		}
		return cache;
	}
	

	/**
	 * Checks whether or not the underlying type
	 * actually has any field instructions
	 * 
	 * @throws RuntimeException
	 */
	public void validate() {
		if(typeInstructions.staticFields.isEmpty() &&
				typeInstructions.fieldMap.isEmpty() &&
				typeInstructions.otherFields.isEmpty())
			throw new RuntimeException(getType().getName() + " has no usable fields");
	}

	
}
