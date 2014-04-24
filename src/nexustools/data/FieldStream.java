/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author katelyn
 * 
 * Indicates a property should be read/written
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldStream {
	
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
	
}
