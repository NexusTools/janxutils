/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import nexustools.io.data.primitives.PrimitiveAdaptor;

/**
 *
 * @author katelyn
 * 
 * @param <T> The class this adaptor is used to read/write
 */
public abstract class Adaptor<T> {
	
	private static final HashMap<Class<?>, Class<?>> classRemap = new HashMap() {
		{
			put(byte.class, Byte.class);
			put(short.class, Short.class);
			put(int.class, Integer.class);
			
			put(float.class, Float.class);
			put(double.class, Double.class);
		}
	};
	private static final HashMap<Class<?>, Adaptor> adaptors = new HashMap();

	/**
	 * Tests whether or not the given type should
	 * be expected to have a subclass or not
	 * 
	 * @param type Type to test
	 * @return true/false
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static boolean isMutable(Class<?> type) throws AdaptorException {
		if(Modifier.isFinal(type.getModifiers()))
			return false;
		
		return !hasPrimitiveAdaptor(type);
	}
	
	/**
	 * Checks whether or not a PrimitiveAdaptor exists
	 * for a given type.
	 * 
	 * @param type Type to test
	 * @return true/false
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static boolean hasPrimitiveAdaptor(Class<?> type) throws AdaptorException {
		Adaptor test = resolveAdaptor(type, false);
		return test != null && test.isPrimitive();
	}
	
	/**
	 * Attempt to find the best adaptor for the given class
	 * or return a GenericAdaptor
	 * 
	 * @param clazz Class to use for resolving adaptor
	 * @return Compatible Adaptor
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static Adaptor resolveAdaptor(Class<?> clazz) throws AdaptorException {
		return resolveAdaptor(clazz, true);
	}
	
	/**
	 * Attempt to find the best adaptor for the given class
	 * or return a GenericAdaptor
	 * 
	 * @param clazz Class to use for resolving adaptor
	 * @param allowFallback Whether or not to provide a GenericAdaptor when no other adaptor can be found
	 * @return Compatible Adaptor or null if allowFallback is disabled and no adaptor is found
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static Adaptor resolveAdaptor(Class<?> clazz, boolean allowFallback) throws AdaptorException {
		Class<?> remap = classRemap.get(clazz);
		if(remap != null)
			clazz = remap;
		
		Adaptor adaptor = adaptors.get(clazz);
		if(adaptor == null) {
			Class<?> bestMatch = null;
			for(Map.Entry<Class<?>, Adaptor> set : adaptors.entrySet()) {
				Class<?> test = set.getKey();
				if(test.isAssignableFrom(clazz) && (bestMatch == null || bestMatch.isAssignableFrom(test))) {
					adaptor = set.getValue();
					bestMatch = test;
				}
			}
		}
		if(adaptor == null && allowFallback) {
			try {
				if(clazz.newInstance() == null)
					throw new RuntimeException("newInstance returned null");
			} catch (IllegalAccessException | InstantiationException | SecurityException ex) {
				throw new AdaptorException(clazz.getName() + " has no accessible empty constructions, and cannot be used without a custom Adaptor", ex);
			}
			try {
				adaptor = new GenericAdaptor<>(clazz);
				((GenericAdaptor)adaptor).validate();
			} catch(RuntimeException ex) {
				throw new AdaptorException(clazz.getName() + " has no valid adaptors", ex);
			}
		}
		
		return adaptor;
	}
	
	/**
	 * Indicates whether or not this adaptor
	 * is for a primitive type.
	 * 
	 * Primitive adaptors cannot have read values into
	 * themselves and instead always create new instances
	 * of the class they're meant for
	 * 
	 * Only adaptors that extend PrimitiveAdaptor can be primitive
	 * 
	 * @return
	 */
	public final boolean isPrimitive() {
		return this instanceof PrimitiveAdaptor;
	}
	
	/**
	 * Return the type of data this adaptor is meant to work with.
	 * 
	 * @return
	 */
	public abstract Class<?> getType();
	
	public static void writeMutable(Object obj, DataOutputStream out) throws IOException, UnsupportedOperationException, AdaptorException {
		if(obj == null) {
			out.writeUTF8(null);
			return;
		}
		
		out.writeUTF8(obj.getClass().getName());
		resolveAndWrite(obj, out);
	}
	
