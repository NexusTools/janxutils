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

import java.io.IOException;
import java.lang.reflect.Field;
import net.nexustools.data.AdaptorException;
import net.nexustools.io.DataInputStream;
import net.nexustools.io.DataOutputStream;

/**
 *
 * @author katelyn
 */
public class FieldDefinition {
	
	protected static class Instruction {
		boolean staticField = false;
		boolean mutableType = true;
		boolean neverNull = false;
		byte fieldID = 0;
	}
	
	private final Field field;
	private final Adaptor adaptor;
	private final Instruction instruction;
	public abstract class Adaptor {
		protected Adaptor() {}
		public abstract void write(Object target, DataOutputStream out) throws IOException;
		public abstract void read(Object target, DataInputStream in) throws IOException;
		@Override
		public String toString() {
			return getClass().getName() + "[" + field.getDeclaringClass().getName() + ":" + field.getName() + "]";
		}
	}
	protected FieldDefinition(Field ffield, Instruction instruction) throws AdaptorException {
		this.instruction = instruction;
		ffield.setAccessible(true);
		field = ffield;
		
		final net.nexustools.data.Adaptor dataAdaptor = net.nexustools.data.Adaptor.resolveAdaptor(ffield.getType());
		if(instruction.mutableType)
			adaptor = new Adaptor() {
				@Override
				public void read(Object target, DataInputStream in) throws IOException {
					try {
						field.set(target, in.readMutableObject());
					} catch (ReflectiveOperationException ex) {
						throw new IOException(ex);
					}
				}
				@Override
				public void write(Object target, DataOutputStream out) throws IOException {
					try {
						out.writeMutableObject(field.get(target));
					} catch (ReflectiveOperationException ex) {
						throw new IOException(ex);
					}
				}
			};
		else {
			if(instruction.neverNull || dataAdaptor.isPrimitive()) // Primitives are considered neverNull
				adaptor = new Adaptor() {

					@Override
					public void read(Object target, DataInputStream in) throws IOException {
						try {
							Object value;
							if(dataAdaptor.isPrimitive())
								value = dataAdaptor.readInstance(in, field.getType());
							else {
								value = field.get(target);
								dataAdaptor.read(value, in);
							}
							field.set(target, value);
						} catch (ReflectiveOperationException ex) {
							throw new IOException();
						}
					}

					@Override
					public void write(Object target, DataOutputStream out) throws IOException {
						try {
							dataAdaptor.write(field.get(target), out);
						} catch (ReflectiveOperationException ex) {
							throw new IOException(ex);
						}
					}

				};
			else
				adaptor = new Adaptor() {

					@Override
					public void read(Object target, DataInputStream in) throws IOException {
						try {
							Object value;
							if(!in.readBoolean())
								value = null;
							else if(dataAdaptor.isPrimitive())
								value = dataAdaptor.readInstance(in, field.getType());
							else {
								value = field.get(target);
								dataAdaptor.read(value, in);
							}

							field.set(target, value);
						} catch (ReflectiveOperationException ex) {
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

							dataAdaptor.write(field.get(target), out);
						} catch (ReflectiveOperationException ex) {
							throw new IOException(ex);
						}
					}
				};
		}
	}
	public Adaptor getAdaptor() {
		return adaptor;
	}
	protected Instruction getInstruction() {
		return instruction;
	}
	
}
