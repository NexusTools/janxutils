/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import nexustools.io.DataInputStream;
import nexustools.io.DataOutputStream;
import nexustools.data.primitives.PrimitiveAdaptor;
import nexustools.utils.ClassUtils;

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
	 * @throws nexustools.data.AdaptorException
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
	 * @throws nexustools.data.AdaptorException
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
	 * @throws nexustools.data.AdaptorException
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
	 * @throws nexustools.data.AdaptorException
	 */
	public static Adaptor resolveAdaptor(Class<?> clazz, boolean allowFallback) throws AdaptorException {
		Class<?> remap = classRemap.get(clazz);
		if(remap != null)
			clazz = remap;
		
		Adaptor adaptor = adaptors.get(clazz);
		Map.Entry<Class<?>, Adaptor> match =  ClassUtils.bestMatch(clazz, adaptors.entrySet());
		if(match != null)
			adaptor = match.getValue();
		if(adaptor == null && allowFallback) {
			try {
				adaptor = new GenericAdaptor<>(clazz);
				((GenericAdaptor)adaptor).validate();
			} catch(RuntimeException ex) {
				throw new AdaptorException("Cannot create fallback adaptor for: " + clazz.getName(), ex);
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
	 * @return Whether or not this Adaptor extends {@link PrimitiveAdaptor}
	 */
	public final boolean isPrimitive() {
		return this instanceof PrimitiveAdaptor;
	}
	
	/**
	 * Return the type of data this adaptor is meant to work with.
	 * 
	 * @return
	 */
	public abstract Class<? extends T> getType();
	
	/**
	 * Writes an object to a {@link DataOutputStream} along with type information
	 * 
	 * @param obj Object to write
	 * @param out Stream to write to
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws AdaptorException
	 */
	public static void resolveAndWriteMutable(Object obj, DataOutputStream out) throws IOException, UnsupportedOperationException, AdaptorException {
		if(obj == null) {
			out.writeUTF8(null);
			return;
		}
		
		out.writeUTF8(obj.getClass().getName());
		resolveAndWrite(obj, out);
	}
	
	public static void resolveAndWriteMutable(Object obj, OutputStream out) throws IOException, UnsupportedOperationException, AdaptorException {
		resolveAndWriteMutable(obj, new DataOutputStream(out));
	}

	/**
	 * Attempts to resolve an instance of a class from the input stream
	 * 
	 * @param in Stream to read from
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws nexustools.io.data.AdaptorException
	 */
	public static Object resolveAndReadMutable(DataInputStream in) throws IOException, ClassNotFoundException, UnsupportedOperationException, AdaptorException {
		String className = in.readUTF8();
		if(className == null)
			return null;
		
		return resolveAndReadInstance(Class.forName(className), in);
	}
	
	/**
	 * Resolves an adaptor and attempts to read a new instance of
	 * the type from the {@link DataInputStream} given
	 * 
	 * @param in Stream to read from
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws UnsupportedOperationException
	 * @throws AdaptorException
	 */
	public static Object resolveAndReadMutable(InputStream in) throws IOException, ClassNotFoundException, UnsupportedOperationException, AdaptorException {
		return resolveAndReadMutable(new DataInputStream(in));
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
	 * @throws nexustools.data.AdaptorException
	 */
	public static Object resolveAndReadInstance(Class<?> target, DataInputStream inStream) throws UnsupportedOperationException, IOException, AdaptorException {
		Adaptor adaptor = resolveAdaptor(target);
		if(adaptor == null)
			throw new UnsupportedOperationException("Unable to find adaptor for `" + target.getName() + "`");
		
		return adaptor.readInstance(inStream, target);
	}
	
	/**
	 * Attempts to resolve an adaptor for the given object
	 * and read data into it
	 * 
	 * @param target
	 * @param inStream
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws nexustools.data.AdaptorException
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
	 * @throws nexustools.data.AdaptorException
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
		System.out.println("Loading data adaptors");
		// Primitives
		register(new nexustools.data.primitives.StringAdaptor());
		
		register(new nexustools.data.primitives.ByteAdaptor());
		register(new nexustools.data.primitives.ShortAdaptor());
		register(new nexustools.data.primitives.IntegerAdaptor());
		
		register(new nexustools.data.primitives.FloatAdaptor());
		register(new nexustools.data.primitives.DoubleAdaptor());
		
		// NexusTools Primitives
		register(new VersionAdaptor());
		
		// Collections and Maps
		register(new CollectionAdaptor());
		register(new MapAdaptor());
		
		System.out.println("Registered " + adaptors.size() + " built-in adaptors");
		int adaptorCount = adaptors.size();
		
		for(AdaptorProvider adaptorProvider : ServiceLoader.load(AdaptorProvider.class)) {
			for(Adaptor adaptor : adaptorProvider.getAdaptors())
				register(adaptor);
			
			System.out.println(adaptorProvider.getClass().getName() + " registered " +(adaptors.size()-adaptorCount) + " adaptors");
			adaptorCount = adaptors.size();
		}
		System.out.println(adaptors.size() + " adaptors loaded");
	}
	
	public final T readInstance(DataInputStream in) throws IOException{
		return readInstance(in, getType());
	}
	public final T createInstance(DataInputStream in) throws IOException{
		return createInstance(in, getType());
	}
	
	/**
	 * Reads a new instance of the type for this adaptor, or a subclass
	 *  
	 * @param in Stream to read from
	 * @param type Type to create
	 * @return A new copy of type read from the DataInputStream
	 * @throws IOException
	 */
	public T readInstance(DataInputStream in, Class<? extends T> type) throws IOException{
		T instance = createInstance(in, type);
		read(instance, in);
		return instance;
	}

	/**
	 * Creates a new instance of the type for this adaptor, or a subclass
	 * 
	 * @param in Stream to read from
	 * @param type Type to create
	 * @return A new uninitialized copy of the requested type
	 * @throws IOException
	 */
	public T createInstance(DataInputStream in, Class<? extends T> type) throws IOException{
		try {
			return (T) type.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Writes target into a {@link DataOutputStream}
	 * 
	 * @param target Target to write
	 * @param out Stream to write into
	 * @throws IOException
	 */
	public abstract void write(T target, DataOutputStream out) throws IOException;

	/**
	 * Reads this object from a {@link DataOutputStream}
	 * 
	 * @param target Target to read
	 * @param in Stream to read from
	 * @throws IOException
	 */
	public abstract void read(T target, DataInputStream in) throws IOException;
	
}
