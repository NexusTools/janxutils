/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.io.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author katelyn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ClassStream {
	
	/**
	 * Whether or not public fields should be read/written
	 * @return true/false
	 */
	boolean publicFields() default true;
	
	/**
	 * Whether or not protected fields should be read/written
	 * @return true/false
	 */
	boolean protectedFields() default false;
	
	/**
	 * Whether or not private fields should be read/written
	 * @return true/false
	 */
	boolean privateFields() default false;
	
	/**
	 * Whether or not transient fields should be read/written
	 * 
	 * @return true/false
	 */
	boolean transientFields() default false;
	
	/**
	 * Whether or not volatile fields should be read/written
	 * 
	 * @return true/false
	 */
	boolean volatileFields() default false;
	
	
	/**
	 * Whether or not to store the fields as they appear,
	 * without indicating any form of identifier
	 * 
	 * This option overrides 
	 * 
	 * @return true/false
	 */
	boolean staticFieldLayout() default false;
	
	
	/**
	 * Whether IDs should be generated for fields without them already
	 * this can save space, but would break things if a field is added/removed
	 * 
	 * IDs are generated by taking the list of fields without IDs and
	 * ordering them alphabetically, than running through them and assigning
	 * unused IDs in the order they appear after being sorted
	 * 
	 * This option does nothing for static fields
	 * and is inherently incompatible with staticFieldLayout
	 * 
	 * @return true/false
	 */
	boolean autogenFieldIDs() default false;
	
}
