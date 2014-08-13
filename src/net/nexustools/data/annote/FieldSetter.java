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

package net.nexustools.data.annote;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author katelyn
 * 
 * Indicates a method to act as the getter of a field
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldSetter {
	
	/**
	 * Indicates the name to use for this field,
	 * a name is derived automatically by default.
	 * 
	 * @return 
	 */
	String fieldName() default "";
	
	/**
	 * Indicates whether or not this field is going to move,
	 * static fields are written without any indicators.
	 * 
	 * Static Fields are written in the order they appear in the class
	 * 
	 * @return
	 */
	boolean staticField() default false;
	
	/**
	 * Indicates the ID to use for storing this field
	 * if this ID is 0, the field name is used instead
	 * 
	 * Unless the class as a ClassStream annotation with
	 * the autogenFieldIDs attribute set to true
	 * 
	 * @return true/false
	 */
	byte fieldID() default 0;

	/**
	 * Indicates whether or not this field's type
	 * can change from the one designated by the field itself.
	 * 
	 * This includes things like using a HashMap when the field says Map
	 * and other subclassing.
	 * 
	 * This adds extra logic to resolve the value type during read/write
	 * 
	 * @return
	 */
	boolean mutableType() default true;

	/**
	 * This indicates whether the field would ever be
	 * set to null or not.
	 * 
	 * @return
	 */
	boolean neverNull() default false;
	
	/**
	* This indicates the revision this field was added on,
	* if more than 0, a revision is also added to the output stream.
	* 
	* Both this and {@link #depreciated} control the detected
	* revision of a containing class.
	*
	* @return Current revision as long
	*/
	long revision() default 0;
	
	/**
	* This indicates the revision this field was depreciated on,
	* if less than Long.MAX_VALUE, a revision is also added to the output stream.
	* 
	* Both this and {@link #revision} control the detected
	* revision of a containing class.
	*
	* @return Revision this field was depreciated on as long
	*/
	long depreciated() default Long.MAX_VALUE;
	
	/**
	* This allows you to specify a string defining revisioning overtime,
	* the format of this string is still being decided.
	* 
	* It can be used to indicate if a field has been depreciated and brought back,
	* as well as the types it has been overtime, and a method to use to translate.
	*
	* @return Definition as string
	*/
	//long depreciated() default Long.MAX_VALUE; // TODO: Implement
	
}