	public static void writeMutable(Object obj, OutputStream out) throws IOException, UnsupportedOperationException, AdaptorException {
		writeMutable(obj, new DataOutputStream(out));
	}

	/**
	 * Attempts to resolve an instance of a class from the input stream
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static Object readMutable(DataInputStream in) throws IOException, ClassNotFoundException, UnsupportedOperationException, AdaptorException {
		String className = in.readUTF8();
		if(className == null)
			return null;
		
		return resolveAndReadInstance(Class.forName(className), in);
	}
	
	public static Object readMutable(InputStream in) throws IOException, ClassNotFoundException, UnsupportedOperationException, AdaptorException {
		return readMutable(new DataInputStream(in));
	}
	
	/**
	 * Resolves an adaptor for the given class and
	 * attempts to read a new instance of of that class
	 * 
	 * 
	 * @param target Target class
	 * @param inStream Stream to read from
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static Object resolveAndReadInstance(Class<?> target, DataInputStream inStream) throws UnsupportedOperationException, IOException, AdaptorException {
		Adaptor adaptor = resolveAdaptor(target);
		if(adaptor == null)
			throw new UnsupportedOperationException("Unable to find adaptor for `" + target.getName() + "`");
		
		try {
			Object newInstance = target.newInstance();
			adaptor.read(newInstance, inStream);
			return newInstance;
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new AdaptorException("Cannot create instance of: " + target.getName(), ex);
		}
	}
	
	/**
	 * Attempts to resolve an adaptor for the given object
	 * and read data into it
	 * 
	 * @param target
	 * @param inStream
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static void resolveAndRead(Object target, DataInputStream inStream) throws UnsupportedOperationException, IOException, AdaptorException {
		Adaptor adaptor = resolveAdaptor(target.getClass());
		if(adaptor == null)
			throw new UnsupportedOperationException("Unable to find adaptor for `" + target.getClass().getName() + "`");
		adaptor.read(target, inStream);
	}
	
	/**
	 * Attempts to resolve an adaptor for the given object
	 * and write it into the output stream
	 * 
	 * @param target
	 * @param outStream
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static void resolveAndWrite(Object target, DataOutputStream outStream) throws UnsupportedOperationException, IOException, AdaptorException {
		Adaptor adaptor = resolveAdaptor(target.getClass());
		if(adaptor == null)
			throw new UnsupportedOperationException("Unable to find adaptor for `" + target.getClass().getName() + "`");
		adaptor.write(target, outStream);
	}
	
	/**
	 * Registers an adaptor
	 * The type the adaptor is for is derived
	 * from its getType method.
	 * 
	 * @param adaptor
	 */
	public static void register(Adaptor adaptor) {
		adaptors.put(adaptor.getType(), adaptor);
	}
	
	static {
		// Primitives
		register(new nexustools.io.data.primitives.StringAdaptor());
		
		register(new nexustools.io.data.primitives.ByteAdaptor());
		register(new nexustools.io.data.primitives.ShortAdaptor());
		register(new nexustools.io.data.primitives.IntegerAdaptor());
		
		register(new nexustools.io.data.primitives.FloatAdaptor());
		register(new nexustools.io.data.primitives.DoubleAdaptor());
		
		// NexusTools Primitives
		register(new VersionAdaptor());
		
		// Collections and Maps
		register(new CollectionAdaptor());
		register(new MapAdaptor());
		
		System.out.println("Locating Adaptors");
		for(AdaptorProvider adaptorProvider : ServiceLoader.load(AdaptorProvider.class)) {
			System.out.println(adaptorProvider.getClass().getName());
			for(Adaptor adaptor : adaptorProvider.getAdaptors())
				register(adaptor);
		}
	}
	
	public T readInstance(DataInputStream in) throws IOException{
		T instance = createInstance(in);
		read(instance, in);
		return instance;
	}
	public T createInstance(DataInputStream in) throws IOException{
		try {
			return (T) getType().newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
	public abstract void write(T target, DataOutputStream out) throws IOException;
	public abstract void read(T target, DataInputStream in) throws IOException;
	
}
